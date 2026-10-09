import Foundation

/// A row from the `users` table.
struct UserProfile: Identifiable, Sendable, Decodable {
    let id: UUID
    let fullName: String
    let intendedMajor: String?
    let targetClub: String?
    let goalHours: Double?
    let clubDeadlines: [ClubDeadline]
    /// pgvector text form, e.g. `"[0.012,-0.034,...]"`. Passed back to the
    /// `match_events` RPC as-is, so we never need to parse 384 floats.
    let interestEmbedding: String?

    enum CodingKeys: String, CodingKey {
        case id
        case fullName = "full_name"
        case intendedMajor = "intended_major"
        case targetClub = "target_club"
        case goalHours = "goal_hours"
        case clubDeadlines = "club_deadlines"
        case interestEmbedding = "interest_embedding"
    }

    var firstName: String {
        fullName.split(separator: " ").first.map(String.init) ?? fullName
    }

    /// Short club label for compact UI, e.g. "NHS".
    var clubShortName: String {
        let club = targetClub ?? "National Honor Society"
        let known: [String: String] = [
            "National Honor Society": "NHS",
            "Business Professionals of America": "BPA",
            "Student Council": "StuCo",
        ]
        return known[club] ?? club
    }
}

// Declared in an extension so the synthesized memberwise init is preserved
// (used by the mock service and SwiftUI previews).
extension UserProfile {
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(UUID.self, forKey: .id)
        fullName = try container.decodeIfPresent(String.self, forKey: .fullName) ?? "Student"
        intendedMajor = try container.decodeIfPresent(String.self, forKey: .intendedMajor)
        targetClub = try container.decodeIfPresent(String.self, forKey: .targetClub)
        goalHours = try container.decodeIfPresent(Double.self, forKey: .goalHours)

        // `club_deadlines` is free-form JSONB; a malformed value should not
        // break the whole dashboard, so fall back to an empty list.
        clubDeadlines = (try? container.decodeIfPresent([ClubDeadline].self, forKey: .clubDeadlines)) ?? []

        // PostgREST serializes `vector` as a string. Accept a raw array too.
        if let text = try? container.decodeIfPresent(String.self, forKey: .interestEmbedding) {
            interestEmbedding = text
        } else if let values = try? container.decodeIfPresent([Double].self, forKey: .interestEmbedding) {
            interestEmbedding = "[" + values.map { String($0) }.joined(separator: ",") + "]"
        } else {
            interestEmbedding = nil
        }
    }
}

/// An item inside the `users.club_deadlines` JSONB array, e.g.
/// `{"club": "NHS", "label": "Fall hours due", "due_date": "2026-12-01"}`.
struct ClubDeadline: Identifiable, Hashable, Sendable, Decodable {
    let club: String
    let label: String
    let dueDate: Date

    var id: String { "\(club)-\(label)-\(dueDate.timeIntervalSince1970)" }

    enum CodingKeys: String, CodingKey {
        case club
        case label
        case dueDate = "due_date"
    }
}
