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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class TestLanguageServerTest {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestLanguageServerTest.class);

	@Rule
	public FakeTestLangClient fakeClient = new FakeTestLangClient(
			new File(new File(System.getProperty("java.io.tmpdir")), "junixsocket-test-server-to-ide.sock"),
			new File(new File(System.getProperty("java.io.tmpdir")), "junixsocket-test-ide-to-server.sock"));

	@Test
	public void shouldRespondToInitRequest() throws IOException, InterruptedException, ExecutionException {
		// given a test lang server
		final TestLanguageServer testLangServer = new TestLanguageServer();
		testLangServer.start();
		fakeClient.waitForConnections();
		// when
		fakeClient.sendInitializeRequest();
		// then expect to received a message notification before the timeout
		fakeClient.waitforResponses(TimeUnit.SECONDS.toMillis(600), "language/status", "language/status");
		assertTrue("Did not receive all messages", fakeClient.verifyMessages());
	}

	@Test
	public void shouldSendPublishDiagnosticNotificationOnDidSaveNotification()
			throws IOException, InterruptedException, ExecutionException, URISyntaxException {
		// given a test lang server with a mock'd DocumentManager
		final DocumentManager mockDocumentManager = Mockito.mock(DocumentManager.class);
		final List<String> docLines = Arrays.asList("foo and bar", "ERROR an error");
		Mockito.when(mockDocumentManager.getContent("file:///path/to/file")).thenReturn(docLines);
		final TestLanguageServer testLangServer = new TestLanguageServer(mockDocumentManager);
		testLangServer.start();
		fakeClient.waitForConnections();
		// when
		fakeClient.sendInitializeRequest();
		fakeClient.waitforResponses(TimeUnit.SECONDS.toMillis(600), "language/status", "language/status");
		// when sending a 'didSave' notification with a line containing the
		// 'ERROR' keyword
		fakeClient.sendDidSaveNotification("file:///path/to/file");
		// then expect to received a message notification before the timeout
		fakeClient.waitforResponses(TimeUnit.SECONDS.toMillis(600), "language/status", "language/status",
				"textDocument/publishDiagnostics");
		assertTrue("Did not receive all messages", fakeClient.verifyMessages());
	}

}
