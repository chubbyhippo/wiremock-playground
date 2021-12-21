package com.example.service;

import com.example.constants.MoviesAppConstants;
import com.example.dto.Movie;
import com.example.exception.MovieErrorResponse;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class MoviesRestClientTest {
    private MoviesRestClient moviesRestClient;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension
            .newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                    .extensions(new ResponseTemplateTransformer(true)))
            .build();

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
        wm.stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("all-movies.json")));

        List<Movie> movies = moviesRestClient.retrieveAllMovies();
        System.out.println(movies);
        assertTrue(movies.size() > 0);
    }

    @Test
    void retrieveAllMoviesMatchesUrl() {
        wm.stubFor(get(urlPathEqualTo(MoviesAppConstants.GET_ALL_MOVIES_V1))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("all-movies.json")));

        List<Movie> movies = moviesRestClient.retrieveAllMovies();
        System.out.println(movies);
        assertTrue(movies.size() > 0);
    }

    @Test
    void retrieveMovieById() {
        wm.stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movie.json")));
        Integer movieId = 1;

        Movie movie = moviesRestClient.retrieveMovieById(movieId);

        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void retrieveMovieByIdNotFound() {
        Integer movieId = 100;

        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveMovieById(movieId));
    }

    @Test
    void retrieveMoviesByName() {
        String movieName = "Avengers";

        List<Movie> movies = moviesRestClient.retrieveMoviesByName(movieName);

        assertEquals(4, movies.size());
    }

    @Test
    void retrieveMoviesByNameNotFound() {
        String movieName = "ABC";

        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveMoviesByName(movieName));
    }

    @Test
    void retrieveMoviesByYear() {
        Integer year = 2012;

        List<Movie> movies = moviesRestClient.retrieveMoviesByYear(year);

        assertEquals(2, movies.size());
    }

    @Test
    void retrieveMoviesByYearNotFound() {
        Integer year = 2999;

        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.retrieveMoviesByYear(year));
    }

    @Test
    void addMovie() {

        Movie movie = new Movie(null, "The Matrix", "Keanu Reeves", LocalDate.of(1999, 3, 24), 1999);

        Movie addedMovie = moviesRestClient.addMovie(movie);
        System.out.println(addedMovie);
        assertNotNull(addedMovie.getMovieId());
    }

    @Test
    void addMovieBadRequest() {

        Movie movie = new Movie(null, "The Matrix", null, LocalDate.of(1999, 3, 24), 1999);

        String expectedErrorMessage = "Please pass all the input fields : [name]";
        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.addMovie(movie), expectedErrorMessage);
    }

    @Test
    void updateMovie() {
        Integer movieId = 3;
        String cast = "ABC";
        Movie movie = Movie.builder().cast(cast).build();

        Movie updatedMovie = moviesRestClient.updateMovie(movieId, movie);

        assertTrue(updatedMovie.getCast().contains(cast));
    }

    @Test
    void updateMovieNotFound() {
        Integer movieId = 999;
        String cast = "ABC";
        Movie movie = Movie.builder().cast(cast).build();

        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.updateMovie(movieId, movie));
    }

    @Test
    void deleteMovie() {
        Movie movie = new Movie(null, "The Matrix 99", "Keanu Reeves", LocalDate.of(1999, 3, 24), 1999);
        Movie addedMovie = moviesRestClient.addMovie(movie);

        String responseMessage = moviesRestClient.deleteMovie(addedMovie.getMovieId());

        String expectedErrorMessage = "Movie Deleted Successfully";
        assertEquals(expectedErrorMessage, responseMessage);
    }

    @Test
    void deleteMovieNotFound() {
        Integer movieId = 99;
        assertThrows(MovieErrorResponse.class,
                () -> moviesRestClient.deleteMovie(movieId));
    }
}
