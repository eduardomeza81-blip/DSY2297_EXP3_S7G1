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

public class NotificarCliente {

    @FunctionName("NotificarCliente")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "notificaciones"
            )
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Procesando notificación al cliente.");

        String body = request.getBody().orElse(null);

        if (body == null || body.isBlank()) {
            return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("""
                    {
                        "mensaje": "Debe enviar los datos de la notificación"
                    }
                    """)
                    .build();
        }

        String idNotificacion = UUID.randomUUID().toString();

        String respuesta = String.format("""
        {
            "idNotificacion": "%s",
            "estado": "ENVIADA",
            "fechaEnvio": "%s",
            "datosRecibidos": %s
        }
        """,
        idNotificacion,
        LocalDateTime.now(),
        body);

        return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(respuesta)
                .build();
    }
}