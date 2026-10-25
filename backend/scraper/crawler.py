import asyncio
from .targets import scrape_all_targets

if __name__ == "__main__":
    results = asyncio.run(scrape_all_targets())
    print(f"Scraped {len(results)} events.")
