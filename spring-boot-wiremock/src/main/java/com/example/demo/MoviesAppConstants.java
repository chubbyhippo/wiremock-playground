package com.example.demo;

public class MoviesAppConstants {
    private MoviesAppConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String GET_ALL_MOVIES_V1 = "/movies/v1/movie_infos";
    public static final String MOVIE_BY_ID_PATH_PARAM_V1 = "/movies/v1/movie_infos/{id}";
    public static final String MOVIE_BY_NAME_QUERY_PARAM_V1 = "/movies/v1/movie_infos/movieName";
    public static final String MOVIE_BY_YEAR_QUERY_PARAM_V1 = "/movies/v1/movie_infos/movieYear";
    public static final String ADD_MOVIE_V1 = "/movies/v1/movie_infos";
}
