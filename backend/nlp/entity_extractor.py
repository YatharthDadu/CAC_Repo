import spacy

class EntityExtractor:
    def __init__(self):
        print("Loading spaCy NER model...")
        try:
            self.nlp = spacy.load("en_core_web_sm")
        except OSError:
            print("Downloading en_core_web_sm model...")
            import subprocess
            subprocess.run(["python", "-m", "spacy", "download", "en_core_web_sm"])
            self.nlp = spacy.load("en_core_web_sm")

    def extract_entities(self, text):
        """
        Extracts DATE, TIME, GPE (Location), and ORG from the text.
        """
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
                
        # Deduplicate
        return {k: list(set(v)) for k, v in entities.items()}
