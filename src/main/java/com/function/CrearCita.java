package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

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

        String respuesta = String.format("""
        {
            "mensaje": "Cita creada correctamente",
            "idCita": "%s",
            "estado": "AGENDADA",
            "fechaCreacion": "%s",
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