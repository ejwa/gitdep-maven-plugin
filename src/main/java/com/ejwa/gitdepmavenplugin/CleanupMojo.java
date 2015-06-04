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
import com.ejwa.gitdepmavenplugin.model.POM;
import com.ejwa.gitdepmavenplugin.util.DirectoryHandler;
import com.ejwa.gitdepmavenplugin.util.GitDependencyHandler;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which cleans up the activities of maven-gitdep-plugin.
 *
 * @goal cleanup
 */
public class CleanupMojo extends AbstractMojo {

	/**
	 * A list of git dependencies... These controll how to fetch git
	 * dependencies from an external source.
	 *
	 * @parameter
	 */
	private List<GitDependency> gitDependencies;

	private void cleanup(POM pom, GitDependency dependency) {
		final GitDependencyHandler dependencyHandler = new GitDependencyHandler(dependency);
		final String version = dependencyHandler.getDependencyVersion(pom);
		final String tempDirectory = Directory.getTempDirectoryString(dependency.getLocation(), version);
		final File file = new File(tempDirectory);

		if (file.exists()) {
			try {
				DirectoryHandler.delete(new File(tempDirectory));
			} catch (IOException ex) {
				getLog().error(ex);
			}
		}
	}

	public void execute() throws MojoExecutionException {
		for (GitDependency d : gitDependencies) {
			final POM pom = POM.getProjectPOM(getLog());
			cleanup(pom, d);
		}
	}
}
