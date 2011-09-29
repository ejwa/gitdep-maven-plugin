package com.ejwa.mavengitdepplugin.util;

import com.ejwa.mavengitdepplugin.GitDependency;
import com.ejwa.mavengitdepplugin.model.POM;
import java.util.ArrayList;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
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
	public boolean isInstalled(Log log, GitDependency dependency, POM pom) {
		final GitDependencyHandler dependencyHandler = new GitDependencyHandler(dependency);
		final String version = dependencyHandler.getDependencyVersion(pom);
		final String type = dependencyHandler.getDependencyType(pom);
		final String classifier = dependencyHandler.getDependencyClassifier(pom);

		final Artifact artifact = factory.createArtifactWithClassifier(dependency.getGroupId(), dependency.getArtifactId(),
		                                                               version, type, classifier);
		try {
			artifactResolver.resolve(artifact, new ArrayList(), local);
		} catch (ArtifactResolutionException ex) {
			throw new IllegalStateException("Failed to find artifact in local repository.", ex);
		} catch (ArtifactNotFoundException e) {
			return false;
		}

		return true;
	}
}
