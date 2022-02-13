package com.example.service;

import com.example.constants.MoviesAppConstants;
import com.example.dto.MovieInfo;
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

    public List<MovieInfo> retrieveAllMovies() {

        try {
            return webClient.get().uri(MoviesAppConstants.GET_ALL_MOVIES_V1)
                    .retrieve()
                    .bodyToFlux(MovieInfo.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in retrieveAllMovies. Status code is {} and the message is {} ",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Exception in retrieveAllMovies and the message is {} ", e.getMessage());
            throw new MovieErrorResponse(e);
        }
    }

    public MovieInfo retrieveMovieById(Integer movieId) {
        try {
            return webClient.get().uri(MoviesAppConstants.MOVIE_BY_ID_PATH_PARAM_V1, movieId)
                    .retrieve()
                    .bodyToMono(MovieInfo.class)
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

    public List<MovieInfo> retrieveMoviesByName(String name) {
        String retrieveByNameUri = UriComponentsBuilder.fromUriString(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1)
                .queryParam("movie_name", name)
                .buildAndExpand()
                .toUriString();

        try {
            return webClient.get()
                    .uri(retrieveByNameUri)
                    .retrieve()
                    .bodyToFlux(MovieInfo.class)
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

    public List<MovieInfo> retrieveMoviesByYear(Integer year) {
        String retrieveByYearUri = UriComponentsBuilder.fromUriString(MoviesAppConstants.MOVIE_BY_YEAR_QUERY_PARAM_V1)
                .queryParam("year", year)
                .buildAndExpand()
                .toUriString();

        try {
            return webClient.get()
                    .uri(retrieveByYearUri)
                    .retrieve()
                    .bodyToFlux(MovieInfo.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in retrieveMovieByYear. Status code is {} and the message is {} ",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Exception in retrieveMovieByYear and the message is {} ", e.getMessage());
            throw new MovieErrorResponse(e);
        }
    }

    public MovieInfo addMovie(MovieInfo movieInfo) {
        try {
            return webClient.post()
                    .uri(MoviesAppConstants.ADD_MOVIE_V1)
                    .bodyValue(movieInfo)
                    .retrieve()
                    .bodyToMono(MovieInfo.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in addMovie. Status code is {} and the message is {} ",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Exception in addMovie and the message is {} ", e.getMessage());
            throw new MovieErrorResponse(e);
        }
    }

    public MovieInfo updateMovie(Integer movieId, MovieInfo movieInfo) {
        try {
            return webClient.put()
                    .uri(MoviesAppConstants.MOVIE_BY_ID_PATH_PARAM_V1, movieId)
                    .bodyValue(movieInfo)
                    .retrieve()
                    .bodyToMono(MovieInfo.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in updateMovie. Status code is {} and the message is {} ",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Exception in updateMovie and the message is {} ", e.getMessage());
            throw new MovieErrorResponse(e);
        }
    }

    public String deleteMovie(Long movieId) {
        try {
            return webClient.delete()
                    .uri(MoviesAppConstants.MOVIE_BY_ID_PATH_PARAM_V1, movieId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in deleteMovie. Status code is {} and the message is {} ",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Exception in deleteMovie and the message is {} ", e.getMessage());
            throw new MovieErrorResponse(e);
        }
    }

    public String deleteMovieByName(String movieName) {
        try {

            String deleteMovieByNameURI =
                    UriComponentsBuilder.fromUriString(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1)
                    .queryParam("movie_name", movieName)
                    .buildAndExpand()
                    .toUriString();

            webClient.delete()
                    .uri(deleteMovieByNameURI)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in deleteMovie. Status code is {} and the message is {} ",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Exception in deleteMovie and the message is {} ", e.getMessage());
            throw new MovieErrorResponse(e);
        }
        return "Movie Deleted Successfully";
    }
}
