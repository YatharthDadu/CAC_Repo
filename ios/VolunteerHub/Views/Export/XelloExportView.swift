import SwiftUI

struct XelloExportView: View {
    let event: VolunteerEvent
    let log: AttendanceLog // Need a model for this
    
    // Derived or synthesized impact text
    @State private var synthesizedImpact: String = ""
    @State private var taskInput: String = ""
    @State private var scaleInput: String = ""
    @State private var isSynthesizing = false
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                
                // Header
                VStack(spacing: 8) {
                    Image(systemName: "doc.on.clipboard.fill")
                        .font(.system(size: 48))
                        .foregroundStyle(.blue)
                    Text("Xello / Common App Bridge")
                        .font(.title2.weight(.bold))
                    Text("Copy these fields directly into your school's Xello portal or Common App activities section.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                }
                .padding(.horizontal)
                
                // AI Synthesizer Section
                VStack(alignment: .leading, spacing: 16) {
                    Text("AI Impact Synthesizer")
                        .font(.headline)
                    
                    VStack(alignment: .leading, spacing: 8) {
                        Text("What did you do? (e.g. 'Sorted food')")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                        TextField("Task", text: $taskInput)
                            .textFieldStyle(.roundedBorder)
                        
                        Text("Scale/Impact (e.g. '200 boxes', '50 families')")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                        TextField("Scale", text: $scaleInput)
                            .textFieldStyle(.roundedBorder)
                        
                        Button(action: generateImpact) {
                            HStack {
                                if isSynthesizing {
                                    ProgressView().tint(.white)
                                } else {
                                    Text("Generate STAR Bullet (Max 150 chars)")
                                }
                            }
                            .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .disabled(taskInput.isEmpty || isSynthesizing)
                    }
                }
                .padding()
                .background(Color(.secondarySystemGroupedBackground))
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .padding(.horizontal)
                
                // Fields to Copy
                VStack(spacing: 1) {
                    CopyFieldRow(title: "Organization Name", value: event.organizationName)
                    CopyFieldRow(title: "Supervisor Name", value: log.supervisorName ?? "N/A")
                    CopyFieldRow(title: "Supervisor Email", value: log.supervisorEmail ?? "N/A")
                    CopyFieldRow(title: "Date", value: event.eventDate.formatted(date: .numeric, time: .omitted))
                    CopyFieldRow(title: "Total Hours", value: String(format: "%.1f", log.awardedHours))
                    
                    if !synthesizedImpact.isEmpty {
                        CopyFieldRow(title: "Reflection / Summary", value: synthesizedImpact)
                    } else if let notes = log.studentTaskNotes {
                        CopyFieldRow(title: "Reflection / Summary", value: notes)
                    }
                }
                .background(Color(.secondarySystemGroupedBackground))
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .padding(.horizontal)
            }
            .padding(.vertical)
        }
        .background(Color(.systemGroupedBackground))
        .navigationTitle("Export")
        .navigationBarTitleDisplayMode(.inline)
    }
    
    private func generateImpact() {
        isSynthesizing = true
        Task {
            do {
                // Instantiate the service. In a real app, inject this or pull from a secure Config.
                let service = AIImpactService(apiKey: "YOUR_GEMINI_API_KEY") 
                let result = try await service.synthesizeImpact(task: taskInput, scale: scaleInput)
                await MainActor.run {
                    self.synthesizedImpact = result
                    self.isSynthesizing = false
                }
            } catch {
                await MainActor.run {
                    self.synthesizedImpact = "Error generating impact. Please try manually."
                    self.isSynthesizing = false
                }
            }
        }
    }
}

struct CopyFieldRow: View {
    let title: String
    let value: String
    @State private var didCopy = false
    
    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text(value)
                    .font(.body)
            }
            Spacer()
            
            Button {
                UIPasteboard.general.string = value
                withAnimation {
                    didCopy = true
                }
                // Reset after 2 seconds
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    withAnimation {
                        didCopy = false
                    }
                }
            } label: {
                Image(systemName: didCopy ? "checkmark.circle.fill" : "doc.on.doc")
                    .font(.title3)
                    .foregroundStyle(didCopy ? .green : .accentColor)
                    .contentTransition(.symbolEffect(.replace))
            }
        }
        .padding()
        .background(Color(.secondarySystemGroupedBackground))
    }
}

#Preview {
    XelloExportView(
        event: VolunteerEvent.samples[0],
        log: AttendanceLog(
            id: UUID(),
            eventId: UUID(),
            userId: UUID(),
            checkInTime: Date(),
            checkOutTime: Date(),
            awardedHours: 4.0,
            supervisorName: "Jane Doe",
            supervisorEmail: "jane@northtexasfoodbank.org",
            supervisorPhone: nil,
            signatureUrl: nil,
            studentTaskNotes: "Sorted 200 boxes of food",
            aiImpactBlurb: nil,
            status: "verified"
        )
    )
}
