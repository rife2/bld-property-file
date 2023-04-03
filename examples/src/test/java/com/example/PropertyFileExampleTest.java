package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyFileExampleTest {
    @Test
    void verifyHello() {
        assertEquals("Hello World!", new PropertyFileExampleMain().getMessage());
    }
}
