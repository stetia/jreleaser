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
package org.jreleaser.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
abstract class AbstractAssembler implements Assembler {
    protected final Set<Artifact> output = new LinkedHashSet<>();
    private final Map<String, String> extraProperties = new LinkedHashMap<>();
    private final Java java = new Java();
    private final String type;
    protected String name;
    protected boolean enabled;
    protected Active active;
    protected String executable;
    protected String templateDirectory;

    protected AbstractAssembler(String type) {
        this.type = type;
    }

    void setAll(AbstractAssembler assembler) {
        this.active = assembler.active;
        this.enabled = assembler.enabled;
        this.name = assembler.name;
        this.executable = assembler.executable;
        this.templateDirectory = assembler.templateDirectory;
        setOutputs(assembler.output);
        setJava(assembler.java);
        setExtraProperties(assembler.extraProperties);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            active = Active.NEVER;
        }
        enabled = active.check(project);
        return enabled;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getExecutable() {
        return executable;
    }

    @Override
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    @Override
    public String getTemplateDirectory() {
        return templateDirectory;
    }

    @Override
    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        this.active = Active.of(str);
    }

    @Override
    public boolean isActiveSet() {
        return active != null;
    }

    @Override
    public Set<Artifact> getOutputs() {
        return output;
    }

    @Override
    public void setOutputs(Set<Artifact> output) {
        this.output.clear();
        this.output.addAll(output);
    }

    @Override
    public void addOutput(Artifact artifact) {
        if (null != artifact) {
            this.output.add(artifact);
        }
    }

    @Override
    public Map<String, String> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, String> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public Java getJava() {
        return java;
    }

    @Override
    public void setJava(Java java) {
        this.java.setAll(java);
    }

    @Override
    public void addExtraProperties(Map<String, String> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public String getPrefix() {
        return getType();
    }

    @Override
    public final Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", active);
        props.put("executable", executable);
        props.put("templateDirectory", templateDirectory);
        asMap(full, props);
        props.put("extraProperties", getResolvedExtraProperties());
        if (java.isEnabled()) {
            props.put("java", java.asMap(full));
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getName(), props);
        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);
}