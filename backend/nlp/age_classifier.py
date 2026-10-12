from transformers import pipeline

class AgeClassifier:
    def __init__(self):
        self.classifier = None
        try:
            from transformers import pipeline
            print("Loading Zero-Shot Classification Pipeline...")
            self.classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")
        except Exception as e:
            print(f"Transformers pipeline unavailable or model download failed ({e}); using keyword-based age classifier.")
        self.candidate_labels = [
            "eligible for high school teens 14-18",
            "requires 18+ adult or background check"
        ]

    def is_eligible_for_high_school(self, text):
        """
        Classifies if the volunteering opportunity is suitable for high school students.
        """
        if self.classifier:
            try:
                truncated_text = text[:1000] 
                result = self.classifier(truncated_text, self.candidate_labels)
                top_label = result["labels"][0]
                score = result["scores"][0]
                return top_label == self.candidate_labels[0] and score > 0.5
            except Exception as e:
                print(f"Classification inference error ({e}); using rule-based check.")
        
        lower = text.lower()
        if "must be 18" in lower or "18 and older" in lower or "adults only" in lower or "background check required" in lower:
            return False
        return True
