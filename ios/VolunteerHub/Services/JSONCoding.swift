import Foundation

/// JSON helpers tuned for PostgREST / Postgres output.
enum JSONCoding {
    /// Models declare explicit snake_case `CodingKeys`, so no key strategy is set.
    static func makeDecoder() -> JSONDecoder {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .custom { decoder in
            let container = try decoder.singleValueContainer()
            let raw = try container.decode(String.self)
            guard let date = PostgresDate.parse(raw) else {
                throw DecodingError.dataCorruptedError(
                    in: container,
                    debugDescription: "Unrecognized date format: \(raw)"
                )
            }
            return date
        }
        return decoder
    }

    static func makeEncoder() -> JSONEncoder {
        JSONEncoder()
    }
}

/// Parses the timestamp shapes Postgres can return:
/// `2026-10-15T14:00:00+00:00`, `2026-10-15T14:00:00.123456+00:00`, `2026-10-15`.
enum PostgresDate {
    static func parse(_ raw: String) -> Date? {
        let iso = ISO8601DateFormatter()
        iso.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        if let date = iso.date(from: raw) { return date }

        iso.formatOptions = [.withInternetDateTime]
        if let date = iso.date(from: raw) { return date }

        let fallback = DateFormatter()
        fallback.locale = Locale(identifier: "en_US_POSIX")
        fallback.timeZone = TimeZone(secondsFromGMT: 0)
        let formats = [
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXXXX",
            "yyyy-MM-dd HH:mm:ss.SSSSSSXXXXX",
            "yyyy-MM-dd HH:mm:ssXXXXX",
            "yyyy-MM-dd",
        ]
        for format in formats {
            fallback.dateFormat = format
            if let date = fallback.date(from: raw) { return date }
        }
        return nil
    }

    /// UTC ISO-8601 with a `Z` suffix (no `+`, so it is URL-query safe).
    static func string(from date: Date) -> String {
        ISO8601DateFormatter().string(from: date)
    }
}
