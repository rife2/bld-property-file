package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyFileExampleTest {
    @Test
    void verifyHello() {
        assertEquals("Hello World!", new PropertyFileExampleMain().getMessage());
    }
}
