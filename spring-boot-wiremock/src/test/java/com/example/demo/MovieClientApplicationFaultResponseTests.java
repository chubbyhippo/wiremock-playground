package com.example.demo;

import com.github.tomakehurst.wiremock.http.Fault;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class MovieClientApplicationFaultResponseTests {

    @Autowired
    MoviesRestClient moviesRestClient;

    @Value("${wiremock.server.port}")
    int port;

    @BeforeEach
    void setUp() {
        var baseUrl = String.format("http://localhost:%s", port);
        System.out.println("baseUrl = " + baseUrl);

        HttpClient httpClient = HttpClient.create()
                .doOnConnected(connection -> connection
                        .addHandler(new ReadTimeoutHandler(5))
                        .addHandler(new WriteTimeoutHandler(5)));

        var webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        moviesRestClient = new MoviesRestClient(webClient);
    }


    @Test
    void shouldRetrieveAllMovies() {
        stubFor(get(anyUrl()).willReturn(aResponse().withStatus(200).withHeader("Content-Type",
                "application/json").withBodyFile("all-movies.json")));

        var movies = moviesRestClient.retrieveAllMovies();
        System.out.println(movies);
        assertThat(movies.size()).isPositive();
    }

    @Test
    void shouldRetrieveAllMoviesWith503serviceUnavailable() {
        stubFor(get(anyUrl())
                .willReturn(serverError()
                        .withStatus(503)
                        .withBody("Service Unavailable")));

        var movieErrorResponse = assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveAllMovies());
        assertEquals("Service Unavailable", movieErrorResponse.getMessage());

    }

    @Test
    void shouldRetrieveAllMoviesWithFaultResponse() {
        stubFor(get(anyUrl())
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        var movieErrorResponse = assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveAllMovies());
        assertEquals("org.springframework.web.reactive.function" +
                ".client.WebClientRequestException: Connection prematurely closed BEFORE response; " +
                "nested exception is reactor.netty.http.client.PrematureCloseException:" +
                " Connection prematurely closed BEFORE response", movieErrorResponse.getMessage());

    }

    @Test
    void shouldRetrieveAllMoviesWithRandomDataThenClose() {
        stubFor(get(anyUrl())
                .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveAllMovies());

    }

    @Test
    void shouldRetrieveAllMoviesWithFixedDelay() {
        stubFor(get(anyUrl())
                .willReturn(ok().withFixedDelay(10000)));

        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveAllMovies());

    }

    @Test
    void retrieveAllMoviesWithRandomDelay() {
        stubFor(get(anyUrl())
                .willReturn(ok().withUniformRandomDelay(5500, 10000)));

        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveAllMovies());

    }
}
