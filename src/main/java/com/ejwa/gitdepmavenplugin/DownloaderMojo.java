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

package com.ejwa.gitdepmavenplugin;

import com.ejwa.gitdepmavenplugin.model.Directory;
import com.ejwa.gitdepmavenplugin.model.Pom;
import com.ejwa.gitdepmavenplugin.util.GitDependencyHandler;
import com.ejwa.gitdepmavenplugin.util.Resolver;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CheckoutResult.Status;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Goal which downloads needed dependencies and puts them in
 * BUILD_DIR/.maven-gitdep-NAME-VERSION-tmp/.
 */
@Mojo(name = "download", aggregator = true)
@Execute(goal = "cleanup")
public class DownloaderMojo extends AbstractMojo {
	@Parameter(required = true, readonly = true)
	private ArtifactRepository localRepository;

	@Component private ArtifactResolver artifactResolver;
	@Component private ArtifactFactory artifactFactory;

	/**
	 * A list of git dependencies... These controll how to fetch git
	 * dependencies from an external source.
	 */
	@Parameter private List<GitDependency> gitDependencies;

	private Git clone(Pom pom, GitDependency dependency) throws MojoExecutionException {
		final CloneCommand c = new CloneCommand();
		final String location = dependency.getLocation();

		c.setURI(location);
		c.setCloneAllBranches(true);
		c.setProgressMonitor(new TextProgressMonitor());

		final GitDependencyHandler dependencyHandler = new GitDependencyHandler(dependency);
		final String version = dependencyHandler.getDependencyVersion(pom);
		final String tempDirectory = Directory.getTempDirectoryString(location, version);

		c.setDirectory(new File(tempDirectory));
		return c.call();
	}

	/*
	 * Checks out a given version. The version can be one of the following:
	 *
	 * SHA-1: a complete or abbreviated SHA-1
	 * short-name: a short reference name under refs/heads, refs/tags, or
	 *             refs/remotes namespace
	 *
	 * The resolve() command of JGit can support other formats, but
	 * because we want everything to work smoothly with Maven, this
	 * plugin does not support the other formats.
	 */
	private void checkout(Git git, Pom pom, GitDependency dependency) throws MojoExecutionException {
		final GitDependencyHandler dependencyHandler = new GitDependencyHandler(dependency);
		final String version = dependencyHandler.getDependencyVersion(pom);

		try {
			final Repository repository = git.getRepository();
			final ObjectId rev = repository.resolve(version);
			final RevCommit rc = new RevWalk(repository).parseCommit(rev);
			final CheckoutCommand checkout = git.checkout();

			checkout.setName("maven-gitdep-branch-" + rc.getCommitTime());
			checkout.setStartPoint(rc);
			checkout.setCreateBranch(true);
			checkout.call();

			final Status status = checkout.getResult().getStatus();

			if (status != Status.OK) {
				throw new MojoExecutionException(String.format("Invalid checkout state (%s) of dependency.", status));
			}
		} catch (IOException | InvalidRefNameException | RefAlreadyExistsException | RefNotFoundException ex) {
			throw new MojoExecutionException(String.format("Failed to check out dependency for %s.%s",
				dependency.getGroupId(), dependency.getArtifactId()), ex);
		}
	}

	@Override
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public void execute() throws MojoExecutionException {
		for (GitDependency d : gitDependencies) {
			final Pom pom = Pom.getProjectPom();
			final Resolver resolver = new Resolver(localRepository, artifactResolver, artifactFactory);

			if (!resolver.isInstalled(getLog(), d, pom)) {
				final Git git = clone(pom, d);
				checkout(git, pom, d);
			}
		}
	}
}
