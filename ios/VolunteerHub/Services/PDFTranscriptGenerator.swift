import Foundation
import PDFKit
import UIKit

/// Generates a complete 8.5x11" PDF transcript using UIGraphicsPDFRenderer
@MainActor
final class PDFTranscriptGenerator {
    
    struct TranscriptData {
        let studentName: String
        let schoolName: String
        let totalHours: Double
        let events: [(event: VolunteerEvent, log: AttendanceLog)]
    }
    
    /// Generates the PDF document and returns its URL in the temporary directory
    func generate(data: TranscriptData) async throws -> URL {
        let format = UIGraphicsPDFRendererFormat()
        let metaData = [
            kCGPDFContextAuthor: "Volunteer Hub App",
            kCGPDFContextTitle: "\(data.studentName) - Volunteer Transcript"
        ]
        format.documentInfo = metaData as [String: Any]
        
        let pageWidth: CGFloat = 612.0 // 8.5 inches * 72 dpi
        let pageHeight: CGFloat = 792.0 // 11 inches * 72 dpi
        let pageRect = CGRect(x: 0, y: 0, width: pageWidth, height: pageHeight)
        
        let renderer = UIGraphicsPDFRenderer(bounds: pageRect, format: format)
        
        // 1. Pre-fetch all signature images concurrently
        var signatures: [UUID: UIImage] = [:]
        try await withThrowingTaskGroup(of: (UUID, UIImage?).self) { group in
            for item in data.events {
                guard let urlString = item.log.signatureUrl, let url = URL(string: urlString) else { continue }
                group.addTask {
                    do {
                        let (imgData, _) = try await URLSession.shared.data(from: url)
                        return (item.log.id, UIImage(data: imgData))
                    } catch {
                        return (item.log.id, nil)
                    }
                }
            }
            
            for try await (id, image) in group {
                if let image {
                    signatures[id] = image
                }
            }
        }
        
        // 2. Render PDF
        let pdfData = renderer.pdfData { context in
            context.beginPage()
            
            let cgContext = context.cgContext
            
            // Margins
            let margin: CGFloat = 36.0
            var currentY: CGFloat = margin
            
            // Helper for drawing text
            func drawText(_ text: String, font: UIFont, yOffset: CGFloat, isBold: Bool = false) -> CGFloat {
                let paragraphStyle = NSMutableParagraphStyle()
                paragraphStyle.alignment = .left
                
                let attributes: [NSAttributedString.Key: Any] = [
                    .font: font,
                    .paragraphStyle: paragraphStyle
                ]
                
                let attributedString = NSAttributedString(string: text, attributes: attributes)
                let textRect = CGRect(x: margin, y: yOffset, width: pageWidth - (margin * 2), height: font.lineHeight)
                attributedString.draw(in: textRect)
                
                return font.lineHeight
            }
            
            // --- HEADER ---
            currentY += drawText("OFFICIAL VOLUNTEER TRANSCRIPT", font: .systemFont(ofSize: 22, weight: .black), yOffset: currentY)
            currentY += 8
            currentY += drawText("Student: \(data.studentName)", font: .systemFont(ofSize: 16, weight: .bold), yOffset: currentY)
            currentY += drawText("Institution: \(data.schoolName)", font: .systemFont(ofSize: 14, weight: .regular), yOffset: currentY)
            currentY += drawText("Total Verified Hours: \(String(format: "%.1f", data.totalHours))", font: .systemFont(ofSize: 14, weight: .medium), yOffset: currentY)
            
            currentY += 24
            
            // Line separator
            cgContext.move(to: CGPoint(x: margin, y: currentY))
            cgContext.addLine(to: CGPoint(x: pageWidth - margin, y: currentY))
            cgContext.setStrokeColor(UIColor.black.cgColor)
            cgContext.setLineWidth(1)
            cgContext.strokePath()
            currentY += 24
            
            // --- EVENTS LIST ---
            let titleFont = UIFont.systemFont(ofSize: 12, weight: .bold)
            let bodyFont = UIFont.systemFont(ofSize: 10, weight: .regular)
            
            for item in data.events {
                // If we run out of room, create a new page
                if currentY > pageHeight - margin - 100 {
                    context.beginPage()
                    currentY = margin
                }
                
                let dateStr = item.event.eventDate.formatted(date: .numeric, time: .omitted)
                let headerLine = "\(dateStr) | \(item.event.organizationName) - \(item.event.title)"
                currentY += drawText(headerLine, font: titleFont, yOffset: currentY)
                
                let hoursStr = "Hours: \(String(format: "%.1f", item.log.awardedHours))"
                let supervisorStr = "Supervisor: \(item.log.supervisorName ?? "N/A")"
                currentY += drawText("\(hoursStr) • \(supervisorStr)", font: bodyFont, yOffset: currentY)
                
                if let impact = item.log.aiImpactBlurb ?? item.log.studentTaskNotes {
                    currentY += drawText("Summary: \(impact)", font: bodyFont, yOffset: currentY)
                }
                
                // Draw Signature if available
                if let signatureImage = signatures[item.log.id] {
                    let sigHeight: CGFloat = 30
                    let sigWidth: CGFloat = sigHeight * (signatureImage.size.width / signatureImage.size.height)
                    
                    let sigRect = CGRect(x: margin, y: currentY, width: sigWidth, height: sigHeight)
                    signatureImage.draw(in: sigRect)
                    currentY += sigHeight
                }
                
                currentY += 16 // Spacing between events
            }
            
            // --- FOOTER ---
            let footerText = "Generated securely via Volunteer Hub - Authenticity verifiable via TX-04 CAC"
            let footerFont = UIFont.systemFont(ofSize: 8, weight: .regular)
            let footerRect = CGRect(x: margin, y: pageHeight - margin - 10, width: pageWidth - (margin * 2), height: 20)
            
            let paragraphStyle = NSMutableParagraphStyle()
            paragraphStyle.alignment = .center
            let attributes: [NSAttributedString.Key: Any] = [
                .font: footerFont,
                .paragraphStyle: paragraphStyle,
                .foregroundColor: UIColor.gray
            ]
            NSAttributedString(string: footerText, attributes: attributes).draw(in: footerRect)
        }
        
        // Save to temp directory
        let tempURL = FileManager.default.temporaryDirectory.appendingPathComponent("Volunteer_Transcript_\(data.studentName.replacingOccurrences(of: " ", with: "_")).pdf")
        try pdfData.write(to: tempURL)
        
        return tempURL
    }
}
