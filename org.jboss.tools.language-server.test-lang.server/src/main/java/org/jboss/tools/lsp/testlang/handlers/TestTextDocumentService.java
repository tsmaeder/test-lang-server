/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.jboss.tools.lsp.testlang.handlers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.jboss.tools.lsp.testlang.DocumentManager;
import org.jboss.tools.lsp.testlang.TestLanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the TextDocumentService
 * @author Thomas Mäder
 *
 */
public class TestTextDocumentService implements TextDocumentService {

	protected static final Pattern showMessagePattern = Pattern
			.compile("window/showMessage:(?<type>\\w+):(?<message>.+)");
	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestTextDocumentService.class);
	private final TestLanguageServer testLanguageServer;

	public TestTextDocumentService(TestLanguageServer testLanguageServer) {
		this.testLanguageServer = testLanguageServer;
	}

	@Override
	public CompletableFuture<CompletionList> completion(TextDocumentPositionParams position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
		return CompletableFuture.supplyAsync(new Supplier<List<? extends DocumentHighlight>>() {

			@Override
			public List<? extends DocumentHighlight> get() {
				LOGGER.info("Handling document highlight request: " + position);
				final List<DocumentHighlight> highlights = new ArrayList<>();
				final TextDocumentIdentifier textDocumentIdentifier = position.getTextDocument();
				// look-up the document from the given textDocumentIdentifier
				final DocumentManager documentManager = testLanguageServer.getDocumentManager();
				final String documentUri = textDocumentIdentifier.getUri();
				// identify the selected text
				final Position selectedPosition = position.getPosition();
				try {
					final List<String> lines = documentManager.getContent(documentUri);
					final String selectedLine = lines.get(selectedPosition.getLine());
					// find the selected word
					final String selectedWord = findSelectedWord(selectedPosition.getCharacter(), selectedLine);
					IntStream.range(0, lines.size()).forEach(lineNumber -> {
						int index= 0;
						final String line = lines.get(lineNumber);
						while((index = line.indexOf(selectedWord, index)) != -1) {
							// in this implementation, the kind of highlight will always be 'Text'
							// (1)
							highlights.add(new DocumentHighlight(
											new Range(new Position(lineNumber, index), new Position(lineNumber, index + selectedWord.length()-1))
											, DocumentHighlightKind.Text));
							index += selectedWord.length();
						}
					});
					return highlights;
				} catch (IOException | URISyntaxException e) {
					throw new RuntimeException(e);
				}			
			}
		});
		

	}

	public static String findSelectedWord(final int selectionPosition, final String selectedLine) {
		// move backward until non alphabetic character is found, or begin of
		// line is reached
		int currentPosition = selectionPosition;
		while (currentPosition > 0 && Character.isAlphabetic(selectedLine.charAt(currentPosition))) {
			LOGGER.trace("Character ''{}'' (position {}) is alphabetic: {}", selectedLine.charAt(currentPosition),
					currentPosition, Character.isAlphabetic(selectedLine.charAt(currentPosition)));
			currentPosition--;
		}
		final int beginPosition = currentPosition < 0 || !Character.isAlphabetic(selectedLine.charAt(currentPosition))
				? currentPosition + 1 : currentPosition;
		// move forward until non alphabetic character is found, or end of line
		// is reached
		currentPosition = selectionPosition;
		while (currentPosition < selectedLine.length()
				&& Character.isAlphabetic(selectedLine.charAt(currentPosition))) {
			LOGGER.trace("Character ''{}'' (position {}) is alphabetic: {}", selectedLine.charAt(currentPosition),
					currentPosition, Character.isAlphabetic(selectedLine.charAt(currentPosition)));
			currentPosition++;
		}
		final int endPosition = currentPosition;
		final String result = selectedLine.substring(beginPosition, endPosition);
		LOGGER.trace("Result: ''{}'' (position {}-{})", result, beginPosition, endPosition);
		return result;
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		LOGGER.info("Handling document saved");
		final DocumentManager documentManager = testLanguageServer.getDocumentManager();
		final String documentUri = params.getTextDocument().getUri();
		try {
			final List<String> lines = documentManager.getContent(documentUri);
			LOGGER.info("Document saved: \n{}", lines.stream().collect(Collectors.joining("\n")));
			// iterate on lines with a counter
			lines.stream().map(line -> showMessagePattern.matcher(line)).filter(matcher -> matcher.matches())
					.forEach(matcher -> {
						final MessageType type = MessageType.valueOf(matcher.group(1));
						final String message = matcher.group(2).trim();
						LOGGER.info("Found line starting with 'window/showMessage' keyword:\n{} (type={})", message,
								type.toString().toLowerCase());
						this.testLanguageServer.sendShowMessageNotification(type, message);
					});
		} catch (IOException | URISyntaxException e) {
			LOGGER.error("Failed to read document content at " + documentUri, e);
		}
	}

}
