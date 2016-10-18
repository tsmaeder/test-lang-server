package org.jboss.tools.lsp.ipc;

import org.jboss.tools.lsp.base.LSPException;

/** 
 * Interface used for registering JSON RPC method handlers 
 * to the framework.
 * 
 * @author Gorkem Ercan
 *
 * @param <R> param object
 * @param <S> result object
 */
public interface RequestHandler<R,S> {
	
	
	/**
	 * Returns true if this handler can 
	 * handle this request. 
	 * 
	 * @param request - JSON RPC method value
	 * @return
	 */
	public boolean canHandle(String request);
	
	/**
	 * Invoked by the framework if canHandle 
	 * returns true. Params are converted to their 
	 * corresponding Java models and passed in. 
	 * Return value is used as response result.
	 * <p>
	 * {@link LSPException}s thrown will be converted to 
	 * errors and delivered to client if this is 
	 * a request/response 
	 * </p>
	 * 
	 * @param param
	 * @return result
	 * @throws LSPException
	 */
	public S handle(R param) ;
	
}
