import SwiftUI

/// A single opportunity in the Discovery feed.
struct EventCardView: View {
    let event: VolunteerEvent
    let isPending: Bool
    let onToggleRSVP: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .firstTextBaseline) {
                Text(event.organizationName)
                    .font(.caption.weight(.semibold))
                    .textCase(.uppercase)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
                Spacer(minLength: 8)
                if event.isTrending {
                    TrendingBadge(peerCount: event.peerRSVPCount)
                }
            }

            Text(event.title)
                .font(.title3.weight(.bold))
                .lineLimit(2)
                .fixedSize(horizontal: false, vertical: true)

            HStack(spacing: 8) {
                InfoChip(systemImage: "calendar", text: event.eventDate.cardDateText)
                InfoChip(systemImage: "clock", text: event.eventDate.timeText)
            }

            Label(event.location, systemImage: "mappin.and.ellipse")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .lineLimit(1)

            Divider()

            HStack {
                if let percent = event.matchPercent {
                    MatchIndicator(percent: percent)
                } else if event.peerRSVPCount > 0 {
                    Label("\(event.peerRSVPCount) going", systemImage: "person.2.fill")
                        .font(.caption.weight(.medium))
                        .foregroundStyle(.secondary)
                }
                Spacer()
                RSVPButton(isRSVPd: event.isRSVPd, isPending: isPending, action: onToggleRSVP)
            }
        }
        .padding(16)
        .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 20, style: .continuous))
        .overlay {
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .strokeBorder(event.isRSVPd ? Color.green.opacity(0.5) : .clear, lineWidth: 1.5)
        }
        .shadow(color: .black.opacity(0.05), radius: 8, y: 4)
        .accessibilityElement(children: .contain)
    }
}

#Preview {
    ScrollView {
        VStack(spacing: 16) {
            EventCardView(
                event: {
                    var event = VolunteerEvent.samples[0]
                    event.peerRSVPCount = 7
                    return event
                }(),
                isPending: false,
                onToggleRSVP: {}
            )
            EventCardView(
                event: {
                    var event = VolunteerEvent.samples[1]
                    event.isRSVPd = true
                    return event
                }(),
                isPending: false,
                onToggleRSVP: {}
            )
        }
        .padding()
    }
    .background(Color(.systemGroupedBackground))
}
