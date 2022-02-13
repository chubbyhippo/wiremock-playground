package com.example.service;

import com.example.constants.MoviesAppConstants;
import com.example.dto.MovieInfo;
import com.example.exception.MovieErrorResponse;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

import static com.example.constants.MoviesAppConstants.ADD_MOVIE_V1;
import static com.example.constants.MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class MoviesRestClientTest {
    private MoviesRestClient moviesRestClient;

    @RegisterExtension
    private final WireMockExtension wm =
            WireMockExtension.newInstance().options(wireMockConfig().dynamicPort().extensions(new ResponseTemplateTransformer(true))).build();

    @BeforeEach
    void setUp() {
        var port = wm.getPort();
        var baseUrl = String.format("http://localhost:%s", port);
        System.out.println("baseUrl = " + baseUrl);
        var webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void retrieveAllMovies() {
        wm.stubFor(get(anyUrl()).willReturn(aResponse().withStatus(200).withHeader("Content-Type",
                "application/json").withBodyFile("all-movies.json")));

        var movies = moviesRestClient.retrieveAllMovies();
        System.out.println(movies);
        assertTrue(movies.size() > 0);
    }

    @Test
    void retrieveAllMoviesMatchesUrl() {
        wm.stubFor(get(urlPathEqualTo(MoviesAppConstants.GET_ALL_MOVIES_V1)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBodyFile("all-movies.json")));

        var movies = moviesRestClient.retrieveAllMovies();
        System.out.println(movies);
        assertTrue(movies.size() > 0);
    }

    @Test
    void retrieveMovieById() {
        wm.stubFor(get(urlPathMatching("/movies/v1/movie_infos/[0-9]")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBodyFile("movie.json")));
        Integer movieId = 1;

        var movie = moviesRestClient.retrieveMovieById(movieId);
        System.out.println("movie = " + movie);
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void retrieveMovieByIdResponseTemplate() {
        wm.stubFor(get(urlPathMatching("/movies/v1/movie_infos/[0-9]"))
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
    void retrieveMovieByIdNotFound() {
        wm.stubFor(get(urlPathMatching("/v1/movie_infos/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("404-movie-id.json")
                ));
        Integer movieId = 100;

        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById(movieId));
    }

    @Test
    void retrieveMoviesByName() {
        var movieName = "Avengers";
        wm.stubFor(get(urlEqualTo(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName))
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
    void retrieveMoviesByNameUrlPathEqualTo() {
        var movieName = "Avengers";
        wm.stubFor(get(urlPathEqualTo(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1))
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

    @Test
    void retrieveMoviesByNameResponseTemplate() {
        var movieName = "Avengers";
        wm.stubFor(get(urlEqualTo(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movie-by-name-template.json")
                ));

        var movies = moviesRestClient.retrieveMoviesByName(movieName);
        System.out.println("movies = " + movies);
        var expectedName = "Avengers: End Game";
        assertEquals(4, movies.size());
        assertEquals(expectedName, movies.get(3).getName());
    }

    @Test
    void retrieveMoviesByNameNotFound() {
        var movieName = "ABC";
        wm.stubFor(get(urlPathEqualTo(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(movieName))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("404-movie-name.json")
                ));

        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMoviesByName(movieName));
    }

    @Test
    void retrieveMoviesByYear() {
        Integer year = 2012;
        wm.stubFor(get(urlPathEqualTo(MoviesAppConstants.MOVIE_BY_YEAR_QUERY_PARAM_V1))
                .withQueryParam("year", equalTo(String.valueOf(year)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("year-template.json")
                ));
        var movies = moviesRestClient.retrieveMoviesByYear(year);
        System.out.println("movies = " + movies);
        assertEquals(2, movies.size());
    }

    @Test
    void retrieveMoviesByYearNotFound() {
        Integer year = 2999;
        wm.stubFor(get(urlPathEqualTo(MoviesAppConstants.MOVIE_BY_YEAR_QUERY_PARAM_V1))
                .withQueryParam("year", equalTo(String.valueOf(year)))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("404-movie-year.json")
                ));
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMoviesByYear(year));
    }

    @Test
    void addMovie() {
        var movie = new MovieInfo(null, "The Matrix", "Keanu Reeves",
                LocalDate.of(1999, 3, 24), 1999);
        wm.stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("The Matrix")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Keanu")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("add-movie.json")
                ));
        var addedMovie = moviesRestClient.addMovie(movie);
        System.out.println(addedMovie);
        assertNotNull(addedMovie.getMovieInfoId());
    }

    @Test
    void addMovieResponseTemplate() {
        var movie = new MovieInfo(null, "The Matrix", "Keanu Reeves",
                LocalDate.of(1999, 3, 24), 1999);
        wm.stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("The Matrix")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Keanu")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("add-movies-template.json")
                ));
        var addedMovie = moviesRestClient.addMovie(movie);
        System.out.println(addedMovie);
        assertNotNull(addedMovie.getMovieInfoId());
    }

    @Test
    void addMovieBadRequest() {

        var movie = new MovieInfo(null, null, "Keanu Reeves",
                LocalDate.of(1999, 3, 24), 1999);
        wm.stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.cast", containing("Keanu")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("400-invalid-input.json")
                ));
        var expectedErrorMessage = "Please pass all the input fields : [name]";
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addMovie(movie), expectedErrorMessage);
    }

    @Test
    void updateMovie() {
        Integer movieId = 3;
        var cast = "ABC";
        var movie = MovieInfo.builder().cast(cast).build();
        wm.stubFor(put(urlPathMatching("/movies/v1/movie_infos/[0-9]+"))
                .withRequestBody(matchingJsonPath("$.cast", containing(cast)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("update-movie-template.json")
                ));
        var updatedMovie = moviesRestClient.updateMovie(movieId, movie);
        System.out.println(updatedMovie);
        assertTrue(updatedMovie.getCast().contains(cast));
    }

    @Test
    void updateMovieNotFound() {
        Integer movieId = 999;
        var cast = "ABC";
        var movie = MovieInfo.builder().cast(cast).build();
        wm.stubFor(put(urlPathMatching("/v1/movie_infos/[0-9]+"))
                .withRequestBody(matchingJsonPath("$.cast", containing(cast)))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                ));
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.updateMovie(movieId, movie));
    }

    @Test
    void deleteMovie() {
        var movie = new MovieInfo(null, "The Matrix", "Keanu Reeves", LocalDate.of(1999, 3, 24), 1999);
        wm.stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("The Matrix")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Keanu")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("add-movie.json")
                ));

        var addedMovie = moviesRestClient.addMovie(movie);
        var expectedErrorMessage = "Movie Deleted Successfully";
        wm.stubFor(delete(urlPathMatching("/movies/v1/movie_infos/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedErrorMessage)
                ));

        var responseMessage = moviesRestClient.deleteMovie(addedMovie.getMovieInfoId());

        assertEquals(expectedErrorMessage, responseMessage);
    }

    @Test
    void deleteMovieNotFound() {
        Long movieId = 99L;
        wm.stubFor(delete(urlPathMatching("/v1/movie_infos/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                ));
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.deleteMovie(movieId));
    }

    @Test
    void deleteMovieByName() {
        var movie = new MovieInfo(null, "The Matrix", "Keanu Reeves", LocalDate.of(1999, 3, 24), 1999);
        wm.stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("The Matrix")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Keanu")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("add-movie.json")
                ));

        var addedMovie = moviesRestClient.addMovie(movie);
        var expectedErrorMessage = "Movie Deleted Successfully";
        wm.stubFor(delete(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(addedMovie.getName()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                ));

        var responseMessage = moviesRestClient.deleteMovieByName(addedMovie.getName());

        assertEquals(expectedErrorMessage, responseMessage);


        wm.verify(exactly(1), postRequestedFor(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("The Matrix")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Keanu"))));

        wm.verify(exactly(1), deleteRequestedFor(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(addedMovie.getName())));
    }

    @Test
    void deleteMovieByNameWithSelectiveProxy() {
        var movie = new MovieInfo(null, "The Matrix", "Keanu Reeves", LocalDate.of(1999, 3, 24), 1999);

        wm.stubFor(any(anyUrl()).willReturn(aResponse().proxiedFrom("http://localhost:8080")));

        var addedMovie = moviesRestClient.addMovie(movie);
        var expectedErrorMessage = "Movie Deleted Successfully";
        wm.stubFor(delete(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(addedMovie.getName()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                ));

        var responseMessage = moviesRestClient.deleteMovieByName(addedMovie.getName());

        assertEquals(expectedErrorMessage, responseMessage);

        wm.verify(exactly(1), deleteRequestedFor(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(addedMovie.getName())));
    }
}
