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

import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.jboss.tools.lsp.testlang.handlers.TestTextDocumentService;

/**
 * Default implementation of the {@link DocumentManager} interface.
 */
public class DocumentManager {
	public List<String> getContent(String path) throws IOException, URISyntaxException {
		return Files.readAllLines(Paths.get(new URI(path)));
	}

	public String getWordAtPosition(TextDocumentPositionParams location) throws IOException, URISyntaxException {
		final List<String> lines = getContent(location.getTextDocument().getUri());
		final String selectedLine = lines.get(location.getPosition().getLine());
		// find the selected word
		return TestTextDocumentService.findSelectedWord(location.getPosition().getCharacter(), selectedLine);
	}

}
