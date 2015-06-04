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
package com.ejwa.mavengitdepplugin.util;

import java.io.File;
import java.io.IOException;

final public class DirectoryHandler {
	private DirectoryHandler() {
	}

	public static void delete(File path) throws IOException {
		if (path.isDirectory()) {
			for (File f : path.listFiles()) {
				delete(f);
			}
		}

		if (!path.delete()) {
			throw new IOException("Failed to delete: " + path);
		}
	}
}
