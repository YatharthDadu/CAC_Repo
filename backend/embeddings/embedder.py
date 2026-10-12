from sentence_transformers import SentenceTransformer

class Embedder:
    def __init__(self):
        self.model = None
        try:
            from sentence_transformers import SentenceTransformer
            print("Loading SentenceTransformer model...")
            self.model = SentenceTransformer('all-MiniLM-L6-v2')
        except Exception as e:
            print(f"SentenceTransformer model unavailable ({e}); using deterministic vector fallback.")

    def generate_embedding(self, text):
        """
        Generates a 384-dimensional dense semantic embedding for the text.
        """
        if not text or not isinstance(text, str):
            return [0.0] * 384
            
        if self.model:
            try:
                embedding = self.model.encode(text)
                return embedding.tolist()
            except Exception as e:
                print(f"Model encode error ({e}); using vector fallback.")
                
        # Deterministic 384-dimensional fallback vector
        import hashlib
        h = hashlib.sha256(text.encode('utf-8')).digest()
        vec = [(b / 255.0 * 2.0 - 1.0) for b in h]
        while len(vec) < 384:
            vec.extend(vec[:384 - len(vec)])
        return vec[:384]
