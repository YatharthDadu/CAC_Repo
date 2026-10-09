import Foundation

/// The data contract ViewModels depend on. `SupabaseService` is the live
/// implementation; `MockVolunteerService` powers previews and demo mode.
protocol VolunteerDataProviding: Sendable {
    func fetchCurrentProfile() async throws -> UserProfile
    /// Top matches from the `match_events` pgvector RPC (falls back to
    /// chronological order if the profile has no interest embedding yet).
    func fetchRecommendedEvents(for profile: UserProfile, limit: Int) async throws -> [VolunteerEvent]
    func fetchVerifiedHours() async throws -> Double
    func fetchUpcomingRSVPEvents() async throws -> [VolunteerEvent]
    func fetchRSVPdEventIDs() async throws -> Set<UUID>
    func fetchPeerRSVPCounts(for eventIDs: [UUID]) async throws -> [UUID: Int]
    func setRSVP(_ isGoing: Bool, for eventID: UUID) async throws
    
    // MARK: - Verification Flow
    func validateQR(seed: String) async throws -> VolunteerEvent
    func checkIn(to eventID: UUID) async throws -> UUID
    func uploadSignature(data: Data, logID: UUID) async throws -> URL
    func verifyAttendance(logID: UUID, supervisorName: String, supervisorEmail: String, signatureURL: URL, hours: Double) async throws
}
