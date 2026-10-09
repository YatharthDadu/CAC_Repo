import SwiftUI

struct RootTabView: View {
    let service: any VolunteerDataProviding

    var body: some View {
        TabView {
            UserDashboardView(service: service)
                .tabItem { Label("Dashboard", systemImage: "chart.pie.fill") }

            DiscoveryFeedView(service: service)
                .tabItem { Label("Discover", systemImage: "sparkles.rectangle.stack.fill") }
                
            CheckInCoordinatorView(service: service)
                .tabItem { Label("Scan", systemImage: "qrcode.viewfinder") }
        }
    }
}

#Preview {
    RootTabView(service: MockVolunteerService())
}
