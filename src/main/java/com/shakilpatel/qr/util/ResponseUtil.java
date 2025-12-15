package com.shakilpatel.qr.util;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.core.Response;

public class ResponseUtil {

    public static Response success(Object data, String message) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("success", true)
                .add("message", message);

        if (data != null) {
            // Add data based on type
            if (data instanceof JsonObject) {
                builder.add("data", (JsonObject) data);
            } else if (data instanceof String) {
                builder.add("data", (String) data);
            } else if (data instanceof Number) {
                builder.add("data", ((Number) data).longValue());
            } else if (data instanceof Boolean) {
                builder.add("data", (Boolean) data);
            }
        }

        return Response.ok(builder.build()).build();
    }

    public static Response success(Object data) {
        return success(data, "Success");
    }

    public static Response created(Object data, String message) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("success", true)
                .add("message", message);

        if (data != null) {
            if (data instanceof JsonObject) {
                builder.add("data", (JsonObject) data);
            }
        }

        return Response.status(Response.Status.CREATED).entity(builder.build()).build();
    }

    public static Response error(String message, Response.Status status) {
        JsonObject error = Json.createObjectBuilder()
                .add("success", false)
                .add("error", message)
                .build();

        return Response.status(status).entity(error).build();
    }

    public static Response badRequest(String message) {
        return error(message, Response.Status.BAD_REQUEST);
    }

    public static Response unauthorized(String message) {
        return error(message, Response.Status.UNAUTHORIZED);
    }

    public static Response forbidden(String message) {
        return error(message, Response.Status.FORBIDDEN);
    }

    public static Response notFound(String message) {
        return error(message, Response.Status.NOT_FOUND);
    }

    public static Response internalError(String message) {
        return error(message, Response.Status.INTERNAL_SERVER_ERROR);
    }
}
