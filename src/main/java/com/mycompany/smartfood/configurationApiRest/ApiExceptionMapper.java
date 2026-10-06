package com.mycompany.smartfood.configurationApiRest;

import java.util.Map;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof WebApplicationException web) {
            return web.getResponse();
        }
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof IllegalArgumentException invalid) {
                String message = invalid.getMessage() == null ? "Requête invalide" : invalid.getMessage();
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(Map.of("error", message))
                        .build();
            }
            cause = cause.getCause();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", "Erreur interne"))
                .build();
    }
}
