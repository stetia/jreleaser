/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.internal.dsl.deploy.maven

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.deploy.maven.ArtifactoryMavenDeployer
import org.jreleaser.gradle.plugin.dsl.deploy.maven.GiteaMavenDeployer
import org.jreleaser.gradle.plugin.dsl.deploy.maven.GithubMavenDeployer
import org.jreleaser.gradle.plugin.dsl.deploy.maven.GitlabMavenDeployer
import org.jreleaser.gradle.plugin.dsl.deploy.maven.Maven
import org.jreleaser.gradle.plugin.dsl.deploy.maven.Nexus2MavenDeployer
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
@CompileStatic
class MavenImpl implements Maven {
    final Property<Active> active
    final NamedDomainObjectContainer<ArtifactoryMavenDeployer> artifactory
    final NamedDomainObjectContainer<GiteaMavenDeployer> gitea
    final NamedDomainObjectContainer<GithubMavenDeployer> github
    final NamedDomainObjectContainer<GitlabMavenDeployer> gitlab
    final NamedDomainObjectContainer<Nexus2MavenDeployer> nexus2

    @Inject
    MavenImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())

        artifactory = objects.domainObjectContainer(ArtifactoryMavenDeployer, new NamedDomainObjectFactory<ArtifactoryMavenDeployer>() {
            @Override
            ArtifactoryMavenDeployer create(String name) {
                ArtifactoryMavenDeployerImpl a = objects.newInstance(ArtifactoryMavenDeployerImpl, objects)
                a.name = name
                return a
            }
        })

        gitea = objects.domainObjectContainer(GiteaMavenDeployer, new NamedDomainObjectFactory<GiteaMavenDeployer>() {
            @Override
            GiteaMavenDeployer create(String name) {
                GiteaMavenDeployerImpl a = objects.newInstance(GiteaMavenDeployerImpl, objects)
                a.name = name
                return a
            }
        })

        github = objects.domainObjectContainer(GithubMavenDeployer, new NamedDomainObjectFactory<GithubMavenDeployer>() {
            @Override
            GithubMavenDeployer create(String name) {
                GithubMavenDeployerImpl a = objects.newInstance(GithubMavenDeployerImpl, objects)
                a.name = name
                return a
            }
        })

        gitlab = objects.domainObjectContainer(GitlabMavenDeployer, new NamedDomainObjectFactory<GitlabMavenDeployer>() {
            @Override
            GitlabMavenDeployer create(String name) {
                GitlabMavenDeployerImpl a = objects.newInstance(GitlabMavenDeployerImpl, objects)
                a.name = name
                return a
            }
        })

        nexus2 = objects.domainObjectContainer(Nexus2MavenDeployer, new NamedDomainObjectFactory<Nexus2MavenDeployer>() {
            @Override
            Nexus2MavenDeployer create(String name) {
                Nexus2MavenDeployerImpl h = objects.newInstance(Nexus2MavenDeployerImpl, objects)
                h.name = name
                return h
            }
        })
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void artifactory(Action<? super NamedDomainObjectContainer<ArtifactoryMavenDeployer>> action) {
        action.execute(artifactory)
    }

    @Override
    void gitea(Action<? super NamedDomainObjectContainer<GiteaMavenDeployer>> action) {
        action.execute(gitea)
    }

    @Override
    void github(Action<? super NamedDomainObjectContainer<GithubMavenDeployer>> action) {
        action.execute(github)
    }

    @Override
    void gitlab(Action<? super NamedDomainObjectContainer<GitlabMavenDeployer>> action) {
        action.execute(gitlab)
    }

    @Override
    void nexus2(Action<? super NamedDomainObjectContainer<Nexus2MavenDeployer>> action) {
        action.execute(nexus2)
    }

    @Override
    void artifactory(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, artifactory)
    }

    @Override
    void gitea(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, gitea)
    }

    @Override
    void github(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, github)
    }

    @Override
    void gitlab(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, gitlab)
    }

    @Override
    void nexus2(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, nexus2)
    }

    @CompileDynamic
    org.jreleaser.model.internal.deploy.maven.Maven toModel() {
        org.jreleaser.model.internal.deploy.maven.Maven maven = new org.jreleaser.model.internal.deploy.maven.Maven()
        if (active.present) maven.active = active.get()

        artifactory.each { maven.addArtifactory(((ArtifactoryMavenDeployerImpl) it).toModel()) }
        gitea.each { maven.addGitea(((GiteaMavenDeployerImpl) it).toModel()) }
        github.each { maven.addGithub(((GithubMavenDeployerImpl) it).toModel()) }
        gitlab.each { maven.addGitlab(((GitlabMavenDeployerImpl) it).toModel()) }
        nexus2.each { maven.addNexus2(((Nexus2MavenDeployerImpl) it).toModel()) }

        maven
    }
}