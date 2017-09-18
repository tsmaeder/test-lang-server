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
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.jboss.tools.lsp.testlang.handlers.TestTextDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the {@link DocumentManager} interface.
 */
public class DocumentManager {
	public static final Logger LOGGER = LoggerFactory.getLogger(DocumentManager.class);

	public Map<String, List<String>> openFiles = new HashMap<String, List<String>>();

	public List<String> getContent(String uri) throws IOException, URISyntaxException {
		synchronized (openFiles) {
			if (openFiles.containsKey(uri)) {
				return Collections.unmodifiableList(openFiles.get(uri));
			}
		}
		return null;
	}

	public void didOpen(String uri, String contents) throws IOException, URISyntaxException {
		List<String> lines = parse(contents);
		synchronized (openFiles) {
			if (openFiles.containsKey(uri)) {
				throw new RuntimeException("File already open: " + uri);
			}
			openFiles.put(uri, lines);
		}
	}

	private List<String> parse(String contents) {
		ArrayList<String> result = new ArrayList<String>();
		if (contents != null) {
			try {
				Utils.parse(new StringReader(contents), (String line, Integer lineNumber)-> {
					result.add(line);
				});
			} catch (IOException e) {
				// ignore, will not happen for StringReader
			}
		}

		return result;
	}

	public void didChange(String uri, List<TextDocumentContentChangeEvent> list) {
		synchronized (openFiles) {
			if (!openFiles.containsKey(uri)) {
				throw new RuntimeException("File not open: " + uri);
			}
			List<String> content = openFiles.get(uri);
			for (TextDocumentContentChangeEvent change : list) {
				applyChange(content, change);
			}
		}
	}

	private void applyChange(List<String> content, TextDocumentContentChangeEvent change) {
		if (change.getRange() == null) {
			content.clear();
			content.addAll(parse(change.getText()));
		} else {
			removeText(content, change.getRange());
			insertText(content, change.getRange().getStart(), change.getText());
		}
	}

	private void insertText(List<String> content, Position position, String text) {
		String lines[] = text.split("\\r?\\n");
		if (lines.length == 0) {
			return;
		}
		if (content.isEmpty()) {
			content.addAll(Arrays.asList(lines));
		} else {
			String firstLine = content.get(position.getLine());
			String start = firstLine.substring(0, position.getCharacter());
			String end = firstLine.substring(position.getCharacter());
			content.set(position.getLine(), start + lines[0]);
			int currentLine = position.getLine();
			for (int i = 1; i < lines.length; i++) {
				content.add(++currentLine, lines[i]);
			}
			content.set(currentLine, content.get(currentLine) + end);
		}
	}

	private void removeText(List<String> content, Range range) {
		String firstLine = content.get(range.getStart().getLine());
		String lastLine = content.get(range.getEnd().getLine());
		String newLine = firstLine.substring(0, range.getStart().getCharacter())
				+ lastLine.substring(range.getEnd().getCharacter());
		content.set(range.getStart().getLine(), newLine);
		int indexToRemove = range.getStart().getLine() + 1;
		for (int i = indexToRemove; i <= range.getEnd().getLine(); i++) {
			content.remove(indexToRemove);
		}
	}

	public void didClose(String uri) {
		synchronized (openFiles) {
			openFiles.remove(uri);
		}
	}

	public void findInDocument(TextDocumentIdentifier document, String selectedWord, LocationConsumer f) {
		List<String> lines = openFiles.get(document.getUri());
		IntStream.range(0, lines.size()).forEach(lineNumber -> {
			int index = 0;
			final String line = lines.get(lineNumber);
			while ((index = line.indexOf(selectedWord, index)) != -1) {
				// in this implementation, the kind of highlight
				// will always be 'Text'
				// (1)
				if (!f.accept(document, new Range(new Position(lineNumber, index),
						new Position(lineNumber, index + selectedWord.length())), selectedWord)) {
					return;
				}
				;
				index += selectedWord.length();
			}
		});

	}

	public String getWordAtPosition(TextDocumentIdentifier document, Position position)
			throws IOException, URISyntaxException {
		final List<String> lines = getContent(document.getUri());
		if (position.getLine() >= lines.size()) {
		  return "";
		}
		final String selectedLine = lines.get(position.getLine());
		// find the selected word
		if (position.getCharacter() >= selectedLine.length()) {
		  return "";
		}
		return DocumentManager.findSelectedWord(position.getCharacter(), selectedLine);
	}

	public static String findSelectedWord(final int selectionPosition, final String selectedLine) {
		LOGGER.debug("Looking for word at Position {} in '{}'", selectionPosition, selectedLine);

		int firstChar = selectionPosition;
		while (firstChar > 0 && Character.isAlphabetic(selectedLine.charAt(firstChar - 1))) {
			firstChar--;
		}
		TestTextDocumentService.LOGGER.debug("First char: {}", firstChar);
		int afterLastChar = selectionPosition;
		while (afterLastChar < selectedLine.length() && Character.isAlphabetic(selectedLine.charAt(afterLastChar))) {
			afterLastChar++;
		}
		TestTextDocumentService.LOGGER.debug("After last char: {}", afterLastChar);
		String word = selectedLine.substring(firstChar, afterLastChar);
		TestTextDocumentService.LOGGER.debug("Found word: {}", word);
		return word;
	}

}
