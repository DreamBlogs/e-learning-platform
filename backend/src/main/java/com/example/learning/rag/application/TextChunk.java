package com.example.learning.rag.application;

import java.util.ArrayList;
import java.util.List;

public record TextChunk(
        int index,
        String text,
        int tokenCount
) {
}
