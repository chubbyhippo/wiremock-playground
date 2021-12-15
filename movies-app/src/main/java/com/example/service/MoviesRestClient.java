package com.example.service;

import com.example.constants.MoviesAppConstants;
import com.example.dto.Movie;
import com.example.exception.MovieErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class MoviesRestClient {
    private final WebClient webClient;

    public List<Movie> retrieveAllMovies() {
        return webClient.get().uri(MoviesAppConstants.GET_ALL_MOVIES_V1)
                .retrieve()
                .bodyToFlux(Movie.class)
                .collectList()
                .block();
    }

    public Movie retrieveMovieById(Integer movieId) {
        try {
            return webClient.get().uri(MoviesAppConstants.RETRIEVE_MOVIE_BY_ID, movieId)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in retrieveMovieById. Status code is {} and the message is {} ",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Exception in retrieveMovieById and the message is {} ", e.getMessage());
            throw new MovieErrorResponse(e);
        }
    }

    public List<Movie> retrieveMoviesByName(String name) {
        String retrieveByNameUri = UriComponentsBuilder.fromUriString(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1)
                .queryParam("movie_name", name)
                .buildAndExpand()
                .toUriString();

        try {
            return webClient.get()
                    .uri(retrieveByNameUri)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in retrieveMovieByName. Status code is {} and the message is {} ",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Exception in retrieveMovieByName and the message is {} ", e.getMessage());
            throw new MovieErrorResponse(e);
        }
    }

    public List<Movie> retrieveMoviesByYear(Integer year) {
        String retrieveByYearUri = UriComponentsBuilder.fromUriString(MoviesAppConstants.MOVIE_BY_YEAR_QUERY_PARAM_V1)
                .queryParam("year", year)
                .buildAndExpand()
                .toUriString();

        try {
            return webClient.get()
                    .uri(retrieveByYearUri)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in retrieveMovieById. Status code is {} and the message is {} ",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Exception in retrieveMovieById and the message is {} ", e.getMessage());
            throw new MovieErrorResponse(e);
        }
    }
}
