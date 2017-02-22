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

/**
 * 
 */
public class TestLanguageServerTest {

	@Rule
	public FakeTestLangClient fakeClient = new FakeTestLangClient(
			new File(new File(System.getProperty("java.io.tmpdir")), "junixsocket-test-server-to-ide.sock"),
			new File(new File(System.getProperty("java.io.tmpdir")), "junixsocket-test-ide-to-server.sock"));

	@Test
	public void shouldRespondToInitRequest() throws IOException, InterruptedException, ExecutionException {
		// given a test lang server
		System.out.println("starting server");
		new TestLanguageServer().start();
		System.out.println("waiting for connections");
		fakeClient.waitForConnections();
		// when
		fakeClient.expectMessages("initialize", "language/status", "language/status");
		fakeClient.sendInitializeRequest();
		// then expect to received a message notification before the timeout
		assertTrue("Did not receive all messages", fakeClient.waitForMessages(TimeUnit.SECONDS.toMillis(600)));
	}

	@Test
	public void shouldSendPublishDiagnosticNotificationOnDidSaveNotification()
			throws IOException, InterruptedException, ExecutionException, URISyntaxException {
		// given a test lang server with a mock'd DocumentManager
		final DocumentManager mockDocumentManager = Mockito.mock(DocumentManager.class);
		final List<String> docLines = Arrays.asList("foo and bar", "window/showMessageNotification:Error:a message to show");
		Mockito.when(mockDocumentManager.getContent("file:///path/to/file")).thenReturn(docLines);
		new TestLanguageServer(mockDocumentManager).start();
		fakeClient.waitForConnections();
		// when
		fakeClient.expectMessages("language/status", "language/status");
		fakeClient.sendInitializeRequest();
		assertTrue("Did not receive all messages", fakeClient.waitForMessages(TimeUnit.SECONDS.toMillis(600)));
		// when sending a 'didSave' notification with a line containing the
		// 'ERROR' keyword
		fakeClient.expectMessages("window/showMessage");
		fakeClient.sendDidSaveNotification("file:///path/to/file");
		// then expect to received a message notification before the timeout
		assertTrue("Did not receive all messages", fakeClient.waitForMessages(TimeUnit.SECONDS.toMillis(600)));
	}

}
