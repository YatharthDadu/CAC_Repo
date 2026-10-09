import Foundation

/// In-memory backend for SwiftUI previews and demo mode (used automatically
/// when Supabase keys are missing from Info.plist).
actor MockVolunteerService: VolunteerDataProviding {
    private var events: [VolunteerEvent]
    private var rsvpdIDs: Set<UUID>
    private let peerCounts: [UUID: Int]
    private let profile: UserProfile
    private let verifiedHours: Double

    init(
        events: [VolunteerEvent] = VolunteerEvent.samples,
        profile: UserProfile = .sample,
        verifiedHours: Double = 14
    ) {
        self.events = events
        self.profile = profile
        self.verifiedHours = verifiedHours
        self.rsvpdIDs = Set(events.prefix(2).map(\.id))

        var counts: [UUID: Int] = [:]
        for (index, event) in events.enumerated() {
            counts[event.id] = [7, 1, 4, 0, 12, 2][index % 6]
        }
        self.peerCounts = counts
    }

    func fetchCurrentProfile() async throws -> UserProfile {
        try await simulateLatency()
        return profile
    }

    func fetchRecommendedEvents(for profile: UserProfile, limit: Int) async throws -> [VolunteerEvent] {
        try await simulateLatency()
        return Array(events.sorted { ($0.similarity ?? 0) > ($1.similarity ?? 0) }.prefix(limit))
    }

    func fetchVerifiedHours() async throws -> Double {
        try await simulateLatency()
        return verifiedHours
    }

    func fetchUpcomingRSVPEvents() async throws -> [VolunteerEvent] {
        try await simulateLatency()
        return events
            .filter { rsvpdIDs.contains($0.id) && $0.eventDate >= .now }
            .sorted { $0.eventDate < $1.eventDate }
            .map { event in
                var event = event
                event.isRSVPd = true
                return event
            }
    }

    func fetchRSVPdEventIDs() async throws -> Set<UUID> {
        rsvpdIDs
    }

    func fetchPeerRSVPCounts(for eventIDs: [UUID]) async throws -> [UUID: Int] {
        peerCounts.filter { eventIDs.contains($0.key) }
    }

    func setRSVP(_ isGoing: Bool, for eventID: UUID) async throws {
        try await Task.sleep(for: .milliseconds(450))
        if isGoing {
            rsvpdIDs.insert(eventID)
        } else {
            rsvpdIDs.remove(eventID)
        }
    }

    private func simulateLatency() async throws {
        try await Task.sleep(for: .milliseconds(300))
    }
}

// MARK: - Sample data

extension UserProfile {
    static let sample = UserProfile(
        id: UUID(),
        fullName: "Maya Patel",
        intendedMajor: "Pre-Med",
        targetClub: "National Honor Society",
        goalHours: 30,
        clubDeadlines: [
            ClubDeadline(
                club: "NHS",
                label: "Fall semester hours due",
                dueDate: Date.now.addingTimeInterval(40 * 86_400)
            ),
        ],
        interestEmbedding: nil
    )
}

extension VolunteerEvent {
    private static func daysFromNow(_ days: Double, hour: Double = 9) -> Date {
        Calendar.current.startOfDay(for: .now).addingTimeInterval(days * 86_400 + hour * 3_600)
    }

    static let samples: [VolunteerEvent] = [
        VolunteerEvent(
            id: UUID(), title: "Patient Comfort Kit Assembly",
            organizationName: "Medical City Frisco", location: "Frisco, TX",
            eventDate: daysFromNow(3), similarity: 0.82
        ),
        VolunteerEvent(
            id: UUID(), title: "Saturday Food Pantry Sort",
            organizationName: "Frisco Family Services", location: "Frisco, TX",
            eventDate: daysFromNow(5, hour: 8), similarity: 0.61
        ),
        VolunteerEvent(
            id: UUID(), title: "Blood Drive Check-In Volunteer",
            organizationName: "Carter BloodCare", location: "McKinney, TX",
            eventDate: daysFromNow(9, hour: 13), similarity: 0.77
        ),
        VolunteerEvent(
            id: UUID(), title: "Lake Ray Hubbard Shoreline Cleanup",
            organizationName: "Keep Rockwall Beautiful", location: "Rockwall, TX",
            eventDate: daysFromNow(12, hour: 7), similarity: 0.34
        ),
        VolunteerEvent(
            id: UUID(), title: "Senior Center Health Fair Greeter",
            organizationName: "Allen Senior Recreation Center", location: "Allen, TX",
            eventDate: daysFromNow(6, hour: 10), similarity: 0.71
        ),
        VolunteerEvent(
            id: UUID(), title: "Kids' STEM Night Table Helper",
            organizationName: "Prosper ISD Education Foundation", location: "Prosper, TX",
            eventDate: daysFromNow(15, hour: 18), similarity: 0.42
        ),
    ]
}
