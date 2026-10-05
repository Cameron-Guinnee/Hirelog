package com.hirelog.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Credentials for the single Hirelog user and the HMAC key used to sign tokens. */
@Validated
@ConfigurationProperties("hirelog.security")
public record SecurityProperties(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank @Size(min = 32, message = "must be at least 32 characters") String jwtSecret,
        @NotNull Duration tokenTtl) {
}
