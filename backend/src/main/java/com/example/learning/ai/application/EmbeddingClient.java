package com.example.learning.ai.application;

import java.util.List;

public interface EmbeddingClient {

    List<Double> embed(String input);
}
