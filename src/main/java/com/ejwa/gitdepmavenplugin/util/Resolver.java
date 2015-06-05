/*
 * Copyright © 2011-2015 Ejwa Software. All rights reserved.
 *
 * This file is part of maven-gitdep-plugin. maven-gitdep-plugin
 * enables the use of git dependencies in Maven 3.
 *
 * maven-gitdep-plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * maven-gitdep-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with maven-gitdep-plugin. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.ejwa.gitdepmavenplugin.util;

import com.ejwa.gitdepmavenplugin.GitDependency;
import com.ejwa.gitdepmavenplugin.model.Pom;
import java.util.ArrayList;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class Resolver {
	private final ArtifactRepository local;
	private final ArtifactResolver artifactResolver;
	private final ArtifactFactory factory;

	public Resolver(ArtifactRepository local, ArtifactResolver artifactResolver, ArtifactFactory factory) {
		this.local = local;
		this.artifactResolver = artifactResolver;
		this.factory = factory;
	}

	/*
	 * Checks if the project described by a given dependency is installed
	 * in the local maven repository.
	 */
	public boolean isInstalled(Log log, GitDependency dependency, Pom pom) throws MojoExecutionException {
		final GitDependencyHandler dependencyHandler = new GitDependencyHandler(dependency);
		final String version = dependencyHandler.getDependencyVersion(pom);
		final String type = dependencyHandler.getDependencyType(pom);
		final String classifier = dependencyHandler.getDependencyClassifier(pom);

		final Artifact artifact = factory.createArtifactWithClassifier(dependency.getGroupId(), dependency.getArtifactId(),
		                                                               version, type, classifier);
		try {
			artifactResolver.resolve(artifact, new ArrayList(), local);
		} catch (ArtifactResolutionException ex) {
			throw new MojoExecutionException(String.format("Failed to find artifact '%s.%s' in local repository.",
				dependency.getGroupId(), dependency.getArtifactId()), ex);
		} catch (ArtifactNotFoundException e) {
			return false;
		}

		return true;
	}
}
