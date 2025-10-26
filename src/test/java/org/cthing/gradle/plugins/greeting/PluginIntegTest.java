/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.greeting;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
        copyProject("project");

        final BuildResult result = createGradleRunner(gradleVersion).build();
        verifyBuild(result);
    }

    @SuppressWarnings("SameParameterValue")
    private void copyProject(final String projectName) throws IOException {
        final URL projectUrl = getClass().getResource("/" + projectName);
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

    private void verifyBuild(final BuildResult result) {
        final BuildTask helloTask = result.task(":hello");
        assertThat(helloTask).isNotNull();
        assertThat(helloTask.getOutcome()).as(result.getOutput()).isEqualTo(SUCCESS);
    }
}
