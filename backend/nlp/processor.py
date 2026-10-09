import json
import spacy
from sentence_transformers import SentenceTransformer
from transformers import pipeline

def load_models():
    """Loads necessary AI/NLP models."""
    print("Loading models (this may take a moment)...")
    
    # 1. spaCy NER
    try:
        nlp = spacy.load("en_core_web_sm")
    except OSError:
        import subprocess
        print("Downloading spaCy model...")
        subprocess.run(["python", "-m", "spacy", "download", "en_core_web_sm"])
        nlp = spacy.load("en_core_web_sm")
        
    # 2. Sentence Transformers for Embeddings
    embedder = SentenceTransformer('all-MiniLM-L6-v2')
    
    # 3. Zero-shot classifier for 18+ filtering
    classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")
    
    return nlp, embedder, classifier

def is_18_plus(text: str, classifier) -> bool:
    """Uses zero-shot classification to detect if an event requires being 18+."""
    if not text:
        return False
        
    labels = ["requires 18+ adult, strictly adults only", "suitable for teenagers and high school students"]
    # Truncate to avoid exceeding max token length
    result = classifier(text[:1000], labels)
    
    top_label = result["labels"][0]
    score = result["scores"][0]
    
    # If the model is confident it's an 18+ event
    if top_label == labels[0] and score > 0.6:
        return True
    return False

def extract_entities(text: str, nlp) -> dict:
    """Extracts exactly dates and locations using spaCy NER."""
    doc = nlp(text)
    dates = [ent.text for ent in doc.ents if ent.label_ == "DATE" or ent.label_ == "TIME"]
    locations = [ent.text for ent in doc.ents if ent.label_ in ["GPE", "LOC", "FAC"]]
    
    return {
        "extracted_dates": list(set(dates)),
        "extracted_locations": list(set(locations))
    }

def process_events(raw_events_file: str, output_file: str):
    nlp, embedder, classifier = load_models()
    
    try:
        with open(raw_events_file, "r") as f:
            raw_events = json.load(f)
    except FileNotFoundError:
        print(f"Error: {raw_events_file} not found. Run scraper first.")
        return

    processed_events = []
    
    for event in raw_events:
        full_text = event.get("full_text", "")
        title = event.get("title", "Unknown Event")
        
        print(f"Processing: {title}")
        
        # 1. Filter out 18+ events
        if is_18_plus(full_text, classifier):
            print("  -> Flagged as 18+. Discarding.")
            continue
            
        # 2. Extract Entities
        entities = extract_entities(full_text, nlp)
        
        # Fallback to raw fields if NLP fails to find anything
        final_date = entities["extracted_dates"][0] if entities["extracted_dates"] else event.get("raw_date", "Date TBA")
        final_location = entities["extracted_locations"][0] if entities["extracted_locations"] else event.get("raw_location", "Location TBA")
        
        # 3. Generate Embeddings
        description = event.get("description", "")
        embedding = embedder.encode(description).tolist() if description else [0.0] * 384
        
        # 4. Structure Output
        clean_event = {
            "title": title,
            "date": final_date,
            "location": final_location,
            "description": description,
            "source_url": event.get("source_url", ""),
            "description_embedding": embedding,
            "metadata": {
                "extracted_dates": entities["extracted_dates"],
                "extracted_locations": entities["extracted_locations"]
            }
        }
        
        processed_events.append(clean_event)
        print("  -> Successfully processed.")
        
    # Output to JSON
    with open(output_file, "w") as f:
        json.dump(processed_events, f, indent=4)
        
    print(f"\nProcessing complete. {len(processed_events)} valid events saved to {output_file}.")

if __name__ == "__main__":
    process_events("raw_events.json", "processed_events_ready_for_db.json")
