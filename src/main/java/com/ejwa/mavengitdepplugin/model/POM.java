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
package com.ejwa.mavengitdepplugin.model;

import com.ejwa.mavengitdepplugin.GitDependency;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.maven.plugin.logging.Log;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class POM {
	private final File file;
	private final Document document;
	private final Element project;
	private final Namespace ns;

	public POM(File pomFile) throws IOException, JDOMException {
		final SAXBuilder builder = new SAXBuilder();
		final Document doc = builder.build(pomFile);

		project = doc.getRootElement();
		ns = project.getNamespace();
		document = doc;
		file = pomFile;
	}

	public Document getDocument() {
		return document;
	}

	public File getFile() {
		return file;
	}

	public String getGroupId() {
		return project.getChild("groupId", ns).getTextTrim();
	}

	public String getArtifactId() {
		return project.getChild("artifactId", ns).getTextTrim();
	}

	public String getParentVersion() {
		return project.getChild("parent", ns).getChild("version", ns).getTextTrim();
	}

	public void setParentVersion(String version) {
		final Element parent = project.getChild("parent", ns);

		if (parent != null) {
			parent.getChild("version", ns).setText(version);
		}
	}

	public String getVersion() {
		return project.getChild("version", ns).getTextTrim();
	}

	public void setVersion(String version) {
		project.getChild("version", ns).setText(version);
	}

	public String getDependencyVersion(GitDependency dependency) {
		final List<Element> dependencies = project.getChild("dependencies", ns).getChildren("dependency", ns);

		for (Element e : dependencies) {
			final String groupIdFound = e.getChild("groupId", ns).getTextTrim();
			final String artifactIdFound = e.getChild("artifactId", ns).getTextTrim();

			if (dependency.getGroupId().equals(groupIdFound) && dependency.getArtifactId().equals(artifactIdFound)) {
				return e.getChild("version", ns).getTextTrim();
			}
		}

		throw new IllegalStateException("Failed to find version tag for the given dependency.");
	}

	public void setDependencyVersion(GitDependency dependency, String version) {
		final Element dependenciesElement = project.getChild("dependencies", ns);

		if (dependenciesElement != null) {
			final List<Element> dependencies = dependenciesElement.getChildren("dependency", ns);

			for (Element e : dependencies) {
				final String groupIdFound = e.getChild("groupId", ns).getTextTrim();
				final String artifactIdFound = e.getChild("artifactId", ns).getTextTrim();

				if (dependency.getGroupId().equals(groupIdFound) &&
				    dependency.getArtifactId().equals(artifactIdFound)) {
					e.getChild("version", ns).setText(version);
				}
			}
		}
	}

	@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
	public static POM getProjectPOM(Log log) {
		final POM pom;

		try {
			pom = new POM(new File("pom.xml"));
		} catch (Exception ex) {
			log.error(ex);
			throw new IllegalStateException("Failed to process POM file.");
		}

		return pom;
	}
}
