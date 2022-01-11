package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @JsonProperty("movie_id")
    private Integer movieId;
    private String name;
    private String cast;
    @JsonProperty("release_date")
    private LocalDate releaseDate;
    private Integer year;

}
