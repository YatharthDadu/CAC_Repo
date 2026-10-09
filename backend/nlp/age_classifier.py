from transformers import pipeline

class AgeClassifier:
    def __init__(self):
        print("Loading Zero-Shot Classification Pipeline...")
        # Using a fast zero-shot classifier
        self.classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")
        self.candidate_labels = [
            "eligible for high school teens 14-18",
            "requires 18+ adult or background check"
        ]

    def is_eligible_for_high_school(self, text):
        """
        Classifies if the volunteering opportunity is suitable for high school students.
        """
        # Truncate text to avoid exceeding max sequence length
        truncated_text = text[:1000] 
        result = self.classifier(truncated_text, self.candidate_labels)
        
        top_label = result["labels"][0]
        score = result["scores"][0]
        
        # If the top label is high school eligible and score is confident enough
        return top_label == self.candidate_labels[0] and score > 0.5
