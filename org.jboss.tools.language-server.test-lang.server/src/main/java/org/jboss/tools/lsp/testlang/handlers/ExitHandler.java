package org.jboss.tools.lsp.testlang.handlers;

import org.jboss.tools.lsp.base.LSPMethods;
import org.jboss.tools.lsp.ipc.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExitHandler implements RequestHandler<Object, Object> {

	/** The usual Logger.*/
	private static final Logger LOGGER = LoggerFactory.getLogger(ExitHandler.class);
	
	@Override
	public boolean canHandle(String request) {
		return LSPMethods.EXIT.getMethod().equals(request);
	}

	@Override
	public Object handle(Object param) {
		LOGGER.info("Exiting Test Language Server");
		System.exit(0);
		return null;
	}

}
