import Foundation

// MARK: - Errors

enum SupabaseError: LocalizedError {
    case notAuthenticated
    case sessionExpired
    case missingProfile
    case invalidURL
    case invalidResponse
    case http(status: Int, message: String)
    case decoding(Error)
    case encoding(Error)
    case transport(Error)

    var errorDescription: String? {
        switch self {
        case .notAuthenticated:
            return "You're not signed in."
        case .sessionExpired:
            return "Your session expired. Please sign in again."
        case .missingProfile:
            return "We couldn't find your profile. Finish onboarding to get recommendations."
        case .invalidURL:
            return "The Supabase URL is invalid. Check SUPABASE_URL in Info.plist."
        case .invalidResponse:
            return "The server returned an unexpected response."
        case let .http(status, message):
            return "Server error (\(status)): \(message)"
        case .decoding:
            return "We received data in an unexpected format."
        case .encoding:
            return "We couldn't prepare the request."
        case let .transport(error):
            return error.localizedDescription
        }
    }
}

// MARK: - Auth session

struct AuthSession: Sendable {
    let accessToken: String
    let refreshToken: String
    let expiresAt: Date
    let userID: UUID
}

// MARK: - Service

/// Talks to Supabase (PostgREST + GoTrue) using plain `URLSession`.
/// An `actor` so the auth session can be refreshed safely from concurrent calls.
actor SupabaseService: VolunteerDataProviding {
    /// Minimum cosine similarity for a recommendation. MiniLM scores for
    /// related-but-not-identical text usually land around 0.2–0.6.
    static let matchThreshold: Double = 0.15

    private static let eventColumns = "id,title,organization_name,location,event_date"

    let config: SupabaseConfig
    private let urlSession: URLSession
    private var authSession: AuthSession?
    private var refreshTask: Task<AuthSession, Error>?

    init(config: SupabaseConfig, urlSession: URLSession = .shared) {
        self.config = config
        self.urlSession = urlSession
    }

    var isAuthenticated: Bool { authSession != nil }

    // MARK: Auth

    @discardableResult
    func signIn(email: String, password: String) async throws -> UUID {
        let body = try encode(PasswordGrant(email: email, password: password))
        let data = try await send(
            Endpoint(
                path: "auth/v1/token",
                method: .post,
                query: [URLQueryItem(name: "grant_type", value: "password")],
                body: body
            ),
            auth: .anonymous
        )
        let session = try Self.makeSession(from: data)
        authSession = session
        return session.userID
    }

    func signOut() async {
        if authSession != nil {
            _ = try? await send(Endpoint(path: "auth/v1/logout", method: .post))
        }
        refreshTask?.cancel()
        refreshTask = nil
        authSession = nil
    }

    // MARK: VolunteerDataProviding

    func fetchCurrentProfile() async throws -> UserProfile {
        let userID = try requireUserID()
        let data = try await send(Endpoint(
            path: "rest/v1/users",
            query: [
                URLQueryItem(name: "select", value: "id,full_name,intended_major,target_club,goal_hours,club_deadlines,interest_embedding"),
                URLQueryItem(name: "id", value: "eq.\(userID.uuidString.lowercased())"),
                URLQueryItem(name: "limit", value: "1"),
            ]
        ))
        guard let profile = try decode([UserProfile].self, from: data).first else {
            throw SupabaseError.missingProfile
        }
        return profile
    }

    func fetchRecommendedEvents(for profile: UserProfile, limit: Int) async throws -> [VolunteerEvent] {
        let safeLimit = min(max(limit, 1), 50)

        // No embedding yet (e.g. onboarding not finished): show upcoming events.
        guard let embedding = profile.interestEmbedding, !embedding.isEmpty else {
            return try await fetchUpcomingEvents(limit: safeLimit)
        }

        let params = MatchEventsParams(
            queryEmbedding: embedding,
            matchThreshold: Self.matchThreshold,
            matchCount: safeLimit
        )
        let data = try await send(Endpoint(
            path: "rest/v1/rpc/match_events",
            method: .post,
            body: try encode(params)
        ))

        // `match_events` does not filter by date, so drop past events here.
        let startOfToday = Calendar.current.startOfDay(for: .now)
        return try decode([VolunteerEvent].self, from: data)
            .filter { $0.eventDate >= startOfToday }
    }

    func fetchVerifiedHours() async throws -> Double {
        let userID = try requireUserID()
        let data = try await send(Endpoint(
            path: "rest/v1/attendance_logs",
            query: [
                URLQueryItem(name: "select", value: "awarded_hours"),
                URLQueryItem(name: "user_id", value: "eq.\(userID.uuidString.lowercased())"),
                URLQueryItem(name: "status", value: "eq.verified"),
            ]
        ))
        return try decode([HoursRow].self, from: data)
            .reduce(0) { $0 + ($1.awardedHours ?? 0) }
    }

    func fetchRSVPdEventIDs() async throws -> Set<UUID> {
        let userID = try requireUserID()
        let data = try await send(Endpoint(
            path: "rest/v1/rsvps",
            query: [
                URLQueryItem(name: "select", value: "event_id"),
                URLQueryItem(name: "user_id", value: "eq.\(userID.uuidString.lowercased())"),
            ]
        ))
        return Set(try decode([RSVPRow].self, from: data).map(\.eventID))
    }

    func fetchUpcomingRSVPEvents() async throws -> [VolunteerEvent] {
        let ids = try await fetchRSVPdEventIDs()
        guard !ids.isEmpty else { return [] }

        let idList = ids.map { $0.uuidString.lowercased() }.joined(separator: ",")
        let data = try await send(Endpoint(
            path: "rest/v1/events",
            query: [
                URLQueryItem(name: "select", value: Self.eventColumns),
                URLQueryItem(name: "id", value: "in.(\(idList))"),
                URLQueryItem(name: "event_date", value: "gte.\(PostgresDate.string(from: .now))"),
                URLQueryItem(name: "order", value: "event_date.asc"),
            ]
        ))
        return try decode([VolunteerEvent].self, from: data).map { event in
            var event = event
            event.isRSVPd = true
            return event
        }
    }

    func fetchPeerRSVPCounts(for eventIDs: [UUID]) async throws -> [UUID: Int] {
        guard !eventIDs.isEmpty else { return [:] }
        let data = try await send(Endpoint(
            path: "rest/v1/rpc/get_peer_rsvp_counts",
            method: .post,
            body: try encode(PeerCountParams(eventIDs: eventIDs))
        ))
        let rows = try decode([PeerCountRow].self, from: data)
        return Dictionary(rows.map { ($0.eventID, $0.rsvpCount) }, uniquingKeysWith: { first, _ in first })
    }

    func setRSVP(_ isGoing: Bool, for eventID: UUID) async throws {
        let userID = try requireUserID()
        if isGoing {
            do {
                _ = try await send(Endpoint(
                    path: "rest/v1/rsvps",
                    method: .post,
                    body: try encode(RSVPInsert(userID: userID, eventID: eventID)),
                    headers: ["Prefer": "return=minimal"]
                ))
            } catch SupabaseError.http(let status, _) where status == 409 {
                // Already RSVP'd (primary-key conflict) — treat as success.
            }
        } else {
            _ = try await send(Endpoint(
                path: "rest/v1/rsvps",
                method: .delete,
                query: [
                    URLQueryItem(name: "user_id", value: "eq.\(userID.uuidString.lowercased())"),
                    URLQueryItem(name: "event_id", value: "eq.\(eventID.uuidString.lowercased())"),
                ],
                headers: ["Prefer": "return=minimal"]
            ))
        }
    }

    // MARK: - Private helpers

    private func fetchUpcomingEvents(limit: Int) async throws -> [VolunteerEvent] {
        let data = try await send(Endpoint(
            path: "rest/v1/events",
            query: [
                URLQueryItem(name: "select", value: Self.eventColumns),
                URLQueryItem(name: "event_date", value: "gte.\(PostgresDate.string(from: .now))"),
                URLQueryItem(name: "order", value: "event_date.asc"),
                URLQueryItem(name: "limit", value: String(limit)),
            ]
        ))
        return try decode([VolunteerEvent].self, from: data)
    }

    func requireUserID() throws -> UUID {
        guard let authSession else { throw SupabaseError.notAuthenticated }
        return authSession.userID
    }

    private enum AuthMode { case user, anonymous }

    func send(_ endpoint: Endpoint, auth: AuthMode = .user) async throws -> Data {
        let bearer: String
        switch auth {
        case .user: bearer = try await validAccessToken()
        case .anonymous: bearer = config.anonKey
        }
        return try await Self.perform(endpoint, config: config, urlSession: urlSession, bearer: bearer)
    }

    /// Returns a non-expired access token, refreshing at most once even if
    /// several requests discover the expiry at the same time.
    private func validAccessToken() async throws -> String {
        guard let current = authSession else { throw SupabaseError.notAuthenticated }
        if current.expiresAt.timeIntervalSinceNow > 60 {
            return current.accessToken
        }
        if let refreshTask {
            return try await refreshTask.value.accessToken
        }

        let config = self.config
        let urlSession = self.urlSession
        let refreshToken = current.refreshToken
        let task = Task<AuthSession, Error> {
            try await Self.refreshSession(refreshToken: refreshToken, config: config, urlSession: urlSession)
        }
        refreshTask = task
        defer { refreshTask = nil }

        do {
            let refreshed = try await task.value
            authSession = refreshed
            return refreshed.accessToken
        } catch is CancellationError {
            throw CancellationError()
        } catch {
            authSession = nil
            throw SupabaseError.sessionExpired
        }
    }

    private static func refreshSession(refreshToken: String, config: SupabaseConfig, urlSession: URLSession) async throws -> AuthSession {
        let body: Data
        do {
            body = try JSONCoding.makeEncoder().encode(RefreshGrant(refreshToken: refreshToken))
        } catch {
            throw SupabaseError.encoding(error)
        }
        let data = try await perform(
            Endpoint(
                path: "auth/v1/token",
                method: .post,
                query: [URLQueryItem(name: "grant_type", value: "refresh_token")],
                body: body
            ),
            config: config,
            urlSession: urlSession,
            bearer: config.anonKey
        )
        return try makeSession(from: data)
    }

    private static func makeSession(from data: Data) throws -> AuthSession {
        let response: TokenResponse
        do {
            response = try JSONCoding.makeDecoder().decode(TokenResponse.self, from: data)
        } catch {
            throw SupabaseError.decoding(error)
        }
        return AuthSession(
            accessToken: response.accessToken,
            refreshToken: response.refreshToken,
            expiresAt: Date.now.addingTimeInterval(TimeInterval(response.expiresIn)),
            userID: response.user.id
        )
    }

    private static func perform(
        _ endpoint: Endpoint,
        config: SupabaseConfig,
        urlSession: URLSession,
        bearer: String
    ) async throws -> Data {
        guard var components = URLComponents(
            url: config.url.appending(path: endpoint.path),
            resolvingAgainstBaseURL: false
        ) else {
            throw SupabaseError.invalidURL
        }
        if !endpoint.query.isEmpty {
            components.queryItems = endpoint.query
        }
        guard let url = components.url else { throw SupabaseError.invalidURL }

        var request = URLRequest(url: url, timeoutInterval: 20)
        request.httpMethod = endpoint.method.rawValue
        request.httpBody = endpoint.body
        request.setValue(config.anonKey, forHTTPHeaderField: "apikey")
        request.setValue("Bearer \(bearer)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if endpoint.body != nil {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }
        for (field, value) in endpoint.headers {
            request.setValue(value, forHTTPHeaderField: field)
        }

        let data: Data
        let response: URLResponse
        do {
            (data, response) = try await urlSession.data(for: request)
        } catch let urlError as URLError where urlError.code == .cancelled {
            throw CancellationError()
        } catch is CancellationError {
            throw CancellationError()
        } catch {
            throw SupabaseError.transport(error)
        }

        guard let http = response as? HTTPURLResponse else {
            throw SupabaseError.invalidResponse
        }
        guard (200..<300).contains(http.statusCode) else {
            throw SupabaseError.http(status: http.statusCode, message: APIErrorBody.message(from: data))
        }
        return data
    }

    nonisolated func decode<T: Decodable>(_ type: T.Type, from data: Data) throws -> T {
        do {
            return try JSONCoding.makeDecoder().decode(T.self, from: data)
        } catch {
            throw SupabaseError.decoding(error)
        }
    }

    nonisolated func encode<T: Encodable>(_ value: T) throws -> Data {
        do {
            return try JSONCoding.makeEncoder().encode(value)
        } catch {
            throw SupabaseError.encoding(error)
        }
    }
}

// MARK: - Wire types

struct Endpoint {
    enum Method: String { case get = "GET", post = "POST", delete = "DELETE" }

    var path: String
    var method: Method = .get
    var query: [URLQueryItem] = []
    var body: Data? = nil
    var headers: [String: String] = [:]

    init(path: String, method: Method = .get, query: [URLQueryItem] = [], body: Data? = nil, headers: [String: String] = [:]) {
        self.path = path
        self.method = method
        self.query = query
        self.body = body
        self.headers = headers
    }
}

private struct PasswordGrant: Encodable {
    let email: String
    let password: String
}

private struct RefreshGrant: Encodable {
    let refreshToken: String
    enum CodingKeys: String, CodingKey { case refreshToken = "refresh_token" }
}

private struct TokenResponse: Decodable {
    struct User: Decodable { let id: UUID }

    let accessToken: String
    let refreshToken: String
    let expiresIn: Int
    let user: User

    enum CodingKeys: String, CodingKey {
        case accessToken = "access_token"
        case refreshToken = "refresh_token"
        case expiresIn = "expires_in"
        case user
    }
}

private struct MatchEventsParams: Encodable {
    let queryEmbedding: String
    let matchThreshold: Double
    let matchCount: Int

    enum CodingKeys: String, CodingKey {
        case queryEmbedding = "query_embedding"
        case matchThreshold = "match_threshold"
        case matchCount = "match_count"
    }
}

private struct PeerCountParams: Encodable {
    let eventIDs: [UUID]
    enum CodingKeys: String, CodingKey { case eventIDs = "event_ids" }
}

private struct PeerCountRow: Decodable {
    let eventID: UUID
    let rsvpCount: Int
    enum CodingKeys: String, CodingKey {
        case eventID = "event_id"
        case rsvpCount = "rsvp_count"
    }
}

private struct HoursRow: Decodable {
    let awardedHours: Double?
    enum CodingKeys: String, CodingKey { case awardedHours = "awarded_hours" }
}

private struct RSVPRow: Decodable {
    let eventID: UUID
    enum CodingKeys: String, CodingKey { case eventID = "event_id" }
}

private struct RSVPInsert: Encodable {
    let userID: UUID
    let eventID: UUID
    enum CodingKeys: String, CodingKey {
        case userID = "user_id"
        case eventID = "event_id"
    }
}

/// PostgREST returns `{message, details, hint}`; GoTrue returns
/// `{error_description}` or `{msg}`. Pick whichever is present.
private struct APIErrorBody: Decodable {
    let message: String?
    let msg: String?
    let errorDescription: String?

    enum CodingKeys: String, CodingKey {
        case message
        case msg
        case errorDescription = "error_description"
    }

    static func message(from data: Data) -> String {
        if let body = try? JSONDecoder().decode(APIErrorBody.self, from: data),
           let text = body.errorDescription ?? body.message ?? body.msg {
            return text
        }
        return String(data: data, encoding: .utf8).flatMap { $0.isEmpty ? nil : $0 } ?? "Unknown error"
    }
}
