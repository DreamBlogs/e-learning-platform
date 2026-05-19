package com.example.learning.processing.application;

public interface MessagePublisher {

    void publish(String queueName, Object payload);
}
