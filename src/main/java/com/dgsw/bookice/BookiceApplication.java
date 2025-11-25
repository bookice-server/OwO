package com.dgsw.bookice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BookiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookiceApplication.class, args);
    }

}
