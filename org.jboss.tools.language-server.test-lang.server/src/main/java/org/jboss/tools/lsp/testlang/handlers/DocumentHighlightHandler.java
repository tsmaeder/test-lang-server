package org.jboss.tools.lsp.testlang.handlers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.jboss.tools.lsp.base.LSPMethods;
import org.jboss.tools.lsp.ipc.RequestHandler;
import org.jboss.tools.lsp.messages.DocumentHighlight;
import org.jboss.tools.lsp.messages.Position;
import org.jboss.tools.lsp.messages.Range;
import org.jboss.tools.lsp.messages.TextDocumentIdentifier;
import org.jboss.tools.lsp.messages.TextDocumentPositionParams;
import org.jboss.tools.lsp.testlang.DocumentManager;
import org.jboss.tools.lsp.testlang.TestLanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document Highlight request handler.
 */
public class DocumentHighlightHandler implements RequestHandler<TextDocumentPositionParams, Object> {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentHighlightHandler.class);

	private final TestLanguageServer testLanguageServer;

	/**
	 * Constructor
	 * 
	 * @param testLanguageServer
	 *            the parent {@link TestLanguageServer}
	 */
	public DocumentHighlightHandler(final TestLanguageServer testLanguageServer) {
		this.testLanguageServer = testLanguageServer;
	}

	@Override
	public boolean canHandle(final String request) {
		return LSPMethods.DOCUMENT_HIGHLIGHT.getMethod().equals(request);
	}

	@Override
	public DocumentHighlight handle(final TextDocumentPositionParams positionParams) {
		LOGGER.info("Handling document highlight request: " + positionParams);
		final List<DocumentHighlight> highlights = new ArrayList<>();
		final TextDocumentIdentifier textDocumentIdentifier = positionParams.getTextDocument();
		// look-up the document from the given textDocumentIdentifier
		final DocumentManager documentManager = testLanguageServer.getDocumentManager();
		final String documentUri = textDocumentIdentifier.getUri();
		// identify the selected text
		final Position selectedPosition = positionParams.getPosition();
		try {
			final List<String> lines = documentManager.getContent(documentUri);
			final String selectedLine = lines.get(selectedPosition.getLine().intValue());
			// find the selected word
			final String selectedWord = findSelectedWord(selectedPosition.getCharacter().intValue(), selectedLine);
			IntStream.range(0, lines.size()).forEach(lineNumber -> {
				int index= 0;
				final String line = lines.get(lineNumber);
				while((index = line.indexOf(selectedWord, index)) != -1) {
					// in this implementation, the kind of highlight will always be 'Text'
					// (1)
					highlights.add(new DocumentHighlight()
							.withKind(1)
							.withRange(
									new Range().withStart(new Position().withLine(lineNumber).withCharacter(index))
									.withEnd(new Position().withLine(lineNumber).withCharacter(index + selectedWord.length()-1))));
					index += selectedWord.length();
				}
			});
			// for now, we return a single highlight element. 
			// See https://github.com/eclipse/che/issues/1802#issuecomment-265979537 for more info
			final DocumentHighlight result = highlights.stream().filter(h -> !h.getRange().includes(selectedPosition)).findFirst().orElse(null);
			return result;
		} catch (IOException | URISyntaxException e) {
			LOGGER.error("Failed to read document content at " + documentUri, e);
			return null;
		}


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
		final int beginPosition = currentPosition < 0 || !Character.isAlphabetic(selectedLine.charAt(currentPosition)) ? currentPosition + 1 : currentPosition;
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

}
