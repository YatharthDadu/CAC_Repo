import Foundation

extension Double {
    /// "14" or "14.5" — at most one decimal place.
    var hoursText: String {
        formatted(.number.precision(.fractionLength(0...1)))
    }
}

extension Date {
    /// "Thu, Oct 15"
    var cardDateText: String {
        formatted(.dateTime.weekday(.abbreviated).month(.abbreviated).day())
    }

    /// "9:00 AM"
    var timeText: String {
        formatted(date: .omitted, time: .shortened)
    }
}
