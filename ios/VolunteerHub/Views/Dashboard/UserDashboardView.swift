import SwiftUI

struct UserDashboardView: View {
    @State private var viewModel: DashboardViewModel
    @State private var isGeneratingPDF = false
    @State private var pdfURL: URLContainer?

    struct URLContainer: Identifiable {
        let id = UUID()
        let url: URL
    }

    init(service: any VolunteerDataProviding) {
        _viewModel = State(initialValue: DashboardViewModel(service: service))
    }

    var body: some View {
        NavigationStack {
            content
                .background(Color(.systemGroupedBackground))
                .navigationTitle("Dashboard")
                .toolbar {
                    ToolbarItem(placement: .primaryAction) {
                        Button(action: {
                            Task { await generatePDF() }
                        }) {
                            if isGeneratingPDF {
                                ProgressView()
                            } else {
                                Image(systemName: "square.and.arrow.up.document")
                            }
                        }
                    }
                }
                .sheet(item: $pdfURL) { urlContainer in
                    ShareSheet(activityItems: [urlContainer.url])
                }
                // Reload every time the tab appears so RSVPs made in
                // Discover show up here immediately.
                .task { await viewModel.load() }
        }
    }

    @ViewBuilder
    private var content: some View {
        switch viewModel.state {
        case .idle, .loading: // `.loading` is only set when there is no cached data
            ProgressView("Loading your hours…")
                .frame(maxWidth: .infinity, maxHeight: .infinity)

        case let .failed(message) where viewModel.profile == nil:
            ContentUnavailableView {
                Label("Couldn't Load Dashboard", systemImage: "wifi.exclamationmark")
            } description: {
                Text(message)
            } actions: {
                Button("Try Again") {
                    Task { await viewModel.load() }
                }
                .buttonStyle(.borderedProminent)
            }

        default:
            loadedContent
        }
    }

    private var loadedContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text("Hi, \(viewModel.greetingName)")
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(.secondary)

                if let refreshError = viewModel.refreshError {
                    Label(refreshError, systemImage: "exclamationmark.triangle.fill")
                        .font(.footnote)
                        .foregroundStyle(.orange)
                }

                progressCard

                if let deadline = viewModel.nextDeadline {
                    DeadlineBanner(deadline: deadline)
                }

                upcomingSection
            }
            .padding()
        }
        .refreshable { await viewModel.load() }
    }

    private var progressCard: some View {
        VStack(spacing: 20) {
            ProgressRingView(
                completedHours: viewModel.verifiedHours,
                goalHours: viewModel.goalHours,
                clubName: viewModel.clubName
            )
            .frame(width: 230, height: 230)
            .frame(maxWidth: .infinity)

            HStack(spacing: 12) {
                StatTile(
                    value: viewModel.remainingHours.hoursText,
                    label: "Hours Left",
                    systemImage: "hourglass"
                )
                StatTile(
                    value: "\(viewModel.upcomingEvents.count)",
                    label: "Upcoming",
                    systemImage: "calendar",
                    tint: .orange
                )
                StatTile(
                    value: "\(viewModel.percentComplete)%",
                    label: "Complete",
                    systemImage: "chart.pie.fill",
                    tint: .teal
                )
            }
        }
        .padding(20)
        .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 24, style: .continuous))
    }

    @ViewBuilder
    private var upcomingSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Upcoming RSVPs")
                .font(.title3.weight(.bold))

            if viewModel.upcomingEvents.isEmpty {
                ContentUnavailableView(
                    "No Upcoming Events",
                    systemImage: "calendar.badge.plus",
                    description: Text("RSVP to an opportunity in Discover and it will show up here.")
                )
                .frame(maxWidth: .infinity)
                .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            } else {
                ForEach(viewModel.upcomingEvents) { event in
                    UpcomingEventRow(event: event)
                }
            }
        }
    }

    private func generatePDF() async {
        isGeneratingPDF = true
        defer { isGeneratingPDF = false }
        
        // Use realistic dummy data or actual log histories here.
        // For demonstration, map the upcoming events to mock verified logs.
        let events = viewModel.upcomingEvents
        let profile = viewModel.profile ?? UserProfile(id: UUID(), fullName: viewModel.greetingName, schoolName: "High School", district: "TX-04", intendedMajor: "CS", goalHours: 30)
        
        let transcriptEvents: [(VolunteerEvent, AttendanceLog)] = events.map { event in
            (event, AttendanceLog(id: UUID(), eventId: event.id, userId: profile.id, checkInTime: Date(), checkOutTime: Date(), awardedHours: event.estimatedHours, supervisorName: "Coordinator", supervisorEmail: nil, supervisorPhone: nil, signatureUrl: nil, studentTaskNotes: nil, aiImpactBlurb: nil, status: "verified"))
        }
        
        let data = PDFTranscriptGenerator.TranscriptData(
            studentName: profile.fullName,
            schoolName: profile.schoolName ?? "Unknown School",
            totalHours: viewModel.verifiedHours,
            events: transcriptEvents
        )
        
        let generator = PDFTranscriptGenerator()
        if let url = try? await generator.generate(data: data) {
            pdfURL = URLContainer(url: url)
        }
    }
}

/// "NHS · Fall semester hours due Dec 1 — in 40 days"
private struct DeadlineBanner: View {
    let deadline: ClubDeadline

    private var daysRemaining: Int {
        let start = Calendar.current.startOfDay(for: .now)
        let end = Calendar.current.startOfDay(for: deadline.dueDate)
        return max(Calendar.current.dateComponents([.day], from: start, to: end).day ?? 0, 0)
    }

    private var isUrgent: Bool { daysRemaining <= 14 }

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "calendar.badge.clock")
                .font(.title2)
                .foregroundStyle(isUrgent ? Color.red : Color.accentColor)
            VStack(alignment: .leading, spacing: 2) {
                Text("\(deadline.club) · \(deadline.label)")
                    .font(.subheadline.weight(.semibold))
                Text("Due \(deadline.dueDate.formatted(.dateTime.month(.abbreviated).day())) — \(daysRemaining == 0 ? "today" : "in \(daysRemaining) days")")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer(minLength: 0)
        }
        .padding(14)
        .background(
            (isUrgent ? Color.red : Color.accentColor).opacity(0.1),
            in: RoundedRectangle(cornerRadius: 16, style: .continuous)
        )
        .accessibilityElement(children: .combine)
    }
}

#Preview {
    UserDashboardView(service: MockVolunteerService())
}
