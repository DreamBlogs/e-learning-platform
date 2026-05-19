package com.example.learning.rag.application;

import java.util.List;

public interface EmbeddingProvider {

    float[] generateEmbedding(String text);

    List<float[]> generateEmbeddings(List<String> texts);

    int getDimensions();
}
