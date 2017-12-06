package com.example.demo;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.resources;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.metadata.strategy.query.PublicResolverBuilder;
import io.leangen.graphql.metadata.strategy.value.jackson.JacksonValueMapperFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootApplication
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  @Bean
  RouterFunction router(GraphQL graphQL) {
    return resources("/**", new ClassPathResource("static/"))
        .and(route(POST("/graphql"),
            r -> r.bodyToMono(GraphQLRequest.class)
                .flatMap(q -> {
                  ExecutionInput input = ExecutionInput.newExecutionInput().query(q.query).build();
                  ExecutionResult result = graphQL.execute(input);
                  return ServerResponse.ok().body(BodyInserters.fromObject(result));
                })));
  }


  @Bean
  GraphQL graphQL(TestGraph testGraph) {
    GraphQLSchema schema = new GraphQLSchemaGenerator()
        .withResolverBuilders(new PublicResolverBuilder(DemoApplication.class.getPackage().getName()))
        .withOperationsFromSingleton(testGraph)
        .withValueMapperFactory(new JacksonValueMapperFactory())
        .generate();
    return GraphQL.newGraphQL(schema).build();
  }

  @Bean
  TestGraph testGraph() {
    return new TestGraph(WebClient.builder().build());
  }
}

class GraphQLRequest {

  public String query;
}