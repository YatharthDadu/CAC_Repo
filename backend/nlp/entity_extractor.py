import re

class EntityExtractor:
    def __init__(self):
        self.nlp = None
        try:
            import spacy
            try:
                self.nlp = spacy.load("en_core_web_sm")
            except Exception:
                print("spaCy model en_core_web_sm not downloaded; using regex/heuristic extractor.")
        except ImportError:
            print("spaCy not installed; using regex/heuristic extractor.")

    def extract_entities(self, text):
        """
        Extracts DATE, TIME, GPE (Location), and ORG from the text.
        """
        if self.nlp:
            try:
                doc = self.nlp(text)
                entities = {
                    "dates": [],
                    "times": [],
                    "locations": [],
                    "organizations": []
                }
                for ent in doc.ents:
                    if ent.label_ == "DATE":
                        entities["dates"].append(ent.text)
                    elif ent.label_ == "TIME":
                        entities["times"].append(ent.text)
                    elif ent.label_ == "GPE":
                        entities["locations"].append(ent.text)
                    elif ent.label_ == "ORG":
                        entities["organizations"].append(ent.text)
                return {k: list(set(v)) for k, v in entities.items()}
            except Exception:
                pass
        
        # Fast regex & rule-based extraction fallback
        times = re.findall(r'\b\d{1,2}(?::\d{2})?\s*(?:AM|PM|am|pm)\b', text)
        dates = re.findall(r'\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{1,2}(?:st|nd|rd|th)?(?:,\s*\d{4})?\b', text, re.IGNORECASE)
        locations = []
        for loc in ["Frisco, TX", "McKinney, TX", "Plano, TX", "Allen, TX", "Richardson, TX", "Dallas, TX"]:
            if loc.lower().split(',')[0] in text.lower():
                locations.append(loc)
                
        return {
            "dates": list(set(dates)),
            "times": list(set(times)),
            "locations": list(set(locations)) or ["Frisco, TX"],
            "organizations": []
        }
