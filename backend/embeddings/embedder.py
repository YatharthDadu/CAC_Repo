from sentence_transformers import SentenceTransformer

class Embedder:
    def __init__(self):
        print("Loading SentenceTransformer model...")
        self.model = SentenceTransformer('all-MiniLM-L6-v2')

    def generate_embedding(self, text):
        """
        Generates a 384-dimensional dense semantic embedding for the text.
        """
        # Ensure text is not empty
        if not text or not isinstance(text, str):
            return [0.0] * 384
            
        embedding = self.model.encode(text)
        # Convert numpy array to list of floats for pgvector
        return embedding.tolist()
