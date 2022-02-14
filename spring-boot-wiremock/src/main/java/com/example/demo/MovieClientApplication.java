package com.example.demo;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@SpringBootApplication
public class MovieClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieClientApplication.class, args);
    }

    @Value("${movieapp.baseUrl}")
    private String baseUrl;

    @Bean
    public WebClient webClient() {
        var httpClient = HttpClient.create()
                .doOnConnected(connection -> connection
                        .addHandler(new ReadTimeoutHandler(5))
                        .addHandler(new WriteTimeoutHandler(5)));
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
