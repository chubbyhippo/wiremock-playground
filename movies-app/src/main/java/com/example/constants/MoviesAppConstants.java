package com.example.constants;

public class MoviesAppConstants {
    private MoviesAppConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String GET_ALL_MOVIES_V1 = "/movieservice/v1/allMovies";
    public static final String RETRIEVE_MOVIE_BY_ID = "/movieservice/v1/movie/{id}";
    public static final String MOVIE_BY_NAME_QUERY_PARAM_V1 = "movieservice/v1/movieName";
    public static final String MOVIE_BY_YEAR_QUERY_PARAM_V1 = "movieservice/v1/movieYear";
}
