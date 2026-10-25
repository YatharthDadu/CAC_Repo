import asyncio
import os
import sys
import uuid
import random
import string
import json
from dotenv import load_dotenv

# Ensure backend directory is in python search path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from scraper.targets import scrape_all_targets
from nlp.age_classifier import AgeClassifier
from nlp.entity_extractor import EntityExtractor
from embeddings.embedder import Embedder
from nlp.enrich import estimate_duration_hours, platform_from_url, resolve_event_date

# Load environment variables
load_dotenv(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env"))

SUPABASE_URL = os.getenv("SUPABASE_URL", "https://your-project-url.supabase.co")
SUPABASE_KEY = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "your-service-role-key")

def generate_qr_seed():
    return ''.join(random.choices(string.ascii_letters + string.digits, k=32))

async def run_pipeline():
    print("Starting Volunteer Hub Multi-Major Data Pipeline...")
    
    # 1. Scrape data
    print("\n--- Phase 1: Scraping Targets across Majors ---")
    raw_events = await scrape_all_targets()
    print(f"Scraped {len(raw_events)} live events.")
    
    # Merge rich curated North Texas catalog
    raw_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "raw_targets.json")
    if os.path.exists(raw_path):
        with open(raw_path, "r", encoding="utf-8") as f:
            curated_events = json.load(f)
        existing_titles = {e.get('title') for e in raw_events}
        for item in curated_events:
            if item.get('title') not in existing_titles:
                raw_events.append(item)
        print(f"Catalog total (live + North Texas targets): {len(raw_events)} events.")
    
    if not raw_events:
        print("No events found. Exiting.")
        return

    # Initialize NLP and Embedding models
    age_classifier = AgeClassifier()
    entity_extractor = EntityExtractor()
    embedder = Embedder()
    
    processed_events = []
    app_catalog = []
    
    # 2. Process data
    print("\n--- Phase 2: NLP Processing & Multi-Major Semantic Embedding ---")
    for i, event in enumerate(raw_events):
        text = event.get('description', event.get('text', ''))
        title = event.get('title', 'Volunteer Opportunity')
        url = event.get('source_url', event.get('url', ''))
        org = event.get('organization_name', event.get('organization', 'Unknown Organization'))
        cat = event.get('category', 'Community & Civic')
        loc = event.get('location', 'Frisco, TX')
        
        # NLP Guard: Check Age Eligibility
        is_eligible = age_classifier.is_eligible_for_high_school(text)
        if not is_eligible:
            continue
            
        # Extract Entities (Dates, Locations)
        entities = entity_extractor.extract_entities(text)
        address = loc if loc and loc != "North Texas / Remote" else (entities["locations"][0] if entities.get("locations") else "Frisco, TX")
        
        # Real date if the source gave one; otherwise a placeholder that the app flags as approximate
        event_date, is_estimated_date = resolve_event_date(event, f"{title}. {text}", i)
        duration_hours = estimate_duration_hours(text)
        source_platform = platform_from_url(url)
        
        # Context-rich embedding text for semantic similarity
        embed_context = f"Volunteer opportunity for high school students in {cat}. Title: {title}. Organization: {org}. Location: {address}. Description: {text}"
        embedding = embedder.generate_embedding(embed_context)
        
        qr = generate_qr_seed()
        
        processed_event = {
            "title": title,
            "organization_name": org,
            "location": address,
            "event_date": event_date,
            "qr_seed": qr,
            "description_embedding": embedding,
            "description": text,
            "category": cat,
            "source_url": url,
            "source_platform": source_platform,
            "duration_hours": duration_hours,
            "is_estimated_date": is_estimated_date
        }
        processed_events.append(processed_event)
        
        app_catalog.append({
            "title": title,
            "organization_name": org,
            "location": address,
            "event_date": event_date,
            "category": cat,
            "description": text,
            "source_url": url,
            "source_platform": source_platform,
            "duration_hours": duration_hours,
            "is_estimated_date": is_estimated_date,
            "qr_seed": qr
        })
        
    print(f"\nPrepared {len(processed_events)} eligible multi-major events with embeddings.")
    
    # Export full catalog for Android app offline synchronization
    catalog_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "processed_catalog.json")
    with open(catalog_path, "w", encoding="utf-8") as f:
        json.dump(app_catalog, f, indent=2)
    print(f"Exported processed catalog to {catalog_path}")

    # 3. Upsert to Supabase if configured
    print("\n--- Phase 3: Supabase Upload ---")
    if "your-project-url" in SUPABASE_URL or "your-service-role-key" in SUPABASE_KEY:
        print("[!] Skipping Supabase upload because valid credentials are not yet set in backend/.env.")
        return
        
    from supabase import create_client, Client
    try:
        supabase: Client = create_client(SUPABASE_URL, SUPABASE_KEY)
        print(f"Connecting to {SUPABASE_URL}...")
        
        # Clear previous records to prevent duplicates and ensure clean ranking
        try:
            supabase.table('events').delete().neq('title', '___NEVER_MATCH___').execute()
            print("Purged outdated records from Supabase 'events' table.")
        except Exception as e:
            print(f"Note on table purge: {e}")
            
        # Insert in chunks of 20
        chunk_size = 20
        inserted_count = 0
        for i in range(0, len(processed_events), chunk_size):
            chunk = processed_events[i:i + chunk_size]
            response = supabase.table('events').insert(chunk).execute()
            inserted_count += len(response.data)
            
        print(f"Successfully inserted {inserted_count} fresh multi-major records into Supabase 'events' table!")
    except Exception as e:
        print(f"Error uploading to Supabase: {e}")
        
    print("\nPipeline execution successfully finished!")

if __name__ == "__main__":
    asyncio.run(run_pipeline())
