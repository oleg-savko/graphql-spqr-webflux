package com.example.demo;

import io.leangen.graphql.annotations.GraphQLQuery;
import org.springframework.web.reactive.function.client.WebClient;

public class TestGraph {

  private final WebClient client;

  public TestGraph(WebClient client) {
    this.client = client;
  }

  @GraphQLQuery(name = "test")
  public TestResponse test() {
    return client.get()
        .uri("https://google.com")
        .retrieve()
        .bodyToMono(String.class)
        .map(TestResponse::new)
        .block();
  }
}
