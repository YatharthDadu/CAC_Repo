import SwiftUI

@main
struct VolunteerHubApp: App {
    @State private var appState = AppState()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(appState)
        }
    }
}

/// Switches between sign-in and the main tab interface.
struct RootView: View {
    @Environment(AppState.self) private var appState

    var body: some View {
        Group {
            switch appState.phase {
            case .signedOut:
                SignInView()
            case .signedIn:
                RootTabView(service: appState.dataProvider)
            }
        }
        .animation(.default, value: appState.phase)
    }
}
