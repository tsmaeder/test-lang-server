package org.jboss.tools.lsp.testlang.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.jboss.tools.lsp.testlang.DocumentManager;
import org.jboss.tools.lsp.testlang.TestLanguageServer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Testing the {@link DocumentHighlightHandler}
 */
public class DocumentHighlightHandlerTest {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentHighlightHandlerTest.class);

	@Test
	public void shouldFindSelectedWord() {
		// given
		final List<String> lines = Arrays.asList("Foo", " Foo ", "Foo Bar Baz", "Bar Foo Baz", "Bar Baz Foo");
		final int[] positions = { 1, 1, 0, 4, 8 };
		// when
		final List<String> result = IntStream.range(0, 5)
				.mapToObj(i -> DocumentManager.findSelectedWord(positions[i], lines.get(i)))
				.collect(Collectors.toList());
		// then
		LOGGER.debug("Result: {}", result.stream().map(r -> "'" + r + "'").collect(Collectors.joining(", ")));
		assertThat(result.stream().allMatch(w -> w.equals("Foo"))).isTrue();

	}

	@Test
	public void shouldHighlightText() throws IOException, URISyntaxException, InterruptedException, ExecutionException {
		// given
		final TestLanguageServer testLanguageServer = Mockito.mock(TestLanguageServer.class);
		final DocumentManager documentManager = Mockito.mock(DocumentManager.class);
		Mockito.when(documentManager.getContent("file:///foo.test"))
				.thenReturn(Arrays.asList(" Foo ", "Foo Bar Baz", "Bar Foo Baz"));
		Mockito.when(testLanguageServer.getDocumentManager()).thenReturn(documentManager);
		TestTextDocumentService documentService = new TestTextDocumentService(testLanguageServer);
		final TextDocumentPositionParams documentPositionParams = new TextDocumentPositionParams(
				new TextDocumentIdentifier("file:///foo.test"), null, new Position(0, 2));
		// when
		final List<? extends DocumentHighlight> result = documentService.documentHighlight(documentPositionParams)
				.get();
		// then
		// assertThat(result).containsExactly(
		// new DocumentHighlight().withKind(1)
		// .withRange(new Range().withStart(new
		// Position().withLine(0).withCharacter(1))
		// .withEnd(new Position().withLine(0).withCharacter(3))),
		// new DocumentHighlight().withKind(1)
		// .withRange(new Range().withStart(new
		// Position().withLine(1).withCharacter(0))
		// .withEnd(new Position().withLine(1).withCharacter(2))),
		// new DocumentHighlight().withKind(1)
		// .withRange(new Range().withStart(new
		// Position().withLine(2).withCharacter(4))
		// .withEnd(new Position().withLine(2).withCharacter(6)))
		//
		// );

		assertThat(result.size()== 1);
		assertThat(result.contains(
				new DocumentHighlight(new Range(new Position(1, 0), new Position(1, 2)), DocumentHighlightKind.Text)));

	}
	
	@Test
	public void testFindSelectedWord() {
		Assert.assertEquals("", DocumentManager.findSelectedWord(0, ""));
		Assert.assertEquals("Foo", DocumentManager.findSelectedWord(1, " Foo "));
		Assert.assertEquals("Foo", DocumentManager.findSelectedWord(4, " Foo "));
		Assert.assertEquals("", DocumentManager.findSelectedWord(0, " Foo "));
		Assert.assertEquals("", DocumentManager.findSelectedWord(5, " Foo "));
		Assert.assertEquals("Foo", DocumentManager.findSelectedWord(3, "Foo"));
		Assert.assertEquals("Foo", DocumentManager.findSelectedWord(0, "Foo"));
		Assert.assertEquals("Foo", DocumentManager.findSelectedWord(0, "Foo "));
		Assert.assertEquals("Foo", DocumentManager.findSelectedWord(4, " Foo"));
	}
}
