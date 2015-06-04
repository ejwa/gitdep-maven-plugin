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
package com.ejwa.gitdepmavenplugin.model;

import java.io.File;
import java.io.IOException;
import lombok.Getter;
import org.apache.maven.plugin.logging.Log;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class POM {
	@Getter private final File file;
	@Getter private final Document document;
	@Getter private final Element project;
	private final Namespace ns;

	public POM(File pomFile) throws IOException, JDOMException {
		final SAXBuilder builder = new SAXBuilder();
		final Document doc = builder.build(pomFile);

		project = doc.getRootElement();
		ns = project.getNamespace();
		document = doc;
		file = pomFile;
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
