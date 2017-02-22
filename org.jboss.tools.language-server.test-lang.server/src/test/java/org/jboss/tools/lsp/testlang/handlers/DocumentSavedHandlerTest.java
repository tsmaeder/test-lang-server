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

package org.jboss.tools.lsp.testlang.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.jboss.tools.lsp.testlang.DocumentManager;
import org.jboss.tools.lsp.testlang.TestLanguageServer;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Testing the {@link DocumentSavedHandler}.
 */
public class DocumentSavedHandlerTest {

	@Test
	public void shouldSendMessageToShow() throws IOException, URISyntaxException {
		// given
		final TestLanguageServer testLanguageServer = Mockito.mock(TestLanguageServer.class);
		final DocumentManager documentManager = Mockito.mock(DocumentManager.class);
		Mockito.when(documentManager.getContent("file:///foo.test"))
				.thenReturn(Arrays.asList("Foo", "window/showMessageNotification:Error: a message"));
		Mockito.when(testLanguageServer.getDocumentManager()).thenReturn(documentManager);
		final TestTextDocumentService service = new TestTextDocumentService(testLanguageServer);
		final DidSaveTextDocumentParams didSaveTextDocumentParams = new DidSaveTextDocumentParams(new TextDocumentIdentifier("file:///foo.test"), null);
		// when
		service.didSave(didSaveTextDocumentParams);
		// then
		Mockito.verify(testLanguageServer, Mockito.times(1)).sendShowMessageNotification(MessageType.Error,
				"a message");
	}

	@Test
	public void shouldSendMessageRequest() throws IOException, URISyntaxException {
		// given
		final TestLanguageServer testLanguageServer = Mockito.mock(TestLanguageServer.class);
		final DocumentManager documentManager = Mockito.mock(DocumentManager.class);
		Mockito.when(documentManager.getContent("file:///foo.test"))
				.thenReturn(Arrays.asList("Foo", "window/showMessageRequest:Error:Command: a message"));
		Mockito.when(testLanguageServer.getDocumentManager()).thenReturn(documentManager);
		final TestTextDocumentService service = new TestTextDocumentService(testLanguageServer);
		final DidSaveTextDocumentParams didSaveTextDocumentParams = new DidSaveTextDocumentParams(new TextDocumentIdentifier("file:///foo.test"), null);
		// when
		service.didSave(didSaveTextDocumentParams);
		// then
		Mockito.verify(testLanguageServer, Mockito.times(1)).sendShowMessageRequest(MessageType.Error,
				"a message", "Command");
	}

	
	@Test
	public void shouldNotMessageToShowWhenMissingType() throws IOException, URISyntaxException {
		// given
		final TestLanguageServer testLanguageServer = Mockito.mock(TestLanguageServer.class);
		final DocumentManager documentManager = Mockito.mock(DocumentManager.class);
		Mockito.when(documentManager.getContent("file:///foo.test"))
				.thenReturn(Arrays.asList("Foo", "window/showMessageNotification: a message"));
		Mockito.when(testLanguageServer.getDocumentManager()).thenReturn(documentManager);
		final TestTextDocumentService service = new TestTextDocumentService(testLanguageServer);
		final DidSaveTextDocumentParams didSaveTextDocumentParams = new DidSaveTextDocumentParams(new TextDocumentIdentifier("file:///foo.test"), null);
		// when
		service.didSave(didSaveTextDocumentParams);
		// then
		Mockito.verify(testLanguageServer, Mockito.times(0)).sendShowMessageNotification(ArgumentMatchers.any(),
				ArgumentMatchers.any());
	}
	
	@Test
	public void shouldMatchPattern() {
		// given
		final String value = "window/showMessageNotification:error:a message";
		// when
		final Matcher matcher = TestTextDocumentService.showMessagePattern.matcher(value);
		// then
		assertThat(matcher.matches()).isTrue();
		assertThat(matcher.group(1)).isEqualTo("error");
		assertThat(matcher.group(2)).isEqualTo("a message");
	}

	@Test
	public void shouldNotMatchPattern() {
		// given
		Stream.of("window/show", "window/showMessageNotification", "window/showMessageNotification:", "window/showMessageNotification:error", "window/showMessageNotification:error:").forEach(value -> {
			// when
			final Matcher matcher = TestTextDocumentService.showMessagePattern.matcher(value);
			// then
			assertThat(matcher.matches()).as(value).isFalse();
		});
	}
}
