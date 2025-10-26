/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.greeting;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cthing.assertj.gradle.GradleProjectAssert.assertThat;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;


public class PluginApplyTest {

    @Test
    public void testApply(@TempDir final File projectDir) {
        final Project project = ProjectBuilder.builder().withName("testProject").withProjectDir(projectDir).build();
        project.getPluginManager().apply("org.cthing.build-greeting");

        assertThat(project).hasPlugin("org.cthing.build-greeting");
    }

    @Test
    public void testBuildCause() throws Exception {
        withEnvironmentVariable("CTHING_CI_BUILD_CAUSE", null)
                .and("BUILD_CAUSE", null)
                .execute(() -> {
                    assertThat(BuildGreetingPlugin.getBuildCause()).isEmpty();
                });
        withEnvironmentVariable("CTHING_CI_BUILD_CAUSE", "  ")
                .and("BUILD_CAUSE", "  ")
                .execute(() -> {
                    assertThat(BuildGreetingPlugin.getBuildCause()).isEmpty();
                });
        withEnvironmentVariable("CTHING_CI_BUILD_CAUSE", null)
                .and("BUILD_CAUSE", "cause 1")
                .execute(() -> {
                    assertThat(BuildGreetingPlugin.getBuildCause()).hasValue("cause 1");
                });
        withEnvironmentVariable("CTHING_CI_BUILD_CAUSE", "cause 2")
                .and("BUILD_CAUSE", null)
                .execute(() -> {
                    assertThat(BuildGreetingPlugin.getBuildCause()).hasValue("cause 2");
                });
        withEnvironmentVariable("CTHING_CI_BUILD_CAUSE", "cause 2")
                .and("BUILD_CAUSE", "cause 1")
                .execute(() -> {
                    assertThat(BuildGreetingPlugin.getBuildCause()).hasValue("cause 2");
                });
        withEnvironmentVariable("CTHING_CI_BUILD_CAUSE", "    ")
                .and("BUILD_CAUSE", "cause 1")
                .execute(() -> {
                    assertThat(BuildGreetingPlugin.getBuildCause()).hasValue("cause 1");
                });
    }

    @Test
    public void testHostName() throws Exception {
        withEnvironmentVariable("HOSTNAME", null)
                .and("COMPUTERNAME", null)
                .execute(() -> {
                    assertThat(BuildGreetingPlugin.getHostName()).isNotBlank();
                });
        withEnvironmentVariable("HOSTNAME", null)
                .and("COMPUTERNAME", "machine1")
                .execute(() -> {
                    assertThat(BuildGreetingPlugin.getHostName()).isEqualTo("machine1");
                });
        withEnvironmentVariable("HOSTNAME", "machine2")
                .and("COMPUTERNAME", null)
                .execute(() -> {
                    assertThat(BuildGreetingPlugin.getHostName()).isEqualTo("machine2");
                });
        withEnvironmentVariable("HOSTNAME", "machine2")
                .and("COMPUTERNAME", "machine1")
                .execute(() -> {
                    assertThat(BuildGreetingPlugin.getHostName()).isEqualTo("machine2");
                });
        withEnvironmentVariable("HOSTNAME", "  ")
                .and("COMPUTERNAME", "machine1")
                .execute(() -> {
                    assertThat(BuildGreetingPlugin.getHostName()).isEqualTo("machine1");
                });
    }
}
