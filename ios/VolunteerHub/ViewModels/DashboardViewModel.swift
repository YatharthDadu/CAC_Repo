import Foundation
import Observation

@MainActor
@Observable
final class DashboardViewModel {
    private(set) var state: LoadState = .idle
    private(set) var profile: UserProfile?
    private(set) var verifiedHours: Double = 0
    private(set) var upcomingEvents: [VolunteerEvent] = []
    /// Non-blocking error shown when a refresh fails but cached data exists.
    var refreshError: String?

    private let service: any VolunteerDataProviding

    init(service: any VolunteerDataProviding) {
        self.service = service
    }

    // MARK: Derived values

    var goalHours: Double { max(profile?.goalHours ?? 30, 1) }
    var progress: Double { min(verifiedHours / goalHours, 1) }
    var remainingHours: Double { max(goalHours - verifiedHours, 0) }
    var percentComplete: Int { Int((progress * 100).rounded()) }
    var clubName: String { profile?.clubShortName ?? "NHS" }
    var greetingName: String { profile?.firstName ?? "there" }

    var nextDeadline: ClubDeadline? {
        profile?.clubDeadlines
            .filter { $0.dueDate >= Calendar.current.startOfDay(for: .now) }
            .min { $0.dueDate < $1.dueDate }
    }

    // MARK: Loading

    /// Safe to call repeatedly (on appear, pull-to-refresh). Only shows a
    /// full-screen spinner on first load.
    func load() async {
        if profile == nil { state = .loading }
        refreshError = nil
        let service = self.service

        do {
            async let profileTask = service.fetchCurrentProfile()
            async let hoursTask = service.fetchVerifiedHours()
            async let upcomingTask = service.fetchUpcomingRSVPEvents()
            let (profile, hours, upcoming) = try await (profileTask, hoursTask, upcomingTask)

            self.profile = profile
            self.verifiedHours = hours
            self.upcomingEvents = upcoming
            state = .loaded
        } catch is CancellationError {
            // View disappeared mid-load; keep whatever we had.
            if profile == nil { state = .idle }
        } catch {
            if profile == nil {
                state = .failed(error.localizedDescription)
            } else {
                refreshError = error.localizedDescription
            }
        }
    }
}
