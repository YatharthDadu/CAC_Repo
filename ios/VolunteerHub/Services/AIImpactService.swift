import Foundation

/// Generates professional STAR-method resume bullets using an LLM.
/// Note: In a production app, you should not ship LLM API keys directly to the client.
/// This logic should ideally live in an Edge Function on Supabase. For this challenge,
/// it's built directly into the client.
actor AIImpactService {
    
    // Set this key in your Info.plist or inject securely
    private let apiKey: String
    
    init(apiKey: String) {
        self.apiKey = apiKey
    }
    
    /// Sends a prompt to the LLM to synthesize a STAR method bullet point.
    /// - Parameters:
    ///   - task: What the student did (e.g. "sorted cans")
    ///   - scale: Quantity or impact (e.g. "200 boxes")
    /// - Returns: A formatted string max 150 characters for Common App.
    func synthesizeImpact(task: String, scale: String) async throws -> String {
        // We will use the Gemini API (Google AI Studio) as an example here.
        // Endpoint: https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=...
        
        let prompt = """
        You are an expert resume writer. A high school student completed a volunteer shift.
        Action: "\(task)"
        Scale/Quantity: "\(scale)"
        
        Write exactly ONE bullet point using the STAR (Situation, Task, Action, Result) method. 
        Start with an active power verb. 
        It MUST be under 150 characters to fit the Common App Activity description limit.
        Do not include quotes around the output. Keep it professional.
        """
        
        let url = URL(string: "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=\(apiKey)")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let payload: [String: Any] = [
            "contents": [
                ["parts": [["text": prompt]]]
            ]
        ]
        
        request.httpBody = try JSONSerialization.data(withJSONObject: payload)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
        
        // Parse Gemini Response
        struct GeminiResponse: Decodable {
            struct Candidate: Decodable {
                struct Content: Decodable {
                    struct Part: Decodable {
                        let text: String
                    }
                    let parts: [Part]
                }
                let content: Content
            }
            let candidates: [Candidate]
        }
        
        let result = try JSONDecoder().decode(GeminiResponse.self, from: data)
        if let text = result.candidates.first?.content.parts.first?.text {
            // Ensure we strictly enforce the 150 char limit just in case the LLM hallucinates
            let cleanText = text.trimmingCharacters(in: .whitespacesAndNewlines)
            if cleanText.count > 150 {
                return String(cleanText.prefix(147)) + "..."
            }
            return cleanText
        }
        
        throw URLError(.cannotParseResponse)
    }
}

// MARK: - Mock for Preview
extension AIImpactService {
    static let mock = AIImpactService(apiKey: "mock")
}
