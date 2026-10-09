import Foundation

struct AttendanceLog: Identifiable, Codable {
    let id: UUID
    let eventId: UUID
    let userId: UUID
    let checkInTime: Date?
    let checkOutTime: Date?
    let awardedHours: Double
    let supervisorName: String?
    let supervisorEmail: String?
    let supervisorPhone: String?
    let signatureUrl: String?
    let studentTaskNotes: String?
    let aiImpactBlurb: String?
    let status: String
    
    enum CodingKeys: String, CodingKey {
        case id
        case eventId = "event_id"
        case userId = "user_id"
        case checkInTime = "check_in_time"
        case checkOutTime = "check_out_time"
        case awardedHours = "awarded_hours"
        case supervisorName = "supervisor_name"
        case supervisorEmail = "supervisor_email"
        case supervisorPhone = "supervisor_phone"
        case signatureUrl = "signature_url"
        case studentTaskNotes = "student_task_notes"
        case aiImpactBlurb = "ai_impact_blurb"
        case status
    }
}
