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

package org.jboss.tools.lsp.ext;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.services.LanguageClient;

/** 
 * Extensions to the protocol initiated by the server and called on the client
 * @author Thomas Mäder
 *
 */
public interface ExtendedLanguageClient extends LanguageClient {
	@JsonNotification("language/status")
	void statusEvent(StatusReport status);

}
