import Foundation

extension MockVolunteerService {
    
    // MARK: - Verification Flow
    
    func validateQR(seed: String) async throws -> VolunteerEvent {
        try await Task.sleep(for: .milliseconds(500))
        if seed == "demo-invalid" {
            throw NSError(domain: "MockError", code: 404, userInfo: [NSLocalizedDescriptionKey: "Invalid QR Code"])
        }
        return VolunteerEvent.samples[0]
    }
    
    func checkIn(to eventID: UUID) async throws -> UUID {
        try await Task.sleep(for: .milliseconds(500))
        return UUID()
    }
    
    func uploadSignature(data: Data, logID: UUID) async throws -> URL {
        try await Task.sleep(for: .milliseconds(800))
        return URL(string: "https://example.com/signature.png")!
    }
    
    func verifyAttendance(logID: UUID, supervisorName: String, supervisorEmail: String, signatureURL: URL, hours: Double) async throws {
        try await Task.sleep(for: .milliseconds(600))
        // Success
    }
}
