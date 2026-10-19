package com.kkubeurakko.kkubeurakko_be.config;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

class ApplicationProfileConfigurationTest {

	@TempDir
	Path configDirectory;

	@Test
	@DisplayName("기본 설정은 prod 대신 local 프로필을 활성화한다")
	void packagedConfigurationActivatesOnlyLocal() {
		runner("classpath:/application.yml", Map.of()).run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context.getEnvironment().getActiveProfiles()).containsExactly("local");
		});
	}

	@Test
	@DisplayName("CI에 application-local.yml이 없어도 기본 프로필 설정을 읽을 수 있다")
	void missingUntrackedLocalConfigurationDoesNotPreventProfileSelection() throws IOException {
		String location = copyApplicationConfiguration();
		assertThat(configDirectory.resolve("application-local.yml")).doesNotExist();

		runner(location, Map.of()).run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context.getEnvironment().getActiveProfiles()).containsExactly("local");
		});
	}

	@Test
	@DisplayName("local 프로필은 로컬 전용 설정을 로드하고 공통 설정보다 우선 적용한다")
	void localProfileLoadsAndOverridesSharedConfiguration() throws IOException {
		String location = copyApplicationConfiguration();
		Files.writeString(configDirectory.resolve("application.yml"),
			Files.readString(configDirectory.resolve("application.yml")) + "\nprofile-test:\n  value: shared\n");
		Files.writeString(configDirectory.resolve("application-local.yml"),
			"profile-test:\n  value: local\n  local-only: true\n");

		runner(location, Map.of()).run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context.getEnvironment().getActiveProfiles()).containsExactly("local");
			assertThat(context.getEnvironment().getProperty("profile-test.value")).isEqualTo("local");
			assertThat(context.getEnvironment().getProperty("profile-test.local-only", Boolean.class)).isTrue();
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {"test", "prod"})
	@DisplayName("명시적인 프로필 설정은 local 기본값을 대체하고 해당 설정만 로드한다")
	void explicitProfileOverridesLocalWithoutLoadingLocalSettings(String profile) throws IOException {
		String location = copyApplicationConfiguration();
		Files.writeString(configDirectory.resolve("application-local.yml"),
			"profile-test:\n  local-only: true\n");
		Files.writeString(configDirectory.resolve("application-" + profile + ".yml"),
			"profile-test:\n  value: " + profile + "\n");

		runner(location, Map.of()).withPropertyValues("spring.profiles.active=" + profile).run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context.getEnvironment().getActiveProfiles()).containsExactly(profile);
			assertThat(context.getEnvironment().getProperty("profile-test.value")).isEqualTo(profile);
			assertThat(context.getEnvironment().containsProperty("profile-test.local-only")).isFalse();
		});
	}

	@Test
	@DisplayName("SPRING_PROFILES_ACTIVE 환경 변수는 local 기본값을 대체한다")
	void environmentVariableOverridesDefaultProfile() throws IOException {
		String location = copyApplicationConfiguration();

		runner(location, Map.of("SPRING_PROFILES_ACTIVE", "prod")).run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context.getEnvironment().getActiveProfiles()).containsExactly("prod");
		});
	}

	private String copyApplicationConfiguration() throws IOException {
		Path application = configDirectory.resolve("application.yml");
		try (InputStream input = getClass().getResourceAsStream("/application.yml")) {
			assertThat(input).as("packaged application.yml").isNotNull();
			Files.copy(input, application);
		}
		return application.toUri().toString();
	}

	private ApplicationContextRunner runner(String location, Map<String, Object> environmentVariables) {
		// Load real Spring config data without scanning application beans or connecting to services.
		return new ApplicationContextRunner()
			.withPropertyValues("spring.config.location=" + location)
			.withInitializer(context -> {
				var sources = context.getEnvironment().getPropertySources();
				// Keep developer/CI machine settings from changing the profiles under test.
				sources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
				sources.remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
				sources.addLast(new SystemEnvironmentPropertySource("testEnvironment", environmentVariables));
			})
			.withInitializer(new ConfigDataApplicationContextInitializer());
	}
}
