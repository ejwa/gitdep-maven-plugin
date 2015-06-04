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
import com.ejwa.gitdepmavenplugin.model.POM;
import java.util.List;
import org.jdom.Element;
import org.jdom.Namespace;

public class GitDependencyHandler {
	private final GitDependency dependency;

	public GitDependencyHandler(GitDependency dependency) {
		this.dependency = dependency;
	}

	private Element findDependencyTagElement(POM pom, String tagName) {
		final Namespace ns = pom.getProject().getNamespace();
		final Element dependenciesElement = pom.getProject().getChild("dependencies", ns);

		if (dependenciesElement != null) {
			@SuppressWarnings("unchecked")
			final List<Element> dependencies = dependenciesElement.getChildren("dependency", ns);

			for (Element e : dependencies) {
				final String groupIdFound = e.getChild("groupId", ns).getTextTrim();
				final String artifactIdFound = e.getChild("artifactId", ns).getTextTrim();
				final Element tagElement = e.getChild(tagName, ns);

				if (tagElement != null && dependency.getGroupId().equals(groupIdFound) &&
				    dependency.getArtifactId().equals(artifactIdFound)) {
					return tagElement;
				}
			}
		}

		return null;
	}

	public String getDependencyVersion(POM pom) {
		final Element versionTag = findDependencyTagElement(pom, "version");

		if (versionTag != null) {
			return versionTag.getTextTrim();
		}

		throw new IllegalStateException("Failed to find version tag for the given dependency.");
	}

	public void setDependencyVersion(POM pom, String version) {
		final Element versionTag = findDependencyTagElement(pom, "version");

		if (versionTag != null) {
			versionTag.setText(version);
		}
	}

	public String getDependencyType(POM pom) {
		final Element typeTag = findDependencyTagElement(pom, "type");
		return typeTag == null ? "jar" : typeTag.getTextTrim();
	}

	public String getDependencyClassifier(POM pom) {
		final Element classifierTag = findDependencyTagElement(pom, "classifier");
		return classifierTag == null ? "" : classifierTag.getTextTrim();
	}

}
