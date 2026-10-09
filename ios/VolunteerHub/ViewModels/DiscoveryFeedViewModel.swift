import Foundation
import Observation

@MainActor
@Observable
final class DiscoveryFeedViewModel {
    enum Filter: String, CaseIterable, Identifiable {
        case forYou = "For You"
        case trending = "Trending"
        case thisWeek = "This Week"

        var id: String { rawValue }
    }

    private(set) var state: LoadState = .idle
    private(set) var events: [VolunteerEvent] = []
    private(set) var pendingRSVPs: Set<UUID> = []
    private(set) var pathwayName: String?
    var selectedFilter: Filter = .forYou
    /// Transient error (e.g. a failed RSVP) presented as an alert.
    var errorMessage: String?

    private let service: any VolunteerDataProviding
    private let recommendationCount = 20

    init(service: any VolunteerDataProviding) {
        self.service = service
    }

    var visibleEvents: [VolunteerEvent] {
        switch selectedFilter {
        case .forYou:
            return events // already ranked by similarity
        case .trending:
            return events
                .filter(\.isTrending)
                .sorted { $0.peerRSVPCount > $1.peerRSVPCount }
        case .thisWeek:
            let weekOut = Date.now.addingTimeInterval(7 * 86_400)
            return events
                .filter { $0.eventDate <= weekOut }
                .sorted { $0.eventDate < $1.eventDate }
        }
    }

    // MARK: Loading

    func load() async {
        if events.isEmpty { state = .loading }
        let service = self.service
        let count = recommendationCount

        do {
            let profile = try await service.fetchCurrentProfile()
            pathwayName = profile.intendedMajor

            async let recommendedTask = service.fetchRecommendedEvents(for: profile, limit: count)
            async let rsvpTask = service.fetchRSVPdEventIDs()
            let (recommended, rsvpdIDs) = try await (recommendedTask, rsvpTask)

            // Peer counts are a nice-to-have; never fail the feed over them.
            let peerCounts = (try? await service.fetchPeerRSVPCounts(for: recommended.map(\.id))) ?? [:]

            events = recommended.map { event in
                var event = event
                event.isRSVPd = rsvpdIDs.contains(event.id)
                event.peerRSVPCount = peerCounts[event.id] ?? 0
                return event
            }
            state = .loaded
        } catch is CancellationError {
            if events.isEmpty { state = .idle }
        } catch {
            if events.isEmpty {
                state = .failed(error.localizedDescription)
            } else {
                errorMessage = error.localizedDescription
            }
        }
    }

    // MARK: RSVP

    /// Optimistically flips the RSVP, then rolls back if the request fails.
    func toggleRSVP(for eventID: UUID) async {
        guard !pendingRSVPs.contains(eventID),
              let index = events.firstIndex(where: { $0.id == eventID })
        else { return }

        let newValue = !events[index].isRSVPd
        events[index].isRSVPd = newValue
        pendingRSVPs.insert(eventID)
        defer { pendingRSVPs.remove(eventID) }

        do {
            try await service.setRSVP(newValue, for: eventID)
        } catch {
            // Re-look-up the index: the list may have been refreshed meanwhile.
            if let current = events.firstIndex(where: { $0.id == eventID }) {
                events[current].isRSVPd = !newValue
            }
            if !(error is CancellationError) {
                errorMessage = newValue
                    ? "Couldn't save your RSVP. \(error.localizedDescription)"
                    : "Couldn't cancel your RSVP. \(error.localizedDescription)"
            }
        }
    }
}
