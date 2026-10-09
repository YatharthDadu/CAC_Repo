import asyncio
import os
import uuid
import random
import string
from datetime import datetime, timezone
from dotenv import load_dotenv
from supabase import create_client, Client

from scraper.crawler import scrape_all_targets
from nlp.age_classifier import AgeClassifier
from nlp.entity_extractor import EntityExtractor
from embeddings.embedder import Embedder

# Load environment variables
load_dotenv()

SUPABASE_URL = os.getenv("SUPABASE_URL", "https://your-project-url.supabase.co")
SUPABASE_KEY = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "your-service-role-key")

def generate_qr_seed():
    return ''.join(random.choices(string.ascii_letters + string.digits, k=32))

async def run_pipeline():
    print("Starting Volunteer Hub Data Pipeline...")
    
    # Initialize Supabase client
    supabase: Client = create_client(SUPABASE_URL, SUPABASE_KEY)
    
    # 1. Scrape data
    print("\n--- Phase 1: Scraping ---")
    raw_events = await scrape_all_targets()
    print(f"Scraped {len(raw_events)} raw events.")
    
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
    for event in raw_events:
        text = event.get('text', '')
        title = event.get('title', 'Unknown Event')
        url = event.get('url', '')
        
        print(f"Processing: {title}")
        
        # NLP Guard: Check Age Eligibility
        is_eligible = age_classifier.is_eligible_for_high_school(text)
        if not is_eligible:
            print(" -> Skipped: Not eligible for high school teens.")
            continue
            
        # Extract Entities (Dates, Locations)
        entities = entity_extractor.extract_entities(text)
        
        # Fallback values if entities aren't found cleanly
        address = entities["locations"][0] if entities["locations"] else "Location TBA"
        # Combine extracted dates/times or use current time + 7 days as dummy
        event_date_str = f"{entities['dates'][0]} {entities['times'][0]}" if entities["dates"] and entities["times"] else None
        
        # In a real scenario, we'd parse event_date_str into a real datetime object.
        # Here we just use a placeholder if parsing fails.
        event_date = datetime.now(timezone.utc).isoformat()
        
        # Generate Vector Embedding
        embedding = embedder.generate_embedding(text)
        
        processed_event = {
            "title": title,
            "organization_name": event.get('organization', 'Unknown'),
            "category": event.get('category', 'Human Services'),
            "description": text[:500] + '...', # truncated for DB
            "address": address,
            "event_date": event_date,
            "estimated_hours": 4.0, # Default or extracted
            "min_age": 14,
            "qr_seed": generate_qr_seed(),
            "source_url": url,
            "description_embedding": embedding
        }
        processed_events.append(processed_event)
        
    # 3. Upsert to Supabase
    print("\n--- Phase 3: Supabase Upsert ---")
    if not processed_events:
        print("No eligible events to upload.")
        return
        
    print(f"Uploading {len(processed_events)} events to database...")
    try:
        response = supabase.table('events').upsert(processed_events).execute()
        print(f"Successfully inserted {len(response.data)} records.")
    except Exception as e:
        print(f"Error upserting to Supabase: {e}")
        
    print("Pipeline execution complete.")

if __name__ == "__main__":
    asyncio.run(run_pipeline())
