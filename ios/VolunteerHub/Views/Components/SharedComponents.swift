import SwiftUI

/// "🔥 Trending · 7 peers" capsule.
struct TrendingBadge: View {
    let peerCount: Int

    var body: some View {
        Label {
            Text("Trending · \(peerCount) \(peerCount == 1 ? "peer" : "peers")")
        } icon: {
            Image(systemName: "flame.fill")
                .symbolEffect(.pulse, options: .repeating)
        }
        .font(.caption.weight(.semibold))
        .foregroundStyle(.orange)
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(.orange.opacity(0.15), in: Capsule())
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Trending with \(peerCount) local students")
    }
}

/// Compact icon + text chip used for date/time metadata.
struct InfoChip: View {
    let systemImage: String
    let text: String

    var body: some View {
        Label(text, systemImage: systemImage)
            .font(.caption.weight(.medium))
            .foregroundStyle(.secondary)
            .padding(.horizontal, 8)
            .padding(.vertical, 5)
            .background(Color(.tertiarySystemFill), in: Capsule())
    }
}

/// "✨ 82% match" indicator from pgvector cosine similarity.
struct MatchIndicator: View {
    let percent: Int

    var body: some View {
        Label("\(percent)% match", systemImage: "sparkles")
            .font(.caption.weight(.semibold))
            .foregroundStyle(.tint)
            .accessibilityLabel("\(percent) percent match with your interests")
    }
}

/// RSVP / Going toggle with loading state and haptics.
struct RSVPButton: View {
    let isRSVPd: Bool
    let isPending: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                if isPending {
                    ProgressView()
                        .controlSize(.small)
                        .tint(.white)
                } else {
                    Image(systemName: isRSVPd ? "checkmark.circle.fill" : "plus.circle.fill")
                        .contentTransition(.symbolEffect(.replace))
                }
                Text(isRSVPd ? "Going" : "RSVP")
            }
            .font(.subheadline.weight(.semibold))
            .frame(minWidth: 84)
        }
        .buttonStyle(.borderedProminent)
        .buttonBorderShape(.capsule)
        .tint(isRSVPd ? Color.green : Color.accentColor)
        .disabled(isPending)
        .animation(.snappy, value: isRSVPd)
        .sensoryFeedback(.success, trigger: isRSVPd)
        .accessibilityLabel(isRSVPd ? "Going" : "RSVP")
        .accessibilityHint(isRSVPd ? "Double-tap to cancel your RSVP" : "Double-tap to reserve your spot")
    }
}

/// Small metric tile used on the dashboard.
struct StatTile: View {
    let value: String
    let label: String
    let systemImage: String
    var tint: Color = .accentColor

    var body: some View {
        VStack(spacing: 6) {
            Image(systemName: systemImage)
                .font(.title3)
                .foregroundStyle(tint)
            Text(value)
                .font(.system(.headline, design: .rounded))
                .contentTransition(.numericText())
            Text(label)
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(Color(.tertiarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
        .accessibilityElement(children: .combine)
    }
}

#Preview("Components") {
    VStack(spacing: 16) {
        TrendingBadge(peerCount: 7)
        HStack {
            InfoChip(systemImage: "calendar", text: "Thu, Oct 15")
            InfoChip(systemImage: "clock", text: "9:00 AM")
        }
        MatchIndicator(percent: 82)
        HStack {
            RSVPButton(isRSVPd: false, isPending: false) {}
            RSVPButton(isRSVPd: true, isPending: false) {}
            RSVPButton(isRSVPd: false, isPending: true) {}
        }
        HStack {
            StatTile(value: "16", label: "Remaining", systemImage: "hourglass")
            StatTile(value: "47%", label: "Complete", systemImage: "chart.pie.fill", tint: .teal)
        }
    }
    .padding()
}
