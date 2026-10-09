import SwiftUI

/// Animated circular progress ring, e.g. "14/30 NHS Hours Completed".
struct ProgressRingView: View {
    let completedHours: Double
    let goalHours: Double
    let clubName: String
    var lineWidth: CGFloat = 20

    @State private var animatedProgress: Double = 0

    private var progress: Double {
        guard goalHours > 0 else { return 0 }
        return min(max(completedHours / goalHours, 0), 1)
    }

    private var isComplete: Bool { progress >= 1 }

    var body: some View {
        ZStack {
            Circle()
                .stroke(Color.accentColor.opacity(0.15), lineWidth: lineWidth)

            Circle()
                .trim(from: 0, to: animatedProgress)
                .stroke(
                    AngularGradient(
                        colors: isComplete ? [.green, .mint, .green] : [.teal, .blue, .indigo, .teal],
                        center: .center
                    ),
                    style: StrokeStyle(lineWidth: lineWidth, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))

            VStack(spacing: 4) {
                if isComplete {
                    Image(systemName: "checkmark.seal.fill")
                        .font(.title2)
                        .foregroundStyle(.green)
                        .symbolEffect(.bounce, value: isComplete)
                }
                Text("\(completedHours.hoursText)/\(goalHours.hoursText)")
                    .font(.system(size: 40, weight: .bold, design: .rounded))
                    .contentTransition(.numericText())
                    .minimumScaleFactor(0.6)
                    .lineLimit(1)
                Text("\(clubName) Hours")
                    .font(.subheadline.weight(.semibold))
                Text("Completed")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            .padding(lineWidth + 8)
        }
        .onAppear {
            withAnimation(.spring(duration: 1.1, bounce: 0.2)) {
                animatedProgress = progress
            }
        }
        .onChange(of: progress) { _, newValue in
            withAnimation(.spring(duration: 0.8)) {
                animatedProgress = newValue
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("\(clubName) service hours")
        .accessibilityValue("\(completedHours.hoursText) of \(goalHours.hoursText) hours completed")
    }
}

#Preview {
    VStack(spacing: 40) {
        ProgressRingView(completedHours: 14, goalHours: 30, clubName: "NHS")
            .frame(width: 220, height: 220)
        ProgressRingView(completedHours: 30, goalHours: 30, clubName: "Key Club")
            .frame(width: 160, height: 160)
    }
}
