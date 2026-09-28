package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificarCliente {

    @FunctionName("NotificarCliente")
    public void run(
            @EventGridTrigger(
                    name = "evento"
            )
            String evento,
            final ExecutionContext context) {

        context.getLogger().info(
                "Evento recibido desde Azure Event Grid."
        );

        context.getLogger().info(
                "Contenido del evento: " + evento
        );

        String idNotificacion = UUID.randomUUID().toString();

        context.getLogger().info(
                "========================================"
        );

        context.getLogger().info(
                "NOTIFICACIÓN AL CLIENTE"
        );

        context.getLogger().info(
                "Id Notificación: " + idNotificacion
        );

        context.getLogger().info(
                "Estado: ENVIADA"
        );

        context.getLogger().info(
                "Fecha envío: " + LocalDateTime.now()
        );

        context.getLogger().info(
                "========================================"
        );
    }
}