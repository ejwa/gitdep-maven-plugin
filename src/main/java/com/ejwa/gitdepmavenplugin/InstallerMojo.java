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
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Goal which compiles and installs previously downloaded
 * dependencies.
 */
@Mojo(name = "install")
public class InstallerMojo extends AbstractMojo {
	/**
	 * A list of git dependencies... These controll how to fetch git
	 * dependencies from an external source.
	 */
	@Parameter private List<GitDependency> gitDependencies;

	private void install(Pom pom, GitDependency dependency) throws MojoExecutionException {
		final GitDependencyHandler dependencyHandler = new GitDependencyHandler(dependency);
		final String version = dependencyHandler.getDependencyVersion(pom);
		final String tempDirectory = Directory.getTempDirectoryString(dependency.getLocation(), version);
		final InvocationRequest request = new DefaultInvocationRequest();
		final Invoker invoker = new DefaultInvoker();

		request.setPomFile(new File(tempDirectory + "/pom.xml"));
		request.setGoals(Collections.singletonList("install"));



		try {
			final InvocationResult result = invoker.execute(request);
			final int exitCode = result.getExitCode();

			if (exitCode != 0) {
				throw new MojoExecutionException(String.format("Build failed with exit code %d.", exitCode));
			}
		} catch (MavenInvocationException ex) {
			throw new MojoExecutionException(String.format("Invocation of install goal failed on '%s'.",
				request.getPomFileName()), ex);
		}
	}

	@Override
	public void execute() throws MojoExecutionException {
		for (GitDependency d : gitDependencies) {
			final Pom pom = Pom.getProjectPom();
			install(pom, d);
		}
	}
}
