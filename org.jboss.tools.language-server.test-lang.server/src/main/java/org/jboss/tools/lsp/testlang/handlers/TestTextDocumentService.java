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
import java.util.Arrays;
import java.util.Collections;
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
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
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
import org.eclipse.lsp4j.jsonrpc.messages.Either;
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

	private final AbstractCommand[] commands = new AbstractCommand[] {
	    new AbstractCommand("window/showMessageNotification:(?<type>\\w+):(?<message>.+)") {
			
			@Override
			public void execute(String[] groups, TextDocumentIdentifier document) {
				final MessageType type = MessageType.valueOf(groups[0]);
				final String message = groups[1].trim();
				LOGGER.info("Found line starting with 'window/showMessageNotification' keyword:\n{} (type={})", message,
						type.toString().toLowerCase());
				testLanguageServer.sendShowMessageNotification(type, message);
			}
		},
	    
	    new AbstractCommand("window/showMessageRequest:(?<type>\\w+):(?<command>\\w+):(?<message>.+)") {
			
			@Override
			public void execute(String[] groups, TextDocumentIdentifier document) {
				final MessageType type = MessageType.valueOf(groups[0]);
				final String message = groups[2].trim();
				LOGGER.info("Found line starting with 'window/showMessageRequest' keyword:\n{} (type={})", message,
						type.toString().toLowerCase());
				testLanguageServer.sendShowMessageRequest(type, message, groups[1]);
			}
		},

	    new AbstractCommand("textDocument/badWord:(?<type>\\w+):(?<word>.+):(?<message>.+)") {
			
			@Override
			public void execute(String[] groups, TextDocumentIdentifier document) {
				final DiagnosticSeverity severity = DiagnosticSeverity.valueOf(groups[0]);
				final String message = groups[2].trim();
				LOGGER.info("Found line starting with 'textDocument/badWord' keyword:\n{} (type={})", message,
						severity.toString().toLowerCase());
				
				
				List<Diagnostic> diagnostics= new ArrayList<>();
				try {
					List<String> content = testLanguageServer.getDocumentManager().getContent(document.getUri());
					int l= 0;
					for (String line : content) {
						int c= 0;
						while(c < line.length()) {
							int start = line.indexOf(groups[1], c);
							if (start >= 0) {
								diagnostics.add(createDiagnostic(DiagnosticSeverity.Error, l, start, groups[1].length(), message));
								c= start+groups[1].length();
							} else {
								break;
							}
						}
						l++;
					}
				} catch (IOException | URISyntaxException e) {
					diagnostics.add(createDiagnostic(DiagnosticSeverity.Error, 0, 0, 0, "Could not compute diagnostics"));
				}
				
				testLanguageServer.publishDiagnostics(document.getUri(), diagnostics);
			}

			private Diagnostic createDiagnostic(DiagnosticSeverity severity, int line, int start, int length, String message) {
				Diagnostic diagnostic = new Diagnostic();
				diagnostic.setSeverity(severity);
				diagnostic.setRange(new Range(new Position(line, start), new Position(line, start+length)));
				diagnostic.setMessage(message);
				return diagnostic;
			}
		},

	};
	
	protected static final Pattern showMessagePattern = Pattern
			.compile("window/showMessageNotification:(?<type>\\w+):(?<message>.+)");
	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestTextDocumentService.class);
	private final TestLanguageServer testLanguageServer;

	
	public TestTextDocumentService(TestLanguageServer testLanguageServer) {
		this.testLanguageServer = testLanguageServer;
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(TextDocumentPositionParams position) {
		List<CompletionItem> items= new ArrayList<>();
		for (int i= 0; i < 6; i++) {
			items.add(createCompletionItem("TestItem "+i));
		}
		
		
		return CompletableFuture.completedFuture(Either.forLeft(items));
	}

	private CompletionItem createCompletionItem(String itemText) {
		CompletionItem i= new CompletionItem();
		i.setDetail(itemText+" Detail");
		i.setInsertText(itemText);
		i.setLabel("Label for "+itemText);
		i.setCommand(new Command("TestCommand"+itemText, "TestCommand", Arrays.asList(itemText)));
		return i;
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		return CompletableFuture.completedFuture(createCompletionItem(unresolved.getInsertText()+" resolved"));
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
		Range range = new Range(position.getPosition(), new Position(position.getPosition().getLine(), position.getPosition().getCharacter()+1));
		return CompletableFuture.completedFuture(new Hover(Arrays.asList(Either.forLeft("First element"), Either.forLeft("Second element")), range));
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		return CompletableFuture.completedFuture(Collections.emptyList());
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
		LOGGER.debug("Looking for word at Position {} in '{}'", selectionPosition, selectedLine);
		
		int firstChar= selectionPosition;
		while (firstChar > 0 && Character.isAlphabetic(selectedLine.charAt(firstChar-1))) {
			firstChar--;
		}
		LOGGER.debug("First char: {}", firstChar);
		int afterLastChar= selectionPosition;
		while (afterLastChar < selectedLine.length() && Character.isAlphabetic(selectedLine.charAt(afterLastChar))) {
			afterLastChar++;
		}
		LOGGER.debug("After last char: {}", afterLastChar);
		String word = selectedLine.substring(firstChar, afterLastChar);
		LOGGER.debug("Found word: {}", word);
		return word;
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		LOGGER.info("Handling document saved");
		final DocumentManager documentManager = testLanguageServer.getDocumentManager();
		final String documentUri = params.getTextDocument().getUri();
		try {
			final List<String> lines = documentManager.getContent(documentUri);
			LOGGER.info("Document saved: \n{}", lines.stream().collect(Collectors.joining("\n")));
			for (String line : lines) {
				for (AbstractCommand command : commands) {
					if (command.maybeExecute(params.getTextDocument(), line)) {
						break;
					}
				}
			}
		} catch (IOException | URISyntaxException e) {
			LOGGER.error("Failed to read document content at " + documentUri, e);
		}
	}

}
