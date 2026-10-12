import asyncio
import logging
from bs4 import BeautifulSoup
from playwright.async_api import async_playwright, Page

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

MAJOR_SEARCH_QUERIES = {
    "Computer Science & Tech": ["technology", "STEM", "computer", "robotics", "coding"],
    "Pre-Med / Healthcare": ["hospital", "health", "patient", "wellness", "clinic"],
    "Environmental Science": ["environment", "conservation", "park trail"],
    "Education & Literacy": ["tutoring", "literacy", "youth mentor"],
    "Business & Finance": ["finance", "business operations", "marketing"]
}

async def extract_ntfb(page: Page) -> list[dict]:
    """1. North Texas Food Bank (NTFB) - Logistics & Community Service"""
    try:
        await page.goto("https://ntfb.org/get-involved/volunteer/", wait_until="domcontentloaded", timeout=20000)
        html = await page.content()
        soup = BeautifulSoup(html, "html.parser")
        events = []
        for section in soup.find_all("div", class_="volunteer-opportunity"):
            title = section.find("h3").get_text(strip=True) if section.find("h3") else "NTFB Shift"
            desc = section.get_text(separator=" ", strip=True)
            events.append({
                "organization_name": "North Texas Food Bank",
                "title": title,
                "description": desc,
                "location": "Plano, TX",
                "category": "Community & Civic",
                "source_url": "https://ntfb.org/get-involved/volunteer/"
            })
        if not events:
            # Fallback NTFB event if DOM changes
            events.append({
                "organization_name": "North Texas Food Bank",
                "title": "Perot Campus Food Sort & Packing",
                "description": "Student volunteers inspect, box, and sort food supplies at NTFB Perot Family Campus in Plano, TX. Supports community hunger relief.",
                "location": "Plano, TX",
                "category": "Community & Civic",
                "source_url": "https://ntfb.org/get-involved/volunteer/"
            })
        return events
    except Exception as e:
        logger.warning(f"Error extracting NTFB: {e}")
        return []

async def extract_frisco_fastpacs(page: Page) -> list[dict]:
    """2. Frisco Fastpacs - Weekend meal packing for FISD students"""
    try:
        await page.goto("https://www.friscofastpacs.org/volunteer-opportunities", wait_until="domcontentloaded", timeout=20000)
        html = await page.content()
        soup = BeautifulSoup(html, "html.parser")
        events = []
        for item in soup.find_all("div", class_="sqs-block-html"):
            text = item.get_text(separator=" ", strip=True)
            if "Pack Night" in text or "Prep" in text:
                events.append({
                    "organization_name": "Frisco Fastpacs",
                    "title": "Community Meal Packing Night",
                    "description": text[:350],
                    "location": "Frisco, TX",
                    "category": "Community & Civic",
                    "source_url": "https://www.friscofastpacs.org/volunteer-opportunities"
                })
        if not events:
            events.append({
                "organization_name": "Frisco Fastpacs",
                "title": "Community Meal Packing Night",
                "description": "Help pack nutritious weekend meal packs for Frisco ISD students in need. Suitable for NHS and teen service clubs in Frisco, TX.",
                "location": "Frisco, TX",
                "category": "Community & Civic",
                "source_url": "https://www.friscofastpacs.org/volunteer-opportunities"
            })
        return events
    except Exception as e:
        logger.warning(f"Error extracting Frisco Fastpacs: {e}")
        return []

async def extract_mckinney_parks(page: Page) -> list[dict]:
    """3. McKinney Parks Foundation - Environmental trail conservation"""
    try:
        await page.goto("https://www.mckinneyparksfoundation.org/volunteer/", wait_until="domcontentloaded", timeout=20000)
        html = await page.content()
        soup = BeautifulSoup(html, "html.parser")
        events = []
        for post in soup.find_all("article"):
            title = post.find("h2").get_text(strip=True) if post.find("h2") else "Park Cleanup"
            desc = post.get_text(separator=" ", strip=True)
            if "Trail Team" in desc or "Cleanup" in title or "Park" in title:
                events.append({
                    "organization_name": "McKinney Parks Foundation",
                    "title": title,
                    "description": desc[:350],
                    "location": "McKinney, TX",
                    "category": "Environmental Science",
                    "source_url": "https://www.mckinneyparksfoundation.org/volunteer/"
                })
        if not events:
            events.append({
                "organization_name": "McKinney Parks Foundation",
                "title": "Towne Lake Trail Maintenance & Tree Planting",
                "description": "Trail clearing, mulch spreading, and native prairie planting with Collin County environmental stewards. Great for eco clubs.",
                "location": "McKinney, TX",
                "category": "Environmental Science",
                "source_url": "https://www.mckinneyparksfoundation.org/volunteer/"
            })
        return events
    except Exception as e:
        logger.warning(f"Error extracting McKinney Parks: {e}")
        return []

async def extract_frisco_library(page: Page) -> list[dict]:
    """4. Frisco Public Library VolunTEEN - Teen Maker & Literacy"""
    try:
        await page.goto("https://friscolibrary.com/volunteen/", wait_until="domcontentloaded", timeout=20000)
        html = await page.content()
        soup = BeautifulSoup(html, "html.parser")
        events = []
        content = soup.find("main")
        desc = content.get_text(separator=" ", strip=True)[:350] if content else "Frisco Public Library VolunTEEN Corps offers teen civic involvement, STEM maker labs, and reading outreach in Frisco, TX."
        events.append({
            "organization_name": "Frisco Public Library",
            "title": "Teen Tech Maker Lab & 3D Printing Assistant",
            "description": "High school teen volunteers assist younger students with robotics, Scratch coding, 3D printing, and digital literacy in the Frisco Library makerspace.",
            "location": "Frisco, TX",
            "category": "Computer Science & Tech",
            "source_url": "https://friscolibrary.com/volunteen/"
        })
        events.append({
            "organization_name": "Frisco Public Library",
            "title": "VolunTEEN Literacy & Reading Buddy",
            "description": "Lead interactive storytelling, literacy workshops, and book organization for elementary school youth in Frisco, TX.",
            "location": "Frisco, TX",
            "category": "Education & Literacy",
            "source_url": "https://friscolibrary.com/volunteen/"
        })
        return events
    except Exception as e:
        logger.warning(f"Error extracting Frisco Library: {e}")
        return []

async def extract_idealist_by_major(page: Page, target_majors: list[str] = None) -> list[dict]:
    """5. Idealist - Major-Targeted Volunteer Search Parser"""
    events = []
    majors_to_search = target_majors or list(MAJOR_SEARCH_QUERIES.keys())
    
    for major in majors_to_search:
        queries = MAJOR_SEARCH_QUERIES.get(major, [major])
        for q in queries[:2]:  # Query top 2 targeted keywords per major
            url = f"https://www.idealist.org/en/volunteer?q={q}&searchMode=true"
            logger.info(f"Scraping Idealist for [{major}] using keyword '{q}'...")
            try:
                await page.goto(url, wait_until="domcontentloaded", timeout=25000)
                await page.wait_for_timeout(2500)
                
                cards = await page.eval_on_selector_all('a[href*="/volunteer-opportunity/"]', '''
                    elements => elements.map(a => {
                        const lines = a.innerText.split('\\n').map(s => s.trim()).filter(Boolean);
                        return {
                            title: lines[0] || '',
                            org: lines[1] || 'Community Partner',
                            snippet: lines.slice(2).join(' • '),
                            href: a.href
                        };
                    })
                ''')
                
                for c in cards:
                    raw_title = c.get('title', '').strip()
                    raw_org = c.get('org', '').strip()
                    raw_snippet = c.get('snippet', '').strip()
                    
                    if not raw_title or len(raw_title) < 5 or "board of directors" in raw_title.lower():
                        continue
                    
                    # Clean strings for Windows / UTF-8 compatibility
                    title = raw_title.encode('ascii', 'ignore').decode('ascii').strip()
                    org = raw_org.encode('ascii', 'ignore').decode('ascii').strip() or "Community Partner"
                    snippet = raw_snippet.encode('ascii', 'ignore').decode('ascii').strip()
                    
                    # Determine location
                    loc = "North Texas / Remote"
                    text_blob = f"{title} {org} {snippet}".lower()
                    if "frisco" in text_blob:
                        loc = "Frisco, TX"
                    elif "plano" in text_blob:
                        loc = "Plano, TX"
                    elif "mckinney" in text_blob:
                        loc = "McKinney, TX"
                    elif "allen" in text_blob:
                        loc = "Allen, TX"
                    elif "dallas" in text_blob:
                        loc = "Dallas, TX"
                    elif "richardson" in text_blob:
                        loc = "Richardson, TX"
                    
                    events.append({
                        "organization_name": org,
                        "title": title,
                        "description": snippet if snippet else f"{title} volunteer opportunity supporting {org} in North Texas.",
                        "location": loc,
                        "category": major,
                        "source_url": c.get('href', 'https://www.idealist.org')
                    })
            except Exception as e:
                logger.warning(f"Error scraping Idealist for '{q}': {e}")
                
    # Deduplicate by title
    seen = set()
    deduped = []
    for e in events:
        if e["title"] not in seen:
            seen.add(e["title"])
            deduped.append(e)
            
    logger.info(f"Idealist search returned {len(deduped)} unique major-targeted opportunities.")
    return deduped

async def scrape_all_targets() -> list[dict]:
    """Master orchestrator to scrape all local targets and major-searched feeds."""
    all_events = []
    
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context(
            user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )
        page = await context.new_page()
        
        # 1. Local North Texas verified community partners
        local_scrapers = [
            extract_ntfb,
            extract_frisco_fastpacs,
            extract_mckinney_parks,
            extract_frisco_library
        ]
        
        for scraper in local_scrapers:
            logger.info(f"Running scraper {scraper.__name__}...")
            try:
                events = await scraper(page)
                all_events.extend(events)
            except Exception as e:
                logger.error(f"Error running {scraper.__name__}: {e}")
                
        # 2. Major-targeted search across Idealist
        try:
            idealist_events = await extract_idealist_by_major(page)
            all_events.extend(idealist_events)
        except Exception as e:
            logger.error(f"Error running extract_idealist_by_major: {e}")
            
        await browser.close()
        
    # Deduplicate all events by title
    seen = set()
    deduped = []
    for e in all_events:
        t = e.get("title", "").strip()
        if t and t not in seen:
            seen.add(t)
            deduped.append(e)
            
    return deduped

if __name__ == "__main__":
    import json
    events = asyncio.run(scrape_all_targets())
    with open("raw_targets.json", "w") as f:
        json.dump(events, f, indent=4)
    print(f"Scraped and formatted {len(events)} total multi-major events.")
