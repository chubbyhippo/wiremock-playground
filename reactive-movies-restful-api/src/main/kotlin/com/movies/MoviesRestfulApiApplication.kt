package com.movies

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MoviesRestfulApiApplication

fun main(args: Array<String>) {
    runApplication<MoviesRestfulApiApplication>(*args)
}

