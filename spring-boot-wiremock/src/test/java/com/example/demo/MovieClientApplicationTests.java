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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class MovieClientApplicationTests {
    @Autowired
    MoviesRestClient moviesRestClient;

    @Value("${wiremock.server.port}")
    int port;

    @BeforeEach
    void setUp() {
        var baseUrl = String.format("http://localhost:%s", port);
        System.out.println("baseUrl = " + baseUrl);
        var webClient = WebClient.builder()
                .baseUrl(baseUrl)
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
    void shouldRetrieveAllMoviesMatchesUrl() {
        stubFor(get(urlPathEqualTo(MoviesAppConstants.GET_ALL_MOVIES_V1))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("all-movies.json")));

        var movies = moviesRestClient.retrieveAllMovies();
        System.out.println(movies);
        assertThat(movies.size()).isPositive();
    }

    @Test
    void shouldRetrieveMovieById() {
        stubFor(get(urlPathMatching("/movies/v1/movie_infos/[0-9]")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json")
                .withBodyFile("movie.json")));
        Integer movieId = 1;

        var movie = moviesRestClient.retrieveMovieById(movieId);
        System.out.println("movie = " + movie);
        assertThat(movie.getName()).isEqualTo("Batman Begins");
    }

    @Test
    void shouldRetrieveMovieByIdResponseTemplate() {
        stubFor(get(urlPathMatching("/movies/v1/movie_infos/[0-9]"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movie-template.json")
                ));
        Integer movieId = 1;

        var movie = moviesRestClient.retrieveMovieById(movieId);
        System.out.println("movie = " + movie);
        assertEquals("Batman Begins", movie.getName());
        assertEquals(movieId, movie.getMovieInfoId().intValue());
    }

    @Test
    void shouldRetrieveMovieByIdNotFound() {
        stubFor(get(urlPathMatching("/v1/movie_infos/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("404-movie-id.json")
                ));
        Integer movieId = 100;

        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById(movieId));
    }

    @Test
    void shouldRetrieveMoviesByName() {
        var movieName = "Avengers";
        stubFor(get(urlEqualTo(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("avengers.json")
                ));

        var movies = moviesRestClient.retrieveMoviesByName(movieName);
        var expectedName = "Avengers: End Game";
        assertEquals(4, movies.size());
        assertEquals(expectedName, movies.get(3).getName());
    }

    @Test
    void shouldRetrieveMoviesByNameUrlPathEqualTo() {
        var movieName = "Avengers";
        stubFor(get(urlPathEqualTo(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(movieName))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("avengers.json")
                ));

        var movies = moviesRestClient.retrieveMoviesByName(movieName);
        var expectedName = "Avengers: End Game";
        assertEquals(4, movies.size());
        assertEquals(expectedName, movies.get(3).getName());
    }
}
