package com.fulfilment.application.monolith.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfilment.application.monolith.exception.mappers.BusinessExceptionMapper;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionMapperTest {

    private ObjectMapper objectMapper;
    private BusinessExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mapper = new BusinessExceptionMapper(objectMapper);
    }

    @Test
    void shouldMapConflictExceptionToHttpResponse() {
        BusinessException exception =
                new BusinessException("Request conflicted", ErrorType.CONFLICT);

        assertMappedResponse(exception);
    }

    @Test
    void shouldMapNotFoundExceptionToHttpResponse() {
        BusinessException exception =
                new BusinessException("Request resource not found", ErrorType.NOT_FOUND);

        assertMappedResponse(exception);
    }

    @Test
    void shouldMapValidationExceptionToHttpResponse() {
        BusinessException exception =
                new BusinessException("Invalid request", ErrorType.VALIDATION);

        assertMappedResponse(exception);
    }

    private void assertMappedResponse(BusinessException exception) {
        try (Response response = mapper.toResponse(exception)) {

            assertHttpLayer(exception, response);

            JsonNode json = parseJson(response);

            assertJsonLayer(exception, json);
        }
    }

    private void assertHttpLayer(BusinessException exception, Response response) {
        assertEquals(exception.getErrorType().status(), response.getStatus());
        assertEquals("application/json", response.getMediaType().toString());
        assertNotNull(response.getEntity());
    }

    private JsonNode parseJson(Response response) {
        try {
            return objectMapper.readTree(response.getEntity().toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response JSON", e);
        }
    }

    private void assertJsonLayer(BusinessException exception, JsonNode json) {
        assertAll(
                () -> assertEquals(
                        exception.getClass().getSimpleName(),
                        json.get("exceptionType").asText()
                ),
                () -> assertEquals(
                        exception.getErrorType().status(),
                        json.get("code").asInt()
                ),
                () -> assertEquals(
                        exception.getMessage(),
                        json.get("error").asText()
                )
        );
    }
}