package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class DemoApplicationTests {

    @Autowired
    MoviesRestClient moviesRestClient;

    @Value("${wiremock.server.port}")
    int port;

    @BeforeEach
    void setUp() {
        var baseUrl = String.format("http://localhost:%s", port);
        System.out.println("baseUrl = " + baseUrl);
        var webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }


    @Test
    void retrieveAllMovies() {
        stubFor(get(anyUrl()).willReturn(aResponse().withStatus(200).withHeader("Content-Type",
                "application/json").withBodyFile("all-movies.json")));

        var movies = moviesRestClient.retrieveAllMovies();
        System.out.println(movies);
        assertThat(movies.size()).isPositive();
    }

}
