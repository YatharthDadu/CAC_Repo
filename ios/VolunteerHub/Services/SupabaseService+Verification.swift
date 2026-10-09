import Foundation

extension SupabaseService {
    
    // MARK: - Verification Flow

    func validateQR(seed: String) async throws -> VolunteerEvent {
        // Query the events table for an event with this exact qr_seed
        let data = try await send(Endpoint(
            path: "rest/v1/events",
            query: [
                URLQueryItem(name: "select", value: "id,title,organization_name,location,event_date"),
                URLQueryItem(name: "qr_seed", value: "eq.\(seed)"),
                URLQueryItem(name: "limit", value: "1")
            ]
        ))
        
        let events = try decode([VolunteerEvent].self, from: data)
        guard let event = events.first else {
            throw SupabaseError.http(status: 404, message: "Invalid or unrecognized QR code.")
        }
        return event
    }

    func checkIn(to eventID: UUID) async throws -> UUID {
        let userID = try requireUserID()
        
        // Insert a new attendance log
        let body = try encode(AttendanceInsert(userID: userID, eventID: eventID))
        let data = try await send(Endpoint(
            path: "rest/v1/attendance_logs",
            method: .post,
            query: [URLQueryItem(name: "select", value: "id")],
            body: body,
            headers: ["Prefer": "return=representation"]
        ))
        
        struct InsertResponse: Decodable { let id: UUID }
        let response = try decode([InsertResponse].self, from: data)
        guard let logID = response.first?.id else {
            throw SupabaseError.invalidResponse
        }
        return logID
    }

    func uploadSignature(data image: Data, logID: UUID) async throws -> URL {
        let userID = try requireUserID()
        let filename = "\(userID.uuidString)/\(logID.uuidString).png"
        
        // Supabase Storage API endpoint for the 'signatures' bucket
        _ = try await send(Endpoint(
            path: "storage/v1/object/signatures/\(filename)",
            method: .post,
            body: image,
            headers: [
                "Content-Type": "image/png",
                "x-upsert": "true"
            ]
        ))
        
        // Construct the public URL
        let publicURLString = config.url.absoluteString + "/storage/v1/object/public/signatures/\(filename)"
        guard let url = URL(string: publicURLString) else {
            throw SupabaseError.invalidURL
        }
        return url
    }

    func verifyAttendance(logID: UUID, supervisorName: String, supervisorEmail: String, signatureURL: URL, hours: Double) async throws {
        let userID = try requireUserID()
        
        let update = AttendanceUpdate(
            status: "verified",
            supervisorName: supervisorName,
            supervisorEmail: supervisorEmail,
            signatureUrl: signatureURL.absoluteString,
            awardedHours: hours,
            checkOutTimestamp: PostgresDate.string(from: .now)
        )
        
        _ = try await send(Endpoint(
            path: "rest/v1/attendance_logs",
            method: .post, // Using PATCH via method override in headers or standard PATCH if supported. PostgREST usually uses PATCH.
            query: [
                URLQueryItem(name: "id", value: "eq.\(logID.uuidString.lowercased())"),
                URLQueryItem(name: "user_id", value: "eq.\(userID.uuidString.lowercased())") // Enforce RLS security check
            ],
            body: try encode(update),
            headers: [
                "Prefer": "return=minimal",
                "X-HTTP-Method-Override": "PATCH" // Workaround if plain PATCH enum isn't in Endpoint
            ]
        ))
    }
}

// MARK: - DTOs

private struct AttendanceInsert: Encodable {
    let userID: UUID
    let eventID: UUID
    enum CodingKeys: String, CodingKey {
        case userID = "user_id"
        case eventID = "event_id"
    }
}

private struct AttendanceUpdate: Encodable {
    let status: String
    let supervisorName: String
    let supervisorEmail: String
    let signatureUrl: String
    let awardedHours: Double
    let checkOutTimestamp: String
    
    enum CodingKeys: String, CodingKey {
        case status
        case supervisorName = "supervisor_name"
        case supervisorEmail = "supervisor_email"
        case signatureUrl = "supervisor_signature_url"
        case awardedHours = "awarded_hours"
        case checkOutTimestamp = "check_out_timestamp"
    }
}
