/*
 * Copyright © 2011 Ejwa Software. All rights reserved.
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
package com.ejwa.mavengitdepplugin;

import com.ejwa.mavengitdepplugin.model.Directory;
import com.ejwa.mavengitdepplugin.model.POM;
import com.ejwa.mavengitdepplugin.util.POMHandler;
import java.io.File;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which compiles and installs previously downloaded
 * dependencies.
 *
 * @goal prepare
 */
public class PreparerMojo extends AbstractMojo {

	/**
	 * A list of git dependencies... These controll how to fetch git
	 * dependencies from an external source.
	 *
	 * @parameter
	 */
	private List<GitDependency> gitDependencies;

	/*
	 * Prepares the project POM files and makes them ready for the next
	 * goal in the build lifecycle. Right now, this simply means modifying
	 * the version to correlate with the checked out git hash version.
	 * The pom files are only modified if the groupId matches and the
	 * artifactId has a substring match.
	 */
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	private void prepare(POM pom, GitDependency dependency) {
		final String version = pom.getDependencyVersion(dependency);
		final String tempDirectory = Directory.getTempDirectoryString(dependency.getLocation(), version);
		try {
			final List<POM> dpoms = POMHandler.locate(new File(tempDirectory), dependency);

			for (POM p : dpoms) {
				p.setVersion(version);
				p.setParentVersion(version);
				p.setDependencyVersion(dependency, version);
				new POMHandler(p).write();
			}
		} catch (Exception ex) {
			getLog().error(ex);
		}
	}

	public void execute() throws MojoExecutionException {
		for (GitDependency d : gitDependencies) {
			final POM pom = POM.getProjectPOM(getLog());
			prepare(pom, d);
		}
	}
}
