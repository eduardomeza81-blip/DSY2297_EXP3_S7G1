package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class CrearCita {

    @FunctionName("CrearCita")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "citas"
            )
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Procesando creación de cita veterinaria.");

        String body = request.getBody().orElse(null);

        if (body == null || body.isBlank()) {
            return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("""
                    {
                        "mensaje": "Debe enviar los datos de la cita"
                    }
                    """)
                    .build();
        }

        String idCita = UUID.randomUUID().toString();

        // ---------------------------------------------------------
        // PUBLICAR EVENTO EN AZURE EVENT GRID
        // ---------------------------------------------------------
        try {

            String endpoint = System.getenv("EVENT_GRID_ENDPOINT");
            String key = System.getenv("EVENT_GRID_KEY");

            if (endpoint == null || key == null) {
                throw new IllegalStateException(
                        "No están configuradas las variables de Event Grid"
                );
            }

            String evento = String.format("""
            [
                {
                    "id": "%s",
                    "eventType": "Veterinaria.CitaCreada",
                    "subject": "veterinaria/citas/%s",
                    "eventTime": "%s",
                    "data": {
                        "idCita": "%s",
                        "estado": "AGENDADA",
                        "datosCita": %s
                    },
                    "dataVersion": "1.0"
                }
            ]
            """,
            UUID.randomUUID(),
            idCita,
            Instant.now(),
            idCita,
            body);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest eventRequest = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("aeg-sas-key", key)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(evento))
                    .build();

            HttpResponse<String> eventResponse =
                    client.send(
                            eventRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            context.getLogger().info(
                    "Respuesta Event Grid: " + eventResponse.statusCode()
            );

            if (eventResponse.statusCode() >= 200 &&
                eventResponse.statusCode() < 300) {

                context.getLogger().info(
                        "Evento Veterinaria.CitaCreada publicado correctamente."
                );

            } else {

                context.getLogger().severe(
                        "Error publicando evento en Event Grid: "
                        + eventResponse.body()
                );

                return request
                        .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                        .header("Content-Type", "application/json")
                        .body("""
                        {
                            "mensaje": "La cita fue procesada pero ocurrió un error al publicar el evento"
                        }
                        """)
                        .build();
            }

        } catch (Exception e) {

            context.getLogger().severe(
                    "Error al publicar evento: " + e.getMessage()
            );

            return request
                    .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body("""
                    {
                        "mensaje": "Error al publicar el evento de la cita"
                    }
                    """)
                    .build();
        }

        // ---------------------------------------------------------
        // RESPUESTA HTTP
        // ---------------------------------------------------------

        String respuesta = String.format("""
        {
            "mensaje": "Cita creada correctamente",
            "idCita": "%s",
            "estado": "AGENDADA",
            "fechaCreacion": "%s",
            "eventoPublicado": "Veterinaria.CitaCreada",
            "datosRecibidos": %s
        }
        """,
        idCita,
        LocalDateTime.now(),
        body);

        return request
                .createResponseBuilder(HttpStatus.CREATED)
                .header("Content-Type", "application/json")
                .body(respuesta)
                .build();
    }
}