package com.example.webApplication.Dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class TokenRespondTest {

    @Test
    void doesNotSerializeRefreshToken() throws Exception {
        String json = new ObjectMapper().writeValueAsString(
            new TokenRespond("access-token", "refresh-token")
        );

        assertEquals("{\"accessToken\":\"access-token\"}", json);
    }
}
