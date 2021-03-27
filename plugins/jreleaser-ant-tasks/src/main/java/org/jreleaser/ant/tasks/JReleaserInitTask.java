/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jreleaser.ant.tasks.internal.JReleaserLoggerAdapter;
import org.jreleaser.cli.Main;
import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.templates.TemplateUtils;
import org.jreleaser.util.JReleaserLogger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.Set;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserInitTask extends Task {
    private boolean overwrite;
    private String format;

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public void execute() throws BuildException {
        try {
            if (!getSupportedConfigFormats().contains(format)) {
                throw new BuildException("Unsupported file format. Must be one of [" +
                    String.join("|", getSupportedConfigFormats()) + "]");
            }

            Path outputDirectory = getProject().getBaseDir().toPath().normalize();
            Path outputFile = outputDirectory.resolve("jreleaser." + format);

            Reader template = TemplateUtils.resolveTemplate(getLogger(), Main.class,
                "META-INF/jreleaser/templates/jreleaser." + format + ".tpl");

            getLogger().info("Writing file " + outputFile.toAbsolutePath());
            try (Writer writer = Files.newBufferedWriter(outputFile, (overwrite ? CREATE : CREATE_NEW), WRITE, TRUNCATE_EXISTING);
                 Scanner scanner = new Scanner(template)) {
                while (scanner.hasNextLine()) {
                    writer.write(scanner.nextLine() + System.lineSeparator());
                }
            } catch (FileAlreadyExistsException e) {
                getLogger().error("File {} already exists and overwrite was set to false.", outputFile.toAbsolutePath());
                return;
            }

            getLogger().info("JReleaser initialized at " + outputDirectory.toAbsolutePath());
        } catch (IllegalStateException | IOException e) {
            throw new JReleaserException("Unexpected error", e);
        }
    }

    private JReleaserLogger getLogger() {
        return new JReleaserLoggerAdapter(getProject());
    }

    private Set<String> getSupportedConfigFormats() {
        Set<String> extensions = new LinkedHashSet<>();

        ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class,
            JReleaserConfigParser.class.getClassLoader());

        for (JReleaserConfigParser parser : parsers) {
            extensions.add(parser.getPreferredFileExtension());
        }

        return extensions;
    }
}