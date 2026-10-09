import SwiftUI

/// Compact row for an upcoming RSVP'd event on the dashboard.
struct UpcomingEventRow: View {
    let event: VolunteerEvent

    var body: some View {
        HStack(spacing: 14) {
            VStack(spacing: 0) {
                Text(event.eventDate, format: .dateTime.month(.abbreviated))
                    .font(.caption2.weight(.bold))
                    .textCase(.uppercase)
                    .foregroundStyle(.red)
                Text(event.eventDate, format: .dateTime.day())
                    .font(.system(.title2, design: .rounded, weight: .bold))
            }
            .frame(width: 52, height: 56)
            .background(Color(.tertiarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 12, style: .continuous))

            VStack(alignment: .leading, spacing: 3) {
                Text(event.title)
                    .font(.subheadline.weight(.semibold))
                    .lineLimit(2)
                Text(event.organizationName)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
                Label("\(event.eventDate.timeText) · \(event.location)", systemImage: "mappin.and.ellipse")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            }

            Spacer(minLength: 0)

            NavigationLink(destination: XelloExportView(
                event: event,
                log: AttendanceLog(id: UUID(), eventId: event.id, userId: UUID(), checkInTime: Date(), checkOutTime: Date(), awardedHours: event.estimatedHours, supervisorName: nil, supervisorEmail: nil, supervisorPhone: nil, signatureUrl: nil, studentTaskNotes: nil, aiImpactBlurb: nil, status: "pending")
            )) {
                Image(systemName: "square.and.arrow.up")
                    .foregroundStyle(.blue)
            }
        }
        .padding(12)
        .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .accessibilityElement(children: .combine)
    }
}

#Preview {
    UpcomingEventRow(event: VolunteerEvent.samples[0])
        .padding()
        .background(Color(.systemGroupedBackground))
}
