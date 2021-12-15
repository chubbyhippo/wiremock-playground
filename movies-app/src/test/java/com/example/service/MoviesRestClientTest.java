package com.example.service;

import com.example.dto.Movie;
import com.example.exception.MovieErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        assertTrue(movies.size() > 0);
    }

    @Test
    void retrieveMovieById() {
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
}
