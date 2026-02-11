package com.fulfilment.application.monolith.exception.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.exception.BusinessException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    private final ObjectMapper objectMapper;

    public BusinessExceptionMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Override
    public Response toResponse(BusinessException exception) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("exceptionType", exception.getClass().getSimpleName());
        json.put("code", exception.getErrorType().status());
        json.put("error", exception.getMessage());

        return Response.status(exception.getErrorType().status())
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .build();
    }
}
