/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.jboss.tools.lsp.testlang;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Interface for the Document manager
 */
public interface DocumentManager {

	/**
	 * Returns the content of the document at the given {@code uri}.
	 * 
	 * @param path
	 *            the document URI.
	 * @return the document content as a list of lines
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	public default List<String> getContent(String path) throws IOException, URISyntaxException {
		return Files.readAllLines(Paths.get(new URI(path)));
	}

}
