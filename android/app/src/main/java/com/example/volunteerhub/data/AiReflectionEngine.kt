package com.example.volunteerhub.data

import com.example.volunteerhub.model.Cause
import com.example.volunteerhub.model.ShiftRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class StarReflection(
    val commonAppBullet: String,
    val xelloParagraph: String,
    val pathwayFocus: String
)

/**
 * AI Impact Reflection Engine (STAR Method: Situation, Task, Action, Result).
 *
 * Designed for Texas / Collin County ISD high schoolers bridging verified service hours
 * directly into Xello, Naviance, Common App activity sections, and National Honor Society audits.
 *
 * Operates with deterministic offline reliability for competition judging,
 * with optional live Gemini 1.5 Flash generation.
 */
object AiReflectionEngine {

    fun getReflections(shift: ShiftRecord): List<StarReflection> {
        val title = shift.title
        val org = shift.organizationName
        val hours = shift.hours

        return when (shift.cause) {
            Cause.TECH -> listOf(
                StarReflection(
                    commonAppBullet = "Engineered Python & robotics modules, mentoring 15+ youth in hands-on STEM debugging at $org.",
                    xelloParagraph = "Faced with diverse student coding backgrounds at $org, I designed modular robotics challenges and guided peers through step-by-step algorithmic debugging. By breaking down complex programming logic into intuitive sprints, our cohort completed all autonomous robot tasks ahead of schedule. This leadership experience sharpened my technical problem-solving and reinforced my ambition to pursue Computer Science.",
                    pathwayFocus = "STEM & Algorithmic Problem Solving"
                ),
                StarReflection(
                    commonAppBullet = "Facilitated youth coding workshops, debugging Scratch & Python algorithms to accelerate digital literacy.",
                    xelloParagraph = "Tasked with helping young students navigate computational concepts, I led interactive coding workshops focusing on algorithmic thinking and clean code architecture. I coached students through iterative problem-solving and bug fixes, boosting project completion rates by 40%. This deepened my patience, instructional leadership, and technical communication skills.",
                    pathwayFocus = "Computer Science & Mentorship"
                ),
                StarReflection(
                    commonAppBullet = "Coordinated technical maker lab workflows, inspecting 3D printing jobs and assisting 20+ young makers.",
                    xelloParagraph = "Serving at $org, I oversaw makerspace operations and instructed students on 3D printer calibration and basic CAD design. When equipment bottlenecks arose, I triaged print queues and established safe workspace protocols, ensuring every student printed their prototype successfully. This hands-on role solidified my interest in mechanical engineering and community technology access.",
                    pathwayFocus = "Engineering & Digital Fabrication"
                )
            )

            Cause.HEALTH -> listOf(
                StarReflection(
                    commonAppBullet = "Assembled 250+ sanitized pediatric patient care kits under strict infection control guidelines at $org.",
                    xelloParagraph = "Confronting high clinical volume at $org, I partnered with nurse coordinators to assemble and inspect sterile patient care packages under rigorous infection control standards. I organized inventory delivery lines to ensure critical bedside supplies reached admitted families without delay, improving supply turnaround by 35%. This clinical exposure strengthened my patient empathy and validated my pre-med career pathway.",
                    pathwayFocus = "Clinical Readiness & Healthcare Systems"
                ),
                StarReflection(
                    commonAppBullet = "Triage patient comfort supplies and coordinated sanitization workflows, assisting 100+ clinical families.",
                    xelloParagraph = "Tasked with supporting front-line healthcare teams, I audited supply logs and prepared individualized comfort kits tailored for recovering patients. By collaborating closely with hospital staff and maintaining compassionate communication with visiting families, I helped create a calm, supportive environment during high-stress admissions. This service solidified my resolve to pursue pediatric healthcare.",
                    pathwayFocus = "Patient Advocacy & Medical Ethics"
                ),
                StarReflection(
                    commonAppBullet = "Organized regional health donation drives, cataloging medical supplies and ensuring safety protocol compliance.",
                    xelloParagraph = "To address critical regional healthcare needs, I managed the intake, cataloging, and sanitization of essential patient care items at $org. I implemented a streamlined verification checklist that minimized sorting errors and sped up emergency care distribution to vulnerable patients. This reinforced my understanding of healthcare logistics and health equity.",
                    pathwayFocus = "Public Health & Hospital Operations"
                )
            )

            Cause.NATURE -> listOf(
                StarReflection(
                    commonAppBullet = "Restored 2.5 miles of native nature trails and planted 40+ indigenous saplings to counteract soil erosion.",
                    xelloParagraph = "To combat soil degradation and trail erosion at $org, I teamed with park conservationists to install natural mulch barriers and plant native drought-resistant saplings. I applied environmental science principles to stabilize hillside runoff zones and guided volunteer teams on local flora preservation, restoring critical trail corridors for community use. This field research deepened my commitment to ecological conservation.",
                    pathwayFocus = "Ecological Conservation & Fieldwork"
                ),
                StarReflection(
                    commonAppBullet = "Mulched conservation corridors and removed invasive species across Collin County public nature preserves.",
                    xelloParagraph = "Faced with aggressive invasive plant overgrowth threatening native wildlife habitats, I performed targeted clearing and soil mulching along regional wetlands. Working systematically in team rotations, we restored over two acres of native woodland buffer zones and cataloged plant health data for city environmental audits. This experience honed my environmental stewardship and outdoor leadership.",
                    pathwayFocus = "Environmental Science & Land Stewardship"
                ),
                StarReflection(
                    commonAppBullet = "Spearheaded urban park cleanup initiative, collecting 350+ lbs of recyclable waste to protect local watersheds.",
                    xelloParagraph = "Confronting urban runoff pollution entering municipal creek tributaries, I organized volunteer cleanup sweeps across regional parklands. By categorizing collected plastics and debris for municipal environmental metrics, our team prevented toxic micro-waste from reaching local waterways. This initiative demonstrated the power of community-driven environmental protection.",
                    pathwayFocus = "Watershed Protection & Urban Ecology"
                )
            )

            Cause.LEARNING -> listOf(
                StarReflection(
                    commonAppBullet = "Tutored 12 elementary students in foundational literacy & math, raising comprehension scores by 25%.",
                    xelloParagraph = "Working with elementary students experiencing foundational literacy gaps at $org, I created structured phonics exercises and individualized reading comprehension games. I adapted lesson paces to match each student's learning style, creating a safe environment where reluctant readers blossomed into enthusiastic storytellers. This role enhanced my instructional design and communication skills.",
                    pathwayFocus = "Educational Equity & Youth Mentorship"
                ),
                StarReflection(
                    commonAppBullet = "Curated bilingual youth reading hours and managed circulation tasks, engaging 40+ young library patrons.",
                    xelloParagraph = "To foster community literacy, I organized weekly interactive read-aloud sessions and assisted young library visitors with book selection at $org. I encouraged active discussion questions and paired reluctant readers with captivating high-interest titles, resulting in enthusiastic student engagement and increased library checkouts. This strengthened my interpersonal empathy and public speaking.",
                    pathwayFocus = "Literacy Advocacy & Community Education"
                )
            )

            Cause.COMMUNITY, Cause.BUSINESS -> listOf(
                StarReflection(
                    commonAppBullet = "Triaged & packaged 1,200+ lbs of emergency food supplies, streamlining delivery for 150+ regional families.",
                    xelloParagraph = "Responding to regional hunger needs at $org, I operated sorting lines and barcode inventory scanners to package balanced nutritional bundles for distribution. By identifying logistical bottlenecks on the packaging line, our team accelerated assembly speeds by 25%, ensuring prompt weekend meal deliveries for over 150 local students and families. This project developed my operational logistics and civic leadership.",
                    pathwayFocus = "Supply Chain Logistics & Civic Impact"
                ),
                StarReflection(
                    commonAppBullet = "Optimized intake workflows and audited food pantry inventory, ensuring equitable emergency nutrition access.",
                    xelloParagraph = "Confronting urgent community meal shortages, I led warehouse intake sorting and verified freshness standards across thousands of perishable donations at $org. I collaborated with volunteer coordinators to organize pantry shelving for maximum packing efficiency, directly impacting local relief distribution. This service deepened my dedication to civic responsibility and community welfare.",
                    pathwayFocus = "Operations Management & Community Welfare"
                )
            )
        }
    }

    /** Returns reflection variant for index, wrapping around cleanly. */
    fun getVariant(shift: ShiftRecord, index: Int): StarReflection {
        val list = getReflections(shift)
        val safeIndex = if (index < 0) 0 else index % list.size
        return list[safeIndex]
    }

    /**
     * Optional live Gemini API query with graceful fallback to curated offline models.
     */
    suspend fun generateGeminiReflection(
        shift: ShiftRecord,
        apiKey: String?
    ): StarReflection? = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank()) return@withContext null
        try {
            val prompt = """
                You are an expert college admissions counselor and high school service-learning coordinator.
                Analyze this verified high school volunteer shift:
                Activity: ${shift.title}
                Organization: ${shift.organizationName}
                Category: ${shift.cause.name}
                Hours: ${shift.hours}
                
                Generate a JSON object with:
                1. "commonAppBullet": Active power verb start, quantified impact, strictly <= 145 characters.
                2. "xelloParagraph": 2-3 sentences applying the STAR method (Situation, Task, Action, Result) highlighting leadership and career pathway alignment.
                3. "pathwayFocus": Short 3-4 word phrase of the career skill focus.
                
                Output ONLY valid JSON:
                {"commonAppBullet": "...", "xelloParagraph": "...", "pathwayFocus": "..."}
            """.trimIndent()

            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.doOutput = true

            val requestBody = JSONObject()
                .put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(
                        JSONObject().put("text", prompt)
                    ))
                ))

            OutputStreamWriter(conn.outputStream).use { it.write(requestBody.toString()) }

            if (conn.responseCode == 200) {
                val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseStr)
                val text = json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cleanJson = text.substringAfter("{").substringBeforeLast("}")
                val parsed = JSONObject("{$cleanJson}")
                val bullet = parsed.getString("commonAppBullet")
                val para = parsed.getString("xelloParagraph")
                val focus = parsed.optString("pathwayFocus", "Leadership & Service Learning")
                StarReflection(
                    commonAppBullet = if (bullet.length > 150) bullet.substring(0, 147) + "..." else bullet,
                    xelloParagraph = para,
                    pathwayFocus = focus
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
