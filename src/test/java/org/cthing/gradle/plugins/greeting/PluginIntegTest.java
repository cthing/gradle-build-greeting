/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.greeting;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.file.PathUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.params.provider.Arguments.arguments;


public class PluginIntegTest {

    private static final Path BASE_DIR = Path.of(System.getProperty("buildDir"), "integTest");
    private static final Path WORKING_DIR = Path.of(System.getProperty("projectDir"), "testkit");
    private static final Pattern GREETING_PATTERN =
            Pattern.compile("BUILDING testProject - Gradle: \\d+\\.\\d+(\\.\\d+)?, Kotlin: \\d+\\.\\d+\\.\\d+, "
                            + "Java: \\d+\\.\\d+\\.\\d+, OS: \\w+, Host: \\w+");

    static {
        try {
            Files.createDirectories(BASE_DIR);
            Files.createDirectories(WORKING_DIR);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Path projectDir;

    public static Stream<Arguments> gradleVersionProvider() {
        return Stream.of(
                arguments("8.0"),
                arguments(GradleVersion.current().getVersion())
        );
    }

    @BeforeEach
    public void setup() throws IOException {
        this.projectDir = Files.createTempDirectory(BASE_DIR, "project");
    }

    @ParameterizedTest
    @MethodSource("gradleVersionProvider")
    public void testShowGreeting(final String gradleVersion) throws IOException {
        copyProject();

        final BuildResult result = createGradleRunner(gradleVersion).build();
        final String output = result.getOutput();
        assertThat(output).containsPattern(GREETING_PATTERN);

        final BuildTask helloTask = result.task(":hello");
        assertThat(helloTask).isNotNull();
        assertThat(helloTask.getOutcome()).as(result.getOutput()).isEqualTo(SUCCESS);

    }

    @ParameterizedTest
    @MethodSource("gradleVersionProvider")
    public void testShowGreetingWithCause(final String gradleVersion) throws IOException {
        copyProject();

        final String cause = "A cause";
        final GradleRunner runner = createGradleRunner(gradleVersion);
        runner.withEnvironment(Map.of("CTHING_CI_BUILD_CAUSE", cause));
        final BuildResult result = runner.build();
        final String output = result.getOutput();
        assertThat(output).containsPattern(GREETING_PATTERN);
        assertThat(output).contains("BUILD CAUSE: " + cause);

        final BuildTask helloTask = result.task(":hello");
        assertThat(helloTask).isNotNull();
        assertThat(helloTask.getOutcome()).as(result.getOutput()).isEqualTo(SUCCESS);

    }

    private void copyProject() throws IOException {
        final URL projectUrl = getClass().getResource("/project");
        assertThat(projectUrl).isNotNull();
        PathUtils.copyDirectory(Path.of(projectUrl.getPath()), this.projectDir);
    }

    private GradleRunner createGradleRunner(final String gradleVersion) {
        return GradleRunner.create()
                           .withProjectDir(this.projectDir.toFile())
                           .withTestKitDir(WORKING_DIR.toFile())
                           .withArguments("hello")
                           .withPluginClasspath()
                           .withGradleVersion(gradleVersion);
    }
}
