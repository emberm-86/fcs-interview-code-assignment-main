package com.fulfilment.application.monolith.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfilment.application.monolith.exception.mappers.GlobalExceptionMapper;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionMapperTest {

    @Test
    void shouldMapAnyExceptionToInternalServerErrorResponse() throws Exception {
        // given
        GlobalExceptionMapper mapper = new GlobalExceptionMapper(new ObjectMapper());

        Exception exception = new RuntimeException("DB crash");

        // when
        Object entity;
        try (Response response = mapper.toResponse(exception)) {

            // then
            assertEquals(500, response.getStatus());
            assertEquals("application/json", response.getMediaType().toString());

            entity = response.getEntity();
        }
        assertNotNull(entity);

        // parse JSON safely
        ObjectMapper om = new ObjectMapper();
        JsonNode json = om.readTree(entity.toString());

        assertEquals("RuntimeException", json.get("exceptionType").asText());
        assertEquals(ErrorType.INTERNAL_SERVER_ERROR.status(), json.get("code").asInt());
        assertEquals("Internal server error", json.get("error").asText());
    }
}
