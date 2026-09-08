package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MascotaGraphQL {

    @FunctionName("MascotaGraphQL")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "graphql"
            )
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Procesando consulta GraphQL");

        try {

            String schema = """
                    type Mascota {
                        id: Int
                        nombre: String
                        especie: String
                        raza: String
                        edad: Int
                        cliente: String
                    }

                    type Query {
                        mascota(id: Int!): Mascota
                    }
                    """;

            SchemaParser schemaParser = new SchemaParser();

            TypeDefinitionRegistry typeDefinitionRegistry =
                    schemaParser.parse(schema);

            RuntimeWiring runtimeWiring = RuntimeWiring
                    .newRuntimeWiring()
                    .type("Query", builder ->
                            builder.dataFetcher(
                                    "mascota",
                                    environment -> {

                                        Integer id =
                                                environment.getArgument("id");

                                        if (id == 1) {

                                            Map<String, Object> mascota =
                                                    new HashMap<>();

                                            mascota.put("id", 1);
                                            mascota.put("nombre", "Toby");
                                            mascota.put("especie", "Perro");
                                            mascota.put("raza", "Beagle");
                                            mascota.put("edad", 5);
                                            mascota.put(
                                                    "cliente",
                                                    "Juan Perez"
                                            );

                                            return mascota;
                                        }

                                        return null;
                                    }
                            )
                    )
                    .build();

            SchemaGenerator schemaGenerator =
                    new SchemaGenerator();

            GraphQLSchema graphQLSchema =
                    schemaGenerator.makeExecutableSchema(
                            typeDefinitionRegistry,
                            runtimeWiring
                    );

            GraphQL graphQL =
                    GraphQL.newGraphQL(graphQLSchema).build();

            String query = request
                    .getBody()
                    .orElse("");

            ExecutionInput executionInput =
                    ExecutionInput.newExecutionInput()
                            .query(query)
                            .build();

            ExecutionResult executionResult =
                    graphQL.execute(executionInput);

            return request
                    .createResponseBuilder(HttpStatus.OK)
                    .header(
                            "Content-Type",
                            "application/json"
                    )
                    .body(
                            executionResult.toSpecification()
                    )
                    .build();

        } catch (Exception e) {

            context.getLogger()
                    .severe(
                            "Error GraphQL: "
                                    + e.getMessage()
                    );

            return request
                    .createResponseBuilder(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .header(
                            "Content-Type",
                            "application/json"
                    )
                    .body("""
                            {
                                "mensaje": "Error procesando consulta GraphQL"
                            }
                            """)
                    .build();
        }
    }
}