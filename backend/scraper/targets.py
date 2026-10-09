import asyncio
import logging
from bs4 import BeautifulSoup
from playwright.async_api import async_playwright, Page

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

async def extract_ntfb(page: Page) -> list[dict]:
    """1. North Texas Food Bank (NTFB) - https://ntfb.org/get-involved/volunteer/"""
    await page.goto("https://ntfb.org/get-involved/volunteer/", wait_until="domcontentloaded")
    html = await page.content()
    soup = BeautifulSoup(html, "html.parser")
    events = []
    
    # NTFB lists events in standard accordion or list containers.
    # Skeleton logic: finding volunteer shift descriptions
    for section in soup.find_all("div", class_="volunteer-opportunity"):
        title = section.find("h3").get_text(strip=True) if section.find("h3") else "NTFB Shift"
        desc = section.get_text(separator=" ", strip=True)
        events.append({
            "organization_name": "North Texas Food Bank",
            "title": title,
            "description": desc,
            "source_url": "https://ntfb.org/get-involved/volunteer/"
        })
    return events

async def extract_frisco_fastpacs(page: Page) -> list[dict]:
    """2. Frisco Fastpacs - https://www.friscofastpacs.org/volunteer-opportunities"""
    await page.goto("https://www.friscofastpacs.org/volunteer-opportunities", wait_until="domcontentloaded")
    html = await page.content()
    soup = BeautifulSoup(html, "html.parser")
    events = []
    
    # Look for Community Pack/Prep Nights
    for item in soup.find_all("div", class_="sqs-block-html"):
        text = item.get_text(separator=" ", strip=True)
        if "Pack Night" in text or "Prep" in text:
            events.append({
                "organization_name": "Frisco Fastpacs",
                "title": "Community Meal Packing",
                "description": text,
                "source_url": "https://www.friscofastpacs.org/volunteer-opportunities"
            })
    return events

async def extract_mckinney_parks(page: Page) -> list[dict]:
    """3. McKinney Parks Foundation - https://www.mckinneyparksfoundation.org/volunteer/"""
    await page.goto("https://www.mckinneyparksfoundation.org/volunteer/", wait_until="domcontentloaded")
    html = await page.content()
    soup = BeautifulSoup(html, "html.parser")
    events = []
    
    # Extract Trail Team environmental cleanups
    for post in soup.find_all("article"):
        title = post.find("h2").get_text(strip=True) if post.find("h2") else "Park Cleanup"
        desc = post.get_text(separator=" ", strip=True)
        if "Trail Team" in desc or "Cleanup" in title:
            events.append({
                "organization_name": "McKinney Parks Foundation",
                "title": title,
                "description": desc,
                "source_url": "https://www.mckinneyparksfoundation.org/volunteer/"
            })
    return events

async def extract_frisco_library(page: Page) -> list[dict]:
    """4. Frisco Public Library VolunTEEN - https://friscolibrary.com/volunteen/"""
    await page.goto("https://friscolibrary.com/volunteen/", wait_until="domcontentloaded")
    html = await page.content()
    soup = BeautifulSoup(html, "html.parser")
    events = []
    
    # Extract library service hours
    content = soup.find("main")
    if content:
        desc = content.get_text(separator=" ", strip=True)
        events.append({
            "organization_name": "Frisco Public Library",
            "title": "VolunTEEN Program",
            "description": desc,
            "source_url": "https://friscolibrary.com/volunteen/"
        })
    return events

async def extract_nbcf(page: Page) -> list[dict]:
    """5. National Breast Cancer Foundation (Frisco HQ) - https://volunteer.nbcf.org/"""
    await page.goto("https://volunteer.nbcf.org/", wait_until="domcontentloaded")
    html = await page.content()
    soup = BeautifulSoup(html, "html.parser")
    events = []
    
    # Look for HOPE Kit packing
    for opp in soup.find_all("div", class_="opportunity"):
        title = opp.find("h4").get_text(strip=True) if opp.find("h4") else "NBCF Volunteer"
        desc = opp.get_text(separator=" ", strip=True)
        events.append({
            "organization_name": "National Breast Cancer Foundation",
            "title": title,
            "description": desc,
            "source_url": "https://volunteer.nbcf.org/"
        })
    return events

async def extract_fmsc(page: Page) -> list[dict]:
    """6. Feed My Starving Children (Richardson) - https://www.fmsc.org/locations/richardson-tx"""
    await page.goto("https://www.fmsc.org/locations/richardson-tx", wait_until="domcontentloaded")
    html = await page.content()
    soup = BeautifulSoup(html, "html.parser")
    events = []
    
    for row in soup.find_all("tr", class_="shift-row"):
        tds = row.find_all("td")
        if len(tds) >= 3:
            time = tds[0].get_text(strip=True)
            desc = f"Food packing shift at FMSC Richardson. Shift time: {time}"
            events.append({
                "organization_name": "Feed My Starving Children",
                "title": "Food Packing Shift",
                "description": desc,
                "source_url": "https://www.fmsc.org/locations/richardson-tx"
            })
    return events

async def extract_idealist(page: Page) -> list[dict]:
    """7. Idealist - Keyword search injection"""
    # Construct URL with Location "North Texas/Dallas", Type "Volunteer", Keyword "Teen/Youth"
    url = "https://www.idealist.org/en/volunteer?q=Teen%20Youth&loc=Dallas%2C%20TX"
    await page.goto(url, wait_until="networkidle")
    html = await page.content()
    soup = BeautifulSoup(html, "html.parser")
    events = []
    
    for card in soup.find_all("div", attrs={"data-testid": "search-result"}):
        title = card.find("h3").get_text(strip=True) if card.find("h3") else "Idealist Volunteer"
        org = card.find("a", class_="org-link").get_text(strip=True) if card.find("a", class_="org-link") else "Idealist Organization"
        desc = card.get_text(separator=" ", strip=True)
        
        events.append({
            "organization_name": org,
            "title": title,
            "description": desc,
            "source_url": url
        })
    return events

async def scrape_all_targets():
    """Master orchestrator to scrape all targets sequentially."""
    all_events = []
    
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context(user_agent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        page = await context.new_page()
        
        targets = [
            extract_ntfb,
            extract_frisco_fastpacs,
            extract_mckinney_parks,
            extract_frisco_library,
            extract_nbcf,
            extract_fmsc,
            extract_idealist
        ]
        
        for target in targets:
            logger.info(f"Scraping with {target.__name__}...")
            try:
                events = await target(page)
                all_events.extend(events)
                logger.info(f"Found {len(events)} events from {target.__name__}.")
            except Exception as e:
                logger.error(f"Failed to scrape {target.__name__}: {e}")
                
        await browser.close()
        
    return all_events

if __name__ == "__main__":
    import json
    events = asyncio.run(scrape_all_targets())
    with open("raw_targets.json", "w") as f:
        json.dump(events, f, indent=4)
    print(f"Scraped {len(events)} total events.")
