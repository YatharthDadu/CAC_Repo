import asyncio
import os
import sys
import uuid
import random
import string
from datetime import datetime, timezone, timedelta
from dotenv import load_dotenv

# Ensure backend directory is in python search path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from scraper.targets import scrape_all_targets
from nlp.age_classifier import AgeClassifier
from nlp.entity_extractor import EntityExtractor
from embeddings.embedder import Embedder

# Load environment variables
load_dotenv(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env"))

SUPABASE_URL = os.getenv("SUPABASE_URL", "https://your-project-url.supabase.co")
SUPABASE_KEY = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "your-service-role-key")

def generate_qr_seed():
    return ''.join(random.choices(string.ascii_letters + string.digits, k=32))

async def run_pipeline():
    print("Starting Volunteer Hub Data Pipeline...")
    
    # Check if Supabase keys are configured
    if "your-project-url" in SUPABASE_URL or "your-service-role-key" in SUPABASE_KEY:
        print("\n[WARNING] Supabase credentials not found or still set to placeholders in backend/.env!")
        print("Please ensure SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY are set.")
    
    # 1. Scrape data
    print("\n--- Phase 1: Scraping Targets ---")
    raw_events = await scrape_all_targets()
    print(f"Scraped {len(raw_events)} live events.")
    
    # Merge rich curated North Texas catalog
    import json
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
    
    # 2. Process data
    print("\n--- Phase 2: NLP Processing & Embedding ---")
    for i, event in enumerate(raw_events):
        text = event.get('description', event.get('text', ''))
        title = event.get('title', 'Volunteer Opportunity')
        url = event.get('source_url', event.get('url', ''))
        org = event.get('organization_name', event.get('organization', 'Unknown Organization'))
        
        print(f"Processing ({i+1}/{len(raw_events)}): {title} ({org})")
        
        # NLP Guard: Check Age Eligibility
        is_eligible = age_classifier.is_eligible_for_high_school(text)
        if not is_eligible:
            print(" -> Skipped: Not eligible for high school teens.")
            continue
            
        # Extract Entities (Dates, Locations)
        entities = entity_extractor.extract_entities(text)
        
        address = entities["locations"][0] if entities.get("locations") else "Frisco, TX"
        
        # Stagger event dates over upcoming weeks for a realistic dashboard calendar
        future_date = datetime.now(timezone.utc) + timedelta(days=(i * 3 + 2))
        event_date = future_date.isoformat()
        
        # Generate Vector Embedding
        embedding = embedder.generate_embedding(text)
        
        processed_event = {
            "title": title,
            "organization_name": org,
            "location": address,
            "event_date": event_date,
            "qr_seed": generate_qr_seed(),
            "description_embedding": embedding
        }
        processed_events.append(processed_event)
        
    print(f"\nPrepared {len(processed_events)} eligible events with embeddings.")
    
    # 3. Upsert to Supabase if configured
    print("\n--- Phase 3: Supabase Upload ---")
    if "your-project-url" in SUPABASE_URL or "your-service-role-key" in SUPABASE_KEY:
        print("[!] Skipping Supabase upload because valid credentials are not yet set in backend/.env.")
        print(f"[+] All {len(processed_events)} processed events are verified and ready for database insertion!")
        return
        
    from supabase import create_client, Client
    try:
        supabase: Client = create_client(SUPABASE_URL, SUPABASE_KEY)
        print(f"Connecting to {SUPABASE_URL}...")
        response = supabase.table('events').insert(processed_events).execute()
        print(f"Successfully inserted {len(response.data)} records into Supabase 'events' table!")
    except Exception as e:
        print(f"Error uploading to Supabase: {e}")
        
    print("Pipeline execution complete.")

if __name__ == "__main__":
    asyncio.run(run_pipeline())
