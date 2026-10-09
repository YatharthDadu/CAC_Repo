import Foundation

/// A volunteer opportunity, decoded from either the `events` table or the
/// `match_events` pgvector RPC.
struct VolunteerEvent: Identifiable, Hashable, Sendable, Decodable {
    let id: UUID
    let title: String
    let organizationName: String
    let location: String
    let eventDate: Date
    /// Cosine similarity (0...1) from `match_events`. `nil` when the event
    /// came from a plain table query rather than vector search.
    let similarity: Double?

    // MARK: Client-side enrichment (not part of the JSON payload)

    /// RSVPs from *other* students (from `get_peer_rsvp_counts`).
    var peerRSVPCount: Int = 0
    /// Whether the signed-in student has RSVP'd.
    var isRSVPd: Bool = false

    enum CodingKeys: String, CodingKey {
        case id
        case title
        case location
        case similarity
        case organizationName = "organization_name"
        case eventDate = "event_date"
    }

    /// Minimum number of peer RSVPs before an event shows the trending badge.
    static let trendingThreshold = 3

    var isTrending: Bool { peerRSVPCount >= Self.trendingThreshold }

    /// Similarity as a whole-number percentage, clamped to 0...100.
    var matchPercent: Int? {
        guard let similarity else { return nil }
        return Int((min(max(similarity, 0), 1) * 100).rounded())
    }
}
