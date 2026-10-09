import SwiftUI

struct DiscoveryFeedView: View {
    @State private var viewModel: DiscoveryFeedViewModel

    init(service: any VolunteerDataProviding) {
        _viewModel = State(initialValue: DiscoveryFeedViewModel(service: service))
    }

    var body: some View {
        NavigationStack {
            content
                .background(Color(.systemGroupedBackground))
                .navigationTitle("Discover")
                .task {
                    // First load only; afterwards the user pulls to refresh.
                    if viewModel.state == .idle {
                        await viewModel.load()
                    }
                }
                .alert(
                    "Something Went Wrong",
                    isPresented: Binding(
                        get: { viewModel.errorMessage != nil },
                        set: { if !$0 { viewModel.errorMessage = nil } }
                    )
                ) {
                    Button("OK", role: .cancel) {}
                } message: {
                    Text(viewModel.errorMessage ?? "")
                }
        }
    }

    @ViewBuilder
    private var content: some View {
        switch viewModel.state {
        case .idle, .loading: // `.loading` is only set when there is no cached data
            ProgressView("Finding opportunities for you…")
                .frame(maxWidth: .infinity, maxHeight: .infinity)

        case let .failed(message) where viewModel.events.isEmpty:
            ContentUnavailableView {
                Label("Couldn't Load Opportunities", systemImage: "wifi.exclamationmark")
            } description: {
                Text(message)
            } actions: {
                Button("Try Again") {
                    Task { await viewModel.load() }
                }
                .buttonStyle(.borderedProminent)
            }

        default:
            feed
        }
    }

    private var feed: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 16) {
                Picker("Filter", selection: $viewModel.selectedFilter) {
                    ForEach(DiscoveryFeedViewModel.Filter.allCases) { filter in
                        Text(filter.rawValue).tag(filter)
                    }
                }
                .pickerStyle(.segmented)

                if let pathway = viewModel.pathwayName, viewModel.selectedFilter == .forYou {
                    Label("Matched to your \(pathway) pathway", systemImage: "sparkles")
                        .font(.subheadline.weight(.medium))
                        .foregroundStyle(.secondary)
                }

                if viewModel.visibleEvents.isEmpty {
                    emptyState
                } else {
                    ForEach(viewModel.visibleEvents) { event in
                        EventCardView(
                            event: event,
                            isPending: viewModel.pendingRSVPs.contains(event.id)
                        ) {
                            Task { await viewModel.toggleRSVP(for: event.id) }
                        }
                    }
                }
            }
            .padding()
            .animation(.snappy, value: viewModel.selectedFilter)
        }
        .refreshable { await viewModel.load() }
    }

    @ViewBuilder
    private var emptyState: some View {
        switch viewModel.selectedFilter {
        case .forYou:
            ContentUnavailableView(
                "No Matches Yet",
                systemImage: "sparkle.magnifyingglass",
                description: Text("New opportunities are added daily. Pull down to refresh.")
            )
        case .trending:
            ContentUnavailableView(
                "Nothing Trending",
                systemImage: "flame",
                description: Text("Events with \(VolunteerEvent.trendingThreshold)+ classmates RSVP'd will appear here.")
            )
        case .thisWeek:
            ContentUnavailableView(
                "Nothing This Week",
                systemImage: "calendar",
                description: Text("Try the For You tab to see upcoming opportunities.")
            )
        }
    }
}

#Preview {
    DiscoveryFeedView(service: MockVolunteerService())
}
