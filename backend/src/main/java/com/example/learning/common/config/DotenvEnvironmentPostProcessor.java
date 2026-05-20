package com.example.learning.common.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.StandardEnvironment;

/**
 * Loads {@code .env} into the environment when keys are missing or blank (e.g. IDE run without
 * exporting variables). Does not override non-blank OS environment variables.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String SOURCE_NAME = "dotenvFile";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getPropertySources().contains(SOURCE_NAME)) {
            return;
        }
        if (environment.acceptsProfiles(Profiles.of("test"))) {
            return;
        }

        String userDir = System.getProperty("user.dir", ".");
        List<Path> candidates = List.of(
                Path.of(userDir, ".env"),
                Path.of(userDir, "backend", ".env")
        );

        Map<String, Object> fromFile = new LinkedHashMap<>();
        for (Path path : candidates) {
            if (Files.isRegularFile(path)) {
                mergeFile(path, fromFile);
            }
        }
        if (fromFile.isEmpty()) {
            return;
        }

        Map<String, Object> toRegister = new LinkedHashMap<>();
        fromFile.forEach((key, value) -> {
            String existing = environment.getProperty(key);
            if (existing == null || existing.isBlank()) {
                toRegister.put(key, value);
            }
        });
        if (toRegister.isEmpty()) {
            return;
        }

        environment.getPropertySources()
                .addAfter(
                        StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        new MapPropertySource(SOURCE_NAME, toRegister)
                );
    }

    private static void mergeFile(Path path, Map<String, Object> target) {
        try {
            for (String rawLine : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String line = rawLine.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("export ")) {
                    line = line.substring("export ".length()).strip();
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).strip();
                String value = line.substring(eq + 1).strip();
                value = stripQuotes(value);
                if (!key.isEmpty()) {
                    target.put(key, value);
                }
            }
        } catch (IOException ignored) {
            // optional file
        }
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
