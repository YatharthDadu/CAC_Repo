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
        lower = text.lower()
        
        # Explicit disqualifiers (18+, 21+, background checks for adults)
        if any(w in lower for w in ["must be 18", "18 and older", "18+", "adults only", "at least 18", "at least 21", "21 and older"]):
            return False
            
        # Clear positive signals for teen / youth / high school programs
        positive_keywords = [
            "teen", "youth", "volunteen", "high school", "student", "grades 9", "grade 9",
            "grade 10", "grade 11", "grade 12", "all ages", "family", "park cleanup",
            "food pack", "sorting", "cleanup"
        ]
        # NOTE: the bare word "volunteer" is deliberately NOT a positive signal, it appears in every
        # listing, including adult-only ones. Ambiguous text goes to the zero-shot model below.
        if any(w in lower for w in positive_keywords):
            return True

        if self.classifier:
            try:
                truncated_text = text[:1000] 
                result = self.classifier(truncated_text, self.candidate_labels)
                top_label = result["labels"][0]
                score = result["scores"][0]
                return top_label == self.candidate_labels[0] and score > 0.4
            except Exception as e:
                print(f"Classification inference error ({e}); using rule-based check.")
        
        return True
