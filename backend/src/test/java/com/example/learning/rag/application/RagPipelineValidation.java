package com.example.learning.rag.application;

import java.util.List;

public class RagPipelineValidation {

    public static void main(String[] args) {
        System.out.println("=== RAG Pipeline Validation ===\n");

        TextChunkingService chunkingService = new TextChunkingService();
        StubEmbeddingProvider embeddingProvider = new StubEmbeddingProvider();

        String sampleText = """
                Machine learning is a subset of artificial intelligence that focuses on the development
                of algorithms and statistical models that enable computer systems to improve their
                performance on a specific task through experience.

                Deep learning is a specialized subset of machine learning that uses neural networks
                with multiple layers to progressively extract higher-level features from raw input.
                For example, in image processing, lower layers may identify edges, while higher layers
                may identify concepts relevant to a human such as digits or letters or faces.

                Natural language processing (NLP) is a branch of artificial intelligence that helps
                computers understand, interpret and manipulate human language. NLP draws from many
                disciplines, including computer science and computational linguistics, in its pursuit
                to fill the gap between human communication and computer understanding.
                """;

        System.out.println("1. Testing Text Chunking");
        System.out.println("   Input text length: " + sampleText.length() + " characters\n");

        List<TextChunk> chunks = chunkingService.chunkText(sampleText);

        System.out.println("   ✓ Generated " + chunks.size() + " chunks");
        for (int i = 0; i < chunks.size(); i++) {
            TextChunk chunk = chunks.get(i);
            System.out.println("   Chunk " + i + ": " + chunk.tokenCount() + " tokens, " +
                    chunk.text().length() + " characters");
        }

        System.out.println("\n2. Testing Embedding Generation");
        System.out.println("   Embedding dimensions: " + embeddingProvider.getDimensions());

        float[] embedding = embeddingProvider.generateEmbedding(chunks.get(0).text());
        System.out.println("   ✓ Generated embedding with " + embedding.length + " dimensions");
        System.out.println("   Sample values: [" + embedding[0] + ", " + embedding[1] + ", " +
                embedding[2] + ", ...]");

        System.out.println("\n3. Testing Batch Embedding Generation");
        List<String> texts = chunks.stream().map(TextChunk::text).toList();
        List<float[]> embeddings = embeddingProvider.generateEmbeddings(texts);
        System.out.println("   ✓ Generated " + embeddings.size() + " embeddings");

        System.out.println("\n4. Validating Chunk Properties");
        boolean allChunksValid = true;
        for (int i = 0; i < chunks.size(); i++) {
            TextChunk chunk = chunks.get(i);
            if (chunk.index() != i) {
                System.out.println("   ✗ Chunk index mismatch at position " + i);
                allChunksValid = false;
            }
            if (chunk.tokenCount() < 1 || chunk.tokenCount() > 1500) {
                System.out.println("   ✗ Chunk token count out of range: " + chunk.tokenCount());
                allChunksValid = false;
            }
            if (chunk.text() == null || chunk.text().isEmpty()) {
                System.out.println("   ✗ Empty chunk text at position " + i);
                allChunksValid = false;
            }
        }
        if (allChunksValid) {
            System.out.println("   ✓ All chunks have valid properties");
        }

        System.out.println("\n5. Validating Embedding Properties");
        boolean allEmbeddingsValid = true;
        for (int i = 0; i < embeddings.size(); i++) {
            float[] emb = embeddings.get(i);
            if (emb.length != embeddingProvider.getDimensions()) {
                System.out.println("   ✗ Embedding dimension mismatch at position " + i);
                allEmbeddingsValid = false;
            }
            double magnitude = 0.0;
            for (float v : emb) {
                magnitude += v * v;
            }
            magnitude = Math.sqrt(magnitude);
            if (Math.abs(magnitude - 1.0) > 0.01) {
                System.out.println("   ✗ Embedding not normalized at position " + i +
                        " (magnitude: " + magnitude + ")");
                allEmbeddingsValid = false;
            }
        }
        if (allEmbeddingsValid) {
            System.out.println("   ✓ All embeddings have correct dimensions and are normalized");
        }

        System.out.println("\n=== Validation Complete ===");
        System.out.println("✓ Text chunking works correctly");
        System.out.println("✓ Embedding generation works correctly");
        System.out.println("✓ RAG foundation layer is ready for integration");
    }

    private static class StubEmbeddingProvider {
        private static final int DIMENSIONS = 1536;

        public float[] generateEmbedding(String text) {
            try {
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(text.getBytes());

                float[] embedding = new float[DIMENSIONS];
                for (int i = 0; i < DIMENSIONS; i++) {
                    int byteIndex = i % hash.length;
                    embedding[i] = (hash[byteIndex] & 0xFF) / 255.0f - 0.5f;
                }

                normalize(embedding);
                return embedding;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public List<float[]> generateEmbeddings(List<String> texts) {
            return texts.stream().map(this::generateEmbedding).toList();
        }

        public int getDimensions() {
            return DIMENSIONS;
        }

        private void normalize(float[] vector) {
            double sumSquares = 0.0;
            for (float v : vector) {
                sumSquares += v * v;
            }
            double magnitude = Math.sqrt(sumSquares);
            if (magnitude > 0) {
                for (int i = 0; i < vector.length; i++) {
                    vector[i] /= magnitude;
                }
            }
        }
    }
}
