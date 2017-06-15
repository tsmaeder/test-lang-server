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

import com.google.common.io.LineReader;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.jboss.tools.lsp.testlang.handlers.TestTextDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        StringBuilder b= new StringBuilder();
        if (contents != null) {
            for (int i = 0; i < contents.length(); i++) {
                if (contents.charAt(i) == '\r') {
                    if (contents.charAt(i+1) == '\n') {
                        i++;
                    }
                    result.add(b.toString());
                    b= new StringBuilder();
                } else if (contents.charAt(i) == '\n') {
                    result.add(b.toString());
                    b= new StringBuilder();
                } else {
                    b.append(contents.charAt(i));
                }
            }
            result.add(b.toString());
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
        String newLine = firstLine.substring(0, range.getStart().getCharacter()) + lastLine.substring(range.getEnd().getCharacter());
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

    public String getWordAtPosition(TextDocumentPositionParams location) throws IOException, URISyntaxException {
        final List<String> lines = getContent(location.getTextDocument().getUri());
        final String selectedLine = lines.get(location.getPosition().getLine());
        // find the selected word
        return DocumentManager.findSelectedWord(location.getPosition().getCharacter(), selectedLine);
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
