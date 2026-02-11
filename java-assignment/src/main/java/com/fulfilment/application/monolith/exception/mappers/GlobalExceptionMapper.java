package com.fulfilment.application.monolith.exception.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.exception.ErrorType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private final ObjectMapper objectMapper;

    public GlobalExceptionMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Response toResponse(Exception exception) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("exceptionType", exception.getClass().getSimpleName());
        json.put("code", ErrorType.INTERNAL_SERVER_ERROR.status());
        json.put("error", "Internal server error");

        return Response.status(ErrorType.INTERNAL_SERVER_ERROR.status())
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .build();
    }
}