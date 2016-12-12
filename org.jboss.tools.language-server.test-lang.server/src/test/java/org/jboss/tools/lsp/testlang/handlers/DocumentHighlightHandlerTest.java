package org.jboss.tools.lsp.testlang.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jboss.tools.lsp.messages.DocumentHighlight;
import org.jboss.tools.lsp.messages.Position;
import org.jboss.tools.lsp.messages.Range;
import org.jboss.tools.lsp.messages.TextDocumentIdentifier;
import org.jboss.tools.lsp.messages.TextDocumentPositionParams;
import org.jboss.tools.lsp.testlang.DocumentManager;
import org.jboss.tools.lsp.testlang.TestLanguageServer;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Testing the {@link DocumentHighlightHandler} 
 */
public class DocumentHighlightHandlerTest {

	
	/** The usual Logger.*/
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentHighlightHandlerTest.class);
	
	
	@Test
	public void shouldFindSelectedWord() {
		// given
		final List<String> lines = Arrays.asList("Foo", " Foo ", "Foo Bar Baz", "Bar Foo Baz", "Bar Baz Foo");
		final int[] positions = {1, 1, 0, 4, 8};
		// when
		final List<String> result = IntStream.range(0, 5).mapToObj(i -> DocumentHighlightHandler.findSelectedWord(positions[i], lines.get(i))).collect(Collectors.toList());
		// then
		LOGGER.debug("Result: {}", result.stream().map(r -> "'" + r + "'").collect(Collectors.joining(", ")));
		assertThat(result.stream().allMatch(w -> w.equals("Foo"))).isTrue();
		
		
	}
	@Test
	public void shouldHighlightText() throws IOException, URISyntaxException {
		// given
		final TestLanguageServer testLanguageServer = Mockito.mock(TestLanguageServer.class);
		final DocumentManager documentManager = Mockito.mock(DocumentManager.class);
		Mockito.when(documentManager.getContent("file:///foo.test"))
				.thenReturn(Arrays.asList(" Foo ", "Foo Bar Baz", "Bar Foo Baz"));
		Mockito.when(testLanguageServer.getDocumentManager()).thenReturn(documentManager);
		final DocumentHighlightHandler documentHighlightHandler = new DocumentHighlightHandler(testLanguageServer);
		final TextDocumentPositionParams documentPositionParams = new TextDocumentPositionParams()
				.withTextDocument(new TextDocumentIdentifier().withUri("file:///foo.test")).withPosition(new Position().withLine(0).withCharacter(2));
		// when
		final DocumentHighlight result = documentHighlightHandler.handle(documentPositionParams);
		// then
//		assertThat(result).containsExactly(
//				new DocumentHighlight().withKind(1)
//						.withRange(new Range().withStart(new Position().withLine(0).withCharacter(1))
//								.withEnd(new Position().withLine(0).withCharacter(3))),
//				new DocumentHighlight().withKind(1)
//						.withRange(new Range().withStart(new Position().withLine(1).withCharacter(0))
//								.withEnd(new Position().withLine(1).withCharacter(2))),
//				new DocumentHighlight().withKind(1)
//						.withRange(new Range().withStart(new Position().withLine(2).withCharacter(4))
//								.withEnd(new Position().withLine(2).withCharacter(6)))
//
//		);
		assertThat(result).isEqualTo(
				new DocumentHighlight().withKind(1)
				.withRange(new Range().withStart(new Position().withLine(1).withCharacter(0))
						.withEnd(new Position().withLine(1).withCharacter(2))));

	}
}
