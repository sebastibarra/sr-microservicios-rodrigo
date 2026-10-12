package com.reservas.ms_reservas.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Carga {@code .env} del microservicio o credenciales compartidas desde {@code ms-users}. */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Path envFile = resolveEnvFile();
		if (envFile == null) {
			return;
		}

		Dotenv dotenv = Dotenv.configure()
				.directory(envFile.getParent().toString())
				.filename(envFile.getFileName().toString())
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();

		Map<String, Object> values = new HashMap<>();
        dotenv.entries().forEach(entry -> {
            if (shouldLoadFromDotenv(entry.getKey())) {
                values.put(entry.getKey(), entry.getValue());
            }
        });

		if (!values.isEmpty()) {
			environment.getPropertySources().addFirst(new MapPropertySource("ms-reservas-dotenv", values));
		}
	}

    private static boolean shouldLoadFromDotenv(String key) {
        String fromOs = System.getenv(key);
        if (fromOs == null || fromOs.isBlank()) {
            return true;
        }
        return fromOs.startsWith("${") && fromOs.endsWith("}");
    }

    private static Path resolveEnvFile() {
		String userDir = System.getProperty("user.dir");
		List<Path> candidates = List.of(
				Path.of(userDir, ".env"),
				Path.of(userDir).resolve("..").resolve("..").resolve("ms-users").resolve(".env").normalize()
		);
		return candidates.stream()
				.filter(Files::isRegularFile)
				.findFirst()
				.orElse(null);
	}
}
