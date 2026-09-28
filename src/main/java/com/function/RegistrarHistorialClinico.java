package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegistrarHistorialClinico {

    @FunctionName("RegistrarHistorialClinico")
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

        String idHistorial = UUID.randomUUID().toString();

        context.getLogger().info(
                "========================================"
        );

        context.getLogger().info(
                "REGISTRO DE HISTORIAL CLINICO"
        );

        context.getLogger().info(
                "Id Historial: " + idHistorial
        );

        context.getLogger().info(
                "Estado: REGISTRADO"
        );

        context.getLogger().info(
                "Fecha registro: " + LocalDateTime.now()
        );

        context.getLogger().info(
                "========================================"
        );
    }
}