import Foundation
import Observation
import PencilKit

@MainActor
@Observable
final class VerificationViewModel {
    enum Phase: Equatable {
        case scanning
        case confirmingCheckIn(VolunteerEvent)
        case checkedIn(event: VolunteerEvent, logID: UUID, startTime: Date)
        case supervisorSignOff(event: VolunteerEvent, logID: UUID, hours: Double)
        case verified
        case error(String)
    }

    private(set) var phase: Phase = .scanning
    private let service: any VolunteerDataProviding
    
    // Check-in tracking
    var isProcessing: Bool = false
    
    // Supervisor form state
    var supervisorName: String = ""
    var supervisorEmail: String = ""
    var canvasView = PKCanvasView()

    init(service: any VolunteerDataProviding) {
        self.service = service
    }

    // MARK: - Step 1: Scan QR
    
    func handleScan(result: Result<String, Error>) async {
        guard case .scanning = phase else { return }
        
        switch result {
        case .success(let seed):
            isProcessing = true
            do {
                let event = try await service.validateQR(seed: seed)
                phase = .confirmingCheckIn(event)
            } catch {
                phase = .error(error.localizedDescription)
            }
            isProcessing = false
        case .failure(let error):
            phase = .error("Camera error: \(error.localizedDescription)")
        }
    }
    
    // MARK: - Step 2: Confirm Check-In
    
    func confirmCheckIn(event: VolunteerEvent) async {
        isProcessing = true
        do {
            let logID = try await service.checkIn(to: event.id)
            phase = .checkedIn(event: event, logID: logID, startTime: .now)
        } catch {
            phase = .error("Failed to check in: \(error.localizedDescription)")
        }
        isProcessing = false
    }
    
    // MARK: - Step 3: End Shift (Transition to Sign-off)
    
    func endShift(event: VolunteerEvent, logID: UUID, startTime: Date) {
        let elapsedHours = Date.now.timeIntervalSince(startTime) / 3600.0
        // Minimum of 0.1 hours to prevent 0.0 values on quick tests
        let roundedHours = max(0.1, (elapsedHours * 4).rounded() / 4.0) // Round to nearest 15 mins
        
        // For testing/demo purposes, if it's less than 1 hour, we can mock a default or keep the real time.
        // We'll pass the exact rounded hours.
        phase = .supervisorSignOff(event: event, logID: logID, hours: roundedHours)
    }
    
    // MARK: - Step 4: Submit Signature & Verify
    
    func submitVerification(event: VolunteerEvent, logID: UUID, hours: Double) async {
        guard !supervisorName.isEmpty else {
            phase = .error("Supervisor name is required.")
            return
        }
        
        guard let signatureData = canvasView.renderToPNG(), !canvasView.drawing.bounds.isEmpty else {
            phase = .error("Signature is required.")
            return
        }
        
        isProcessing = true
        do {
            let signatureURL = try await service.uploadSignature(data: signatureData, logID: logID)
            try await service.verifyAttendance(
                logID: logID,
                supervisorName: supervisorName,
                supervisorEmail: supervisorEmail,
                signatureURL: signatureURL,
                hours: hours
            )
            phase = .verified
        } catch {
            phase = .error("Verification failed: \(error.localizedDescription)")
        }
        isProcessing = false
    }
    
    func reset() {
        phase = .scanning
        isProcessing = false
        supervisorName = ""
        supervisorEmail = ""
        canvasView.clear()
    }
}
