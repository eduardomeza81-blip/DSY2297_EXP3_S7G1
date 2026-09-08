package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

public class Function {

    @FunctionName("MascotasRest")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {
                            HttpMethod.GET,
                            HttpMethod.POST,
                            HttpMethod.PUT,
                            HttpMethod.DELETE
                    },
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "mascotas/{id}"
            )
            HttpRequestMessage<Optional<String>> request,

            @BindingName("id") String id,

            final ExecutionContext context) {

        context.getLogger().info(
                "Método recibido: " + request.getHttpMethod()
                        + " - Mascota ID: " + id
        );

        switch (request.getHttpMethod()) {

            case GET:
                return handleGet(request, id);

            case POST:
                return handlePost(request, id);

            case PUT:
                return handlePut(request, id);

            case DELETE:
                return handleDelete(request, id);

            default:
                return request
                        .createResponseBuilder(HttpStatus.METHOD_NOT_ALLOWED)
                        .body("""
                        {
                            "mensaje": "Método no permitido"
                        }
                        """)
                        .build();
        }
    }

    // GET - Consultar mascota
    private HttpResponseMessage handleGet(
            HttpRequestMessage<Optional<String>> request,
            String id) {

        if ("1".equals(id)) {

            String respuesta = """
            {
                "id": 1,
                "nombre": "Toby",
                "especie": "Perro",
                "raza": "Beagle",
                "edad": 5,
                "cliente": "Juan Perez"
            }
            """;

            return request
                    .createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(respuesta)
                    .build();
        }

        return request
                .createResponseBuilder(HttpStatus.NOT_FOUND)
                .header("Content-Type", "application/json")
                .body("""
                {
                    "mensaje": "Mascota no encontrada"
                }
                """)
                .build();
    }

    // POST - Crear mascota
    private HttpResponseMessage handlePost(
            HttpRequestMessage<Optional<String>> request,
            String id) {

        String body = request.getBody().orElse("{}");

        String respuesta = """
        {
            "mensaje": "Mascota creada correctamente",
            "id": "%s",
            "datos": %s
        }
        """.formatted(id, body);

        return request
                .createResponseBuilder(HttpStatus.CREATED)
                .header("Content-Type", "application/json")
                .body(respuesta)
                .build();
    }

    // PUT - Actualizar mascota
    private HttpResponseMessage handlePut(
            HttpRequestMessage<Optional<String>> request,
            String id) {

        String body = request.getBody().orElse("{}");

        String respuesta = """
        {
            "mensaje": "Mascota actualizada correctamente",
            "id": "%s",
            "datos": %s
        }
        """.formatted(id, body);

        return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(respuesta)
                .build();
    }

    // DELETE - Eliminar mascota
    private HttpResponseMessage handleDelete(
            HttpRequestMessage<Optional<String>> request,
            String id) {

        String respuesta = """
        {
            "mensaje": "Mascota eliminada correctamente",
            "id": "%s"
        }
        """.formatted(id);

        return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(respuesta)
                .build();
    }
}