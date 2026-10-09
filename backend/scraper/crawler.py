import asyncio
from playwright.async_api import async_playwright
from bs4 import BeautifulSoup
from .targets import TARGET_URLS

async def fetch_page_content(url):
    """Fetches the raw text content of a page using Playwright."""
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        page = await browser.new_page()
        try:
            await page.goto(url, wait_until="networkidle")
            html = await page.content()
            soup = BeautifulSoup(html, "html.parser")
            # Extract main text
            text = soup.get_text(separator=' ', strip=True)
            title = soup.title.string if soup.title else ""
            return {"title": title, "text": text, "url": url}
        except Exception as e:
            print(f"Error scraping {url}: {e}")
            return None
        finally:
            await browser.close()

async def scrape_all_targets():
    """Scrapes all defined target URLs."""
    results = []
    for target in TARGET_URLS:
        print(f"Scraping {target['url']}...")
        data = await fetch_page_content(target['url'])
        if data:
            data['organization'] = target['organization']
            data['category'] = target['category']
            results.append(data)
    return results

if __name__ == "__main__":
    results = asyncio.run(scrape_all_targets())
    print(f"Scraped {len(results)} events.")
