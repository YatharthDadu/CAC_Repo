import SwiftUI

struct XelloExportView: View {
    let event: VolunteerEvent
    let log: AttendanceLog
    
    @State private var synthesizedImpact: String = "Collaborated with healthcare staff to assemble 250+ patient comfort kits, optimizing logistics under time constraints to directly support patient wellness and community care."
    @State private var taskInput: String = ""
    @State private var scaleInput: String = ""
    @State private var isSynthesizing = false
    @State private var showingPDFShare = false
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                
                // Activity Details Card
                VStack(alignment: .leading, spacing: 12) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(event.title)
                            .font(.headline)
                        Text(event.organizationName)
                            .font(.subheadline)
                        Text("\(event.eventDate.formatted(date: .abbreviated, time: .omitted)) ? \(String(format: "%.1f", log.awardedHours)) hours")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    }
                    
                    HStack(spacing: 12) {
                        HStack(spacing: 4) {
                            Text("VERIFIED")
                                .font(.caption.weight(.bold))
                            Image(systemName: "checkmark.circle.fill")
                                .font(.caption)
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.green)
                        .foregroundStyle(.white)
                        .clipShape(Capsule())
                        
                        Image(systemName: "signature")
                            .font(.title2)
                            .foregroundStyle(.black.opacity(0.7))
                            .frame(width: 80, height: 30)
                            .background(Color.black.opacity(0.05))
                        
                        VStack(alignment: .leading) {
                            Text(log.supervisorName ?? "Marta Alvarez")
                                .font(.caption.weight(.semibold))
                            Text("(Coordinator)")
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(.systemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .shadow(color: .black.opacity(0.05), radius: 5, y: 2)
                
                // Xello Bridge Section
                VStack(alignment: .leading, spacing: 12) {
                    Text("Xello Bridge (Fast Copy)")
                        .font(.title3.weight(.bold))
                    
                    VStack(spacing: 0) {
                        Text("Xello & Portals Copy Drawer")
                            .font(.subheadline.weight(.semibold))
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding()
                            .background(Color.blue.opacity(0.15))
                        
                        VStack(spacing: 0) {
                            FastCopyRow(title: "Organization", value: event.organizationName)
                            Divider().padding(.leading)
                            FastCopyRow(title: "Hours & Date", value: "\(String(format: "%.1f", log.awardedHours)) hours ? \(event.eventDate.formatted(date: .abbreviated, time: .omitted))")
                            Divider().padding(.leading)
                            FastCopyRow(title: "Supervisor Email", value: log.supervisorEmail ?? "m.alvarez@medicalcity.org")
                        }
                        .background(Color(.systemBackground))
                    }
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.blue.opacity(0.2), lineWidth: 1))
                }
                
                // AI Impact Section
                VStack(alignment: .leading, spacing: 12) {
                    Text("AI Impact Reflection (STAR Method)")
                        .font(.title3.weight(.bold))
                    
                    VStack(alignment: .leading, spacing: 12) {
                        Label("AI Reflection Generator", systemImage: "sparkles")
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(.blue)
                        
                        Text(synthesizedImpact)
                            .font(.footnote)
                            .padding()
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color.black.opacity(0.05))
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                        
                        HStack {
                            Button {
                                // Regenerate Action
                            } label: {
                                Label("Regenerate", systemImage: "sparkles")
                                    .font(.caption.weight(.medium))
                                    .foregroundStyle(.primary)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color.black.opacity(0.05))
                                    .clipShape(Capsule())
                            }
                            
                            Spacer()
                            
                            Button {
                                UIPasteboard.general.string = synthesizedImpact
                            } label: {
                                Text("Copy Paragraph")
                                    .font(.caption.weight(.medium))
                                    .foregroundStyle(.primary)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color.black.opacity(0.05))
                                    .clipShape(Capsule())
                            }
                        }
                    }
                    .padding()
                    .background(Color(.systemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .shadow(color: .black.opacity(0.05), radius: 5, y: 2)
                }
                
                // PDF Button
                Button {
                    showingPDFShare = true
                } label: {
                    HStack {
                        Image(systemName: "doc.text.fill")
                        Text("Generate Verified Resume (PDF)")
                            .font(.headline)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .foregroundStyle(.white)
                    .background(Color.blue)
                    .clipShape(Capsule())
                }
                .padding(.top, 8)
                .sheet(isPresented: $showingPDFShare) {
                    NavigationStack {
                        VStack {
                            Image(systemName: "checkmark.seal.fill")
                                .font(.system(size: 64))
                                .foregroundStyle(.green)
                                .padding()
                            Text("PDF Transcript Generated!")
                                .font(.title2.weight(.bold))
                            Text("You can now print this document or attach it to the CAC portal.")
                                .foregroundStyle(.secondary)
                                .multilineTextAlignment(.center)
                                .padding()
                        }
                        .navigationTitle("Generated PDF")
                        .navigationBarTitleDisplayMode(.inline)
                    }
                }
            }
            .padding()
        }
        .background(Color(.systemGroupedBackground))
        .navigationTitle("Activity Details")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct FastCopyRow: View {
    let title: String
    let value: String
    @State private var didCopy = false
    
    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text(value)
                    .font(.subheadline)
            }
            Spacer()
            
            Button {
                UIPasteboard.general.string = value
                withAnimation { didCopy = true }
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    withAnimation { didCopy = false }
                }
            } label: {
                Image(systemName: didCopy ? "checkmark" : "doc.on.doc")
                    .font(.body.weight(.bold))
                    .foregroundStyle(.white)
                    .padding(8)
                    .background(didCopy ? Color.green : Color.blue)
                    .clipShape(Circle())
            }
        }
        .padding()
    }
}
