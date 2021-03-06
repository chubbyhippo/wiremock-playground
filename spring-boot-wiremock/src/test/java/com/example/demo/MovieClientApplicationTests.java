package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

import static com.example.demo.MoviesAppConstants.ADD_MOVIE_V1;
import static com.example.demo.MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1;
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
        assertThat(movies).isNotEmpty();
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
        assertThat(movies).isNotEmpty();
    }

    @Test
    void shouldRetrieveMovieById() {
        stubFor(get(urlPathMatching("/movies/v1/movie_infos/[0-9]")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json")
                .withBodyFile("movie.json")));
        Long movieId = 1L;

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
        Long movieId = 1L;

        var movie = moviesRestClient.retrieveMovieById(movieId);
        System.out.println("movie = " + movie);
        assertThat(movie.getName()).isEqualTo("Batman Begins");
        assertThat(movie.getMovieInfoId()).isEqualTo(movieId);
    }

    @Test
    void shouldRetrieveMovieByIdNotFound() {
        stubFor(get(urlPathMatching("/v1/movie_infos/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("404-movie-id.json")
                ));
        Long movieId = 100L;

        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById(movieId));
    }

    @Test
    void shouldRetrieveMoviesByName() {
        var movieName = "Avengers";
        stubFor(get(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName))
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
        stubFor(get(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
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
    void shouldRetrieveMoviesByNameResponseTemplate() {
        var movieName = "Avengers";
        stubFor(get(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName))
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
    void shouldRetrieveMoviesByNameNotFound() {
        var movieName = "ABC";
        stubFor(get(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(movieName))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("404-movie-name.json")
                ));

        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMoviesByName(movieName));
    }

    @Test
    void shouldRetrieveMoviesByYear() {
        Integer year = 2012;
        stubFor(get(urlPathEqualTo(MoviesAppConstants.MOVIE_BY_YEAR_QUERY_PARAM_V1))
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
    void shouldRetrieveMoviesByYearNotFound() {
        Integer year = 2999;
        stubFor(get(urlPathEqualTo(MoviesAppConstants.MOVIE_BY_YEAR_QUERY_PARAM_V1))
                .withQueryParam("year", equalTo(String.valueOf(year)))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("404-movie-year.json")
                ));
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMoviesByYear(year));
    }

    @Test
    void shouldAddMovie() {
        var movie = new MovieInfo(null, "The Matrix", "Keanu Reeves",
                LocalDate.of(1999, 3, 24), 1999);
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
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
    void shouldAddMovieResponseTemplate() {
        var movie = new MovieInfo(null, "The Matrix", "Keanu Reeves",
                LocalDate.of(1999, 3, 24), 1999);
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
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
    void shouldAddMovieBadRequest() {

        var movie = new MovieInfo(null, null, "Keanu Reeves",
                LocalDate.of(1999, 3, 24), 1999);
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
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
    void shouldUpdateMovie() {
        Integer movieId = 3;
        var cast = "ABC";
        var movie = MovieInfo.builder().cast(cast).build();
        stubFor(put(urlPathMatching("/movies/v1/movie_infos/[0-9]+"))
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
    void shouldUpdateMovieNotFound() {
        Integer movieId = 999;
        var cast = "ABC";
        var movie = MovieInfo.builder().cast(cast).build();
        stubFor(put(urlPathMatching("/movies/v1/movie_infos/[0-9]+"))
                .withRequestBody(matchingJsonPath("$.cast", containing(cast)))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                ));
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.updateMovie(movieId, movie));
    }

    @Test
    void shouldDeleteMovie() {
        var movie = new MovieInfo(null, "The Matrix", "Keanu Reeves",
                LocalDate.of(1999, 3, 24), 1999);
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("The Matrix")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Keanu")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("add-movie.json")
                ));

        var addedMovie = moviesRestClient.addMovie(movie);
        var expectedErrorMessage = "Movie Deleted Successfully";
        stubFor(delete(urlPathMatching("/movies/v1/movie_infos/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedErrorMessage)
                ));

        var responseMessage = moviesRestClient.deleteMovie(addedMovie.getMovieInfoId());

        assertEquals(expectedErrorMessage, responseMessage);
    }

    @Test
    void shouldDeleteMovieNotFound() {
        Long movieId = 99L;
        stubFor(delete(urlPathMatching("/movies/v1/movie_infos/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                ));
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.deleteMovie(movieId));
    }

    @Test
    void shouldDeleteMovieByName() {
        var movie = new MovieInfo(null, "The Matrix", "Keanu Reeves",
                LocalDate.of(1999, 3, 24), 1999);
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("The Matrix")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Keanu")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("add-movie.json")
                ));

        var addedMovie = moviesRestClient.addMovie(movie);
        var expectedErrorMessage = "Movie Deleted Successfully";
        stubFor(delete(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(addedMovie.getName()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                ));

        var responseMessage = moviesRestClient.deleteMovieByName(addedMovie.getName());

        assertEquals(expectedErrorMessage, responseMessage);


        verify(exactly(1), postRequestedFor(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("The Matrix")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Keanu"))));

        verify(exactly(1), deleteRequestedFor(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(addedMovie.getName())));
    }

    @Test
    @Disabled("Need to spin up an actual service")
    void shouldDeleteMovieByNameWithSelectiveProxy() {
        var movie = new MovieInfo(null, "The Matrix", "Keanu Reeves",
                LocalDate.of(1999, 3, 24), 1999);

        stubFor(any(anyUrl()).willReturn(aResponse().proxiedFrom("http://localhost:8080")));

        var addedMovie = moviesRestClient.addMovie(movie);
        var expectedErrorMessage = "Movie Deleted Successfully";
        stubFor(delete(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(addedMovie.getName()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                ));

        var responseMessage = moviesRestClient.deleteMovieByName(addedMovie.getName());

        assertEquals(expectedErrorMessage, responseMessage);

        verify(exactly(1), deleteRequestedFor(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(addedMovie.getName())));
    }
}
