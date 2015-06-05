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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class PomHandler {
	private final Pom pom;

	public PomHandler(Pom pom) {
		this.pom = pom;
	}

	public void write() throws IOException {
		final XMLOutputter xml = new XMLOutputter(Format.getPrettyFormat());
		final OutputStream output = new FileOutputStream(pom.getFile());

		try {
			xml.output(pom.getDocument(), output);
		} finally {
			output.close();
		}
	}

	private static void locate(List<Pom> poms, File file, GitDependency dependency) throws IOException, JDOMException {
		if ("pom.xml".equals(file.getName())) {
			final Pom pom = new Pom(file);

			if (pom.getGroupId().equals(dependency.getGroupId()) &&
			    pom.getArtifactId().contains(dependency.getArtifactId())) {
				poms.add(pom);
			}
		} else  if (file.isDirectory()) {
			final File[] files = file.listFiles();

			if (files != null) {
				for (File f : files) {
					locate(poms, f, dependency);
				}
			}
		}
	}

	/*
	 * Recursively searches for POM files that match the given dependency.
	 */
	public static List<Pom> locate(File file, GitDependency dependency) throws IOException, JDOMException {
		final List<Pom> poms = new ArrayList<>();

		locate(poms, file, dependency);
		return poms;
	}
}
