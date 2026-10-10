import SwiftUI

struct CheckInCoordinatorView: View {
    @State private var viewModel: VerificationViewModel
    @State private var isCameraPresented = true
    
    init(service: any VolunteerDataProviding) {
        _viewModel = State(initialValue: VerificationViewModel(service: service))
    }
    
    var body: some View {
        NavigationStack {
            ZStack {
                Color(.systemGroupedBackground).ignoresSafeArea()
                
                switch viewModel.phase {
                case .scanning:
                    scanningView
                case .confirmingCheckIn(let event):
                    confirmCheckInView(event: event)
                case .checkedIn(let event, let logID, let startTime):
                    activeShiftView(event: event, logID: logID, startTime: startTime)
                case .supervisorSignOff(let event, let logID, let hours):
                    supervisorSignOffView(event: event, logID: logID, hours: hours)
                case .verified:
                    verifiedSuccessView
                case .error(let message):
                    errorView(message: message)
                }
            }
            .navigationTitle("Check In")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                if viewModel.phase != .scanning {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") {
                            viewModel.reset()
                        }
                        .disabled(viewModel.isProcessing)
                    }
                }
            }
        }
    }
    
    // MARK: - Scanning Phase
    private var scanningView: some View {
        VStack(spacing: 24) {
            Text("Scan Event QR Code")
                .font(.title2.weight(.bold))
            
            Text("Point your camera at the QR code provided by the event coordinator to check in.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            
            ZStack {
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.black.opacity(0.1))
                    .frame(width: 300, height: 300)
                
                QRScannerView(isScanning: $isCameraPresented) { result in
                    Task {
                        await viewModel.handleScan(result: result)
                    }
                }
                .frame(width: 300, height: 300)
                .clipShape(RoundedRectangle(cornerRadius: 16))
                
                if viewModel.isProcessing {
                    ProgressView()
                        .scaleEffect(1.5)
                        .tint(.white)
                }
            }
        }
    }
    
    // MARK: - Confirm Check-In Phase
    private func confirmCheckInView(event: VolunteerEvent) -> some View {
        VStack(spacing: 32) {
            Image(systemName: "mappin.and.ellipse")
                .font(.system(size: 64))
                .foregroundStyle(Color.accentColor)
            
            VStack(spacing: 8) {
                Text(event.title)
                    .font(.title2.weight(.bold))
                    .multilineTextAlignment(.center)
                Text(event.organizationName)
                    .font(.headline)
                    .foregroundStyle(.secondary)
            }
            
            Button {
                Task { await viewModel.confirmCheckIn(event: event) }
            } label: {
                HStack {
                    if viewModel.isProcessing {
                        ProgressView().tint(.white)
                    } else {
                        Text("Confirm Check-In")
                    }
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .disabled(viewModel.isProcessing)
            .padding(.horizontal, 32)
        }
    }
    
    // MARK: - Active Shift Phase
    private func activeShiftView(event: VolunteerEvent, logID: UUID, startTime: Date) -> some View {
        VStack(spacing: 40) {
            VStack(spacing: 12) {
                Text("You are checked in!")
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(.green)
                Text(event.title)
                    .font(.headline)
                Text("Started at \(startTime.formatted(date: .omitted, time: .shortened))")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
            
            // A mock timer/pulse could go here
            Image(systemName: "clock.badge.checkmark.fill")
                .font(.system(size: 80))
                .foregroundStyle(.green)
                .symbolEffect(.pulse, options: .repeating)
            
            Button(role: .destructive) {
                viewModel.endShift(event: event, logID: logID, startTime: startTime)
            } label: {
                Text("End Shift & Verify")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .padding(.horizontal, 32)
        }
    }
    
    // MARK: - Supervisor Sign-Off Phase
    private func supervisorSignOffView(event: VolunteerEvent, logID: UUID, hours: Double) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Supervisor Verification")
                        .font(.title2.weight(.bold))
                    Text("Please hand your device to the supervisor to authorize your completed hours.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                .padding(.horizontal)
                
                VStack(spacing: 0) {
                    HStack {
                        Text("Hours Completed:")
                        Spacer()
                        // Formatting the hours calculated from the elapsed time
                        Text("\(hours.formatted()) hrs")
                            .bold()
                    }
                    .padding()
                    Divider()
                    TextField("Supervisor Full Name", text: $viewModel.supervisorName)
                        .textContentType(.name)
                        .padding()
                    Divider()
                    TextField("Supervisor Email (Optional)", text: $viewModel.supervisorEmail)
                        .textContentType(.emailAddress)
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                        .padding()
                }
                .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 12))
                .padding(.horizontal)
                
                VStack(alignment: .leading, spacing: 8) {
                    Text("Supervisor Signature")
                        .font(.headline)
                        .padding(.horizontal)
                    
                    SignaturePadView(canvasView: $viewModel.canvasView)
                        .frame(height: 200)
                        .background(Color(.secondarySystemGroupedBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color(.systemGray4), lineWidth: 1)
                        )
                        .padding(.horizontal)
                    
                    Button("Clear Signature") {
                        viewModel.canvasView.clear()
                    }
                    .font(.footnote)
                    .foregroundStyle(.red)
                    .padding(.horizontal)
                }
                
                Button {
                    Task { await viewModel.submitVerification(event: event, logID: logID, hours: hours) }
                } label: {
                    HStack {
                        if viewModel.isProcessing {
                            ProgressView().tint(.white)
                        } else {
                            Text("Verify & Submit")
                        }
                    }
                    .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.large)
                .disabled(viewModel.isProcessing || viewModel.supervisorName.isEmpty)
                .padding()
            }
            .padding(.vertical)
        }
    }
    
    // MARK: - Success Phase
    private var verifiedSuccessView: some View {
        VStack(spacing: 24) {
            Image(systemName: "checkmark.seal.fill")
                .font(.system(size: 80))
                .foregroundStyle(.green)
            
            Text("Verification Complete!")
                .font(.title.weight(.bold))
            
            Text("Your hours have been officially logged and verified. They will now appear on your dashboard.")
                .font(.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            
            Button {
                viewModel.reset()
            } label: {
                Text("Done")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .padding(32)
        }
    }
    
    // MARK: - Error Phase
    private func errorView(message: String) -> some View {
        VStack(spacing: 24) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 64))
                .foregroundStyle(.red)
            
            Text("An error occurred")
                .font(.title2.weight(.bold))
            
            Text(message)
                .font(.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            
            Button("Try Again") {
                viewModel.reset()
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
        }
    }
}

#Preview {
    CheckInCoordinatorView(service: MockVolunteerService())
}
