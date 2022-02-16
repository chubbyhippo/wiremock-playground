package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

import static com.example.demo.MoviesAppConstants.ADD_MOVIE_V1;
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

    @Test
    void shouldRetrieveMoviesByNameResponseTemplate() {
        var movieName = "Avengers";
        stubFor(get(urlEqualTo(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName))
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
        stubFor(get(urlPathEqualTo(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1))
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
}
