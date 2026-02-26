package com.fulfilment.application.monolith.exception;

import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {

        LOGGER.error("Unhandled exception", exception);

        int status = resolveStatus(exception);

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "timestamp", OffsetDateTime.now().toString(),
                        "status", status,
                        "error", resolveMessage(exception, status)
                ))
                .build();
    }

    private int resolveStatus(Exception exception) {
        if (exception instanceof WebApplicationException webEx) {
            return webEx.getResponse().getStatus();
        }
        if (exception instanceof IllegalArgumentException) {
            return 400;
        }
        return 500;
    }

    private String resolveMessage(Exception exception, int status) {
        return status == 500
                ? "Internal server error"
                : exception.getMessage();
    }
}