package com.kkubeurakko.kkubeurakko_be.config;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

class CiWorkflowTest {

	private Map<Object, Object> workflow;
	private Map<Object, Object> testJob;
	private List<Map<Object, Object>> steps;

	@BeforeEach
	void loadWorkflow() throws IOException {
		try (InputStream input = Files.newInputStream(Path.of(".github", "workflows", "cicd.yml"))) {
			workflow = mapping(new Yaml(new SafeConstructor(new LoaderOptions())).load(input));
		}
		testJob = mapping(mapping(workflow.get("jobs")).get("test"));
		steps = ((List<?>) testJob.get("steps")).stream().map(CiWorkflowTest::mapping).toList();
	}

	@ParameterizedTest
	@ValueSource(strings = {"push", "pull_request"})
	@DisplayName("main 브랜치 push와 PR은 테스트 워크플로우를 실행한다")
	void mainBranchEventsRunTests(String event) {
		// SnakeYAML uses YAML 1.1, where the unquoted GitHub Actions key 'on' is boolean true.
		Map<Object, Object> triggers = mapping(workflow.containsKey("on") ? workflow.get("on") : workflow.get(true));
		Map<Object, Object> trigger = mapping(triggers.get(event));

		assertThat(trigger.get("branches")).isEqualTo(List.of("main"));
		assertThat(trigger).doesNotContainKey("branches-ignore");
		assertThat(trigger).doesNotContainKey("paths");
		assertThat(trigger).doesNotContainKey("paths-ignore");
	}

	@Test
	@DisplayName("새 실행은 같은 ref의 이전 실행만 취소한다")
	void concurrencyCancellationIsScopedToTheSameRef() {
		Map<Object, Object> concurrency = mapping(workflow.get("concurrency"));

		assertThat(concurrency.get("group")).isEqualTo("ci-${{ github.ref }}");
		assertThat(concurrency.get("cancel-in-progress")).isEqualTo(true);
	}

	@Test
	@DisplayName("CI는 배포 없이 읽기 권한만 가진 테스트 job을 실행한다")
	void workflowContainsOnlyReadOnlyTestJob() {
		assertThat(mapping(workflow.get("jobs")).keySet()).containsExactly("test");
		assertThat(testJob.get("runs-on")).isEqualTo("ubuntu-latest");
		Map<Object, Object> permissions = mapping(testJob.get("permissions"));
		assertThat(permissions.keySet()).containsExactly("contents");
		assertThat(permissions.get("contents")).isEqualTo("read");
		assertThat(testJob).doesNotContainKey("needs");
		assertThat(workflow.toString()).doesNotContain("secrets.", "docker", "self-hosted");
	}

	@Test
	@DisplayName("소스 체크아웃과 JDK 17 및 Gradle 준비가 테스트 실행보다 먼저 수행된다")
	void preparesJavaAndGradleBeforeExecutingTests() {
		Map<Object, Object> checkout = stepUsing("actions/checkout");
		Map<Object, Object> java = stepUsing("actions/setup-java");
		Map<Object, Object> gradle = stepUsing("gradle/actions/setup-gradle");
		Map<Object, Object> javaInputs = mapping(java.get("with"));
		Map<Object, Object> runTests = testStep();

		assertThat(javaInputs.get("java-version").toString()).isEqualTo("17");
		assertThat(javaInputs.get("distribution")).isEqualTo("temurin");
		assertThat(gradle.get("uses")).isEqualTo("gradle/actions/setup-gradle@v4");
		assertThat(steps.indexOf(checkout)).isLessThan(steps.indexOf(java));
		assertThat(steps.indexOf(java)).isLessThan(steps.indexOf(gradle));
		assertThat(steps.indexOf(gradle)).isLessThan(steps.indexOf(runTests));
		assertThat(steps.subList(0, steps.indexOf(runTests)))
			.anySatisfy(step -> assertThat(step.get("run")).isEqualTo("chmod +x gradlew"));
	}

	@Test
	@DisplayName("Gradle 테스트는 생략되거나 실패가 무시되지 않는다")
	void executesTestsWithoutSkippingOrIgnoringFailures() {
		Map<Object, Object> runTests = testStep();

		assertThat(runTests.get("run")).isEqualTo("./gradlew test");
		assertThat(runTests).doesNotContainKey("if");
		assertThat(runTests.get("continue-on-error")).isIn(null, false);
		assertThat(testJob).doesNotContainKey("if");
		assertThat(testJob.get("continue-on-error")).isIn(null, false);
	}

	@Test
	@DisplayName("테스트 실패 시 Gradle 테스트 보고서를 업로드한다")
	void uploadsTheGradleReportAfterFailure() {
		Map<Object, Object> upload = stepUsing("actions/upload-artifact");
		Map<Object, Object> inputs = mapping(upload.get("with"));

		assertThat(upload.get("if")).isEqualTo("failure()");
		assertThat(inputs.get("name")).isEqualTo("test-report");
		assertThat(inputs.get("path")).isEqualTo("build/reports/tests/test");
		assertThat(steps.indexOf(upload)).isGreaterThan(steps.indexOf(testStep()));
	}

	private Map<Object, Object> testStep() {
		return steps.stream()
			.filter(step -> "Run tests".equals(step.get("name")))
			.findFirst().orElseThrow(() -> new AssertionError("Missing Gradle test step"));
	}

	private Map<Object, Object> stepUsing(String action) {
		List<Map<Object, Object>> matching = steps.stream()
			.filter(step -> step.get("uses") instanceof String uses && uses.startsWith(action + "@"))
			.toList();
		assertThat(matching).as("step using %s", action).hasSize(1);
		return matching.get(0);
	}

	@SuppressWarnings("unchecked")
	private static Map<Object, Object> mapping(Object value) {
		assertThat(value).isInstanceOf(Map.class);
		return (Map<Object, Object>) value;
	}
}
