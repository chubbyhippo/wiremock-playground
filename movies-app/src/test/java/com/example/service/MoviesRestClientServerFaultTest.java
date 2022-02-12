package com.example.service;

import com.example.exception.MovieErrorResponse;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WireMockTest
class MoviesRestClientServerFaultTest {
    private MoviesRestClient moviesRestClient;

    @RegisterExtension
    private final WireMockExtension wm =
            WireMockExtension.newInstance().options(wireMockConfig().dynamicPort().extensions(new ResponseTemplateTransformer(true))).build();

    @BeforeEach
    void setUp() {
        int port = wm.getPort();
        String baseUrl = String.format("http://localhost:%s", port);
        System.out.println("baseUrl = " + baseUrl);
        WebClient webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void retrieveAllMovies() {
        wm.stubFor(get(anyUrl()).willReturn(serverError()));

        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveAllMovies());
    }

    @Test
    void retrieveAllMoviesWith503serviceUnavailable() {
        wm.stubFor(get(anyUrl())
                .willReturn(serverError()
                        .withStatus(503)
                        .withBody("Service Unavailable")));

        MovieErrorResponse movieErrorResponse = assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveAllMovies());
        assertEquals("Service Unavailable", movieErrorResponse.getMessage());

    }

    @Test
    void retrieveAllMoviesWithFaultResponse() {
        wm.stubFor(get(anyUrl())
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        MovieErrorResponse movieErrorResponse = assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveAllMovies());
        assertEquals("org.springframework.web.reactive.function" +
                ".client.WebClientRequestException: Connection prematurely closed BEFORE response; " +
                "nested exception is reactor.netty.http.client.PrematureCloseException:" +
                " Connection prematurely closed BEFORE response", movieErrorResponse.getMessage());

    }

    @Test
    void retrieveAllMoviesWithRandomDataThenClose() {
        wm.stubFor(get(anyUrl())
                .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveAllMovies());

    }
}
