package com.collibra.codechallenge.protocol;

public interface Protocol {

    String helloMessage(String sessionId);

    String byeMessage(String sessionTime);

    String processMessage(String message, String sessionTime);
}
