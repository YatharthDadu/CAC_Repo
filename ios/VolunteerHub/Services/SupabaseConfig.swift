import Foundation

/// Supabase project settings, read from Info.plist.
///
/// Add these keys to Info.plist (ideally via an untracked `Secrets.xcconfig`):
///   SUPABASE_URL      = https://<project-ref>.supabase.co
///   SUPABASE_ANON_KEY = <anon public key>
///
/// Never ship the service-role key in the app.
struct SupabaseConfig: Sendable {
    let url: URL
    let anonKey: String

    /// Returns `nil` when keys are missing, which puts the app in demo mode.
    static func fromBundle(_ bundle: Bundle = .main) -> SupabaseConfig? {
        guard
            let urlString = bundle.object(forInfoDictionaryKey: "SUPABASE_URL") as? String,
            let url = URL(string: urlString.trimmingCharacters(in: .whitespacesAndNewlines)),
            url.host != nil,
            let anonKey = bundle.object(forInfoDictionaryKey: "SUPABASE_ANON_KEY") as? String,
            !anonKey.isEmpty,
            !anonKey.hasPrefix("$(") // unresolved build setting
        else {
            return nil
        }
        return SupabaseConfig(url: url, anonKey: anonKey)
    }
}
