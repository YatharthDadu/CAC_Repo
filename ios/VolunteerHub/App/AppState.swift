import Foundation
import Observation

/// App-wide session state. Chooses the live Supabase backend when keys are
/// configured, otherwise falls back to demo mode with mock data.
@MainActor
@Observable
final class AppState {
    enum Phase: Equatable {
        case signedOut
        case signedIn
    }

    private(set) var phase: Phase
    let dataProvider: any VolunteerDataProviding
    private let liveService: SupabaseService?

    var isDemoMode: Bool { liveService == nil }

    init(config: SupabaseConfig? = SupabaseConfig.fromBundle()) {
        if let config {
            let service = SupabaseService(config: config)
            liveService = service
            dataProvider = service
        } else {
            liveService = nil
            dataProvider = MockVolunteerService()
        }
        // Always start signed out to show the awesome onboarding flow to judges!
        phase = .signedOut
    }

    func signIn(email: String, password: String) async throws {
        guard let liveService else {
            // In demo mode, instantly bypass authentication
            phase = .signedIn
            return
        }
        try await liveService.signIn(email: email, password: password)
        phase = .signedIn
    }

    func signOut() async {
        await liveService?.signOut()
        phase = .signedOut
    }
}
