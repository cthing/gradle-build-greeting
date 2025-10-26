/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.greeting;

import java.lang.management.ManagementFactory;
import java.util.Optional;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import kotlin.KotlinVersion;


/**
 * Displays build environment information at the start of a build. The following
 * information is provided:
 * <ul>
 *     <li>Project name</li>
 *     <li>Gradle version</li>
 *     <li>Kotlin version</li>
 *     <li>Java version</li>
 *     <li>Operating system name</li>
 *     <li>Host name</li>
 * </ul>
 */
public class BuildGreetingPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        if (!project.equals(project.getRootProject())) {
            return;
        }

        final Logger logger = project.getLogger();

        logger.lifecycle("\nBUILDING {} - Gradle: {}, Kotlin: {}, Java: {}, OS: {}, Host: {}\n",
                         project.getName(),
                         project.getGradle().getGradleVersion(),
                         KotlinVersion.CURRENT,
                         System.getProperty("java.version"),
                         System.getProperty("os.name"),
                         getHostName());

        getBuildCause().ifPresent(cause -> logger.lifecycle("BUILD CAUSE: {}", cause));
    }

    /**
     * Determines the reason a build was started. The {@code CTHING_CI_BUILD_CAUSE} environment variable
     * is checked first. The value of that variable is returned if it is defined and not blank. Otherwise,
     * the value of the {@code BUILD_CAUSE} environment variable is returned if it is defined and not blank.
     * Otherwise, an empty optional is returned.
     *
     * @return Build cause or an empty optional.
     */
    static Optional<String> getBuildCause() {
        return Optional.ofNullable(System.getenv("CTHING_CI_BUILD_CAUSE"))
                       .filter(s -> !s.isBlank())
                       .or(() -> Optional.ofNullable(System.getenv("BUILD_CAUSE")))
                       .filter(s -> !s.isBlank());
    }

    /**
     * Obtains the name of the host on which this code is executing. Does not use reverse DNS
     * to avoid timeouts.
     *
     * @return Name of host machine
     */
    static String getHostName() {
        String host = System.getenv("HOSTNAME");
        if (host == null || host.isBlank()) {
            host = System.getenv("COMPUTERNAME"); // Windows
        }
        if (host != null && !host.isBlank()) {
            return host;
        }

        final String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        final int at = runtimeName.indexOf('@');
        return at > 0 ? runtimeName.substring(at + 1) : runtimeName;
    }
}
