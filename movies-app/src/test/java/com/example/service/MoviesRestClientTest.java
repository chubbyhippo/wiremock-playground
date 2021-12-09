package com.example.service;

import com.example.dto.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MoviesRestClientTest {
    private MoviesRestClient moviesRestClient;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:8081";
        WebClient webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void retrieveAllMovies() {
        List<Movie> movies = moviesRestClient.retrieveAllMovies();
        System.out.println(movies);
        assertTrue(movies.size()>0);
    }
}
