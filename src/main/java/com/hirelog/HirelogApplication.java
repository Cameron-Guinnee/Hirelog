package com.hirelog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HirelogApplication {

    public static void main(String[] args) {
        SpringApplication.run(HirelogApplication.class, args);
    }
}
