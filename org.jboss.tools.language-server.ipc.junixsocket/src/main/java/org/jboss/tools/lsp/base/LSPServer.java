package org.jboss.tools.lsp.base;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jboss.tools.lsp.base.ResponseError.ReservedCode;
import org.jboss.tools.lsp.ipc.RequestHandler;
import org.jboss.tools.lsp.transport.Connection;
import org.jboss.tools.lsp.transport.Connection.MessageListener;
import org.jboss.tools.lsp.transport.TransportMessage;
import org.jboss.tools.lsp.transport.junixsocket.NamedPipeConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Base class for Language Servers
 */
public class LSPServer implements MessageListener {

	/** The usual Logger.*/
	private static final Logger LOGGER = LoggerFactory.getLogger(LSPServer.class);
	
	public static final String STDOUT_PIPE_NAME = "STDOUT_PIPE_NAME";
	public static final String STDIN_PIPE_NAME = "STDIN_PIPE_NAME";

	private Connection connection;
	private final Gson gson;
	private static LSPServer instance;
	private List<RequestHandler<?, ?>> handlers;

	protected LSPServer() {
		GsonBuilder builder = new GsonBuilder();
		gson = builder.registerTypeAdapter(Message.class, new MessageJSONHandler()).create();
	}

	public static LSPServer getInstance() {
		if (instance == null) {
			instance = new LSPServer();
		}
		return instance;
	}

	public void connect(List<RequestHandler<?, ?>> handlers) throws IOException {
		setHandlers(handlers);
		final String stdInName = EnvironmentUtils.getEnvVarOrSysProp(STDIN_PIPE_NAME);
		final String stdOutName = EnvironmentUtils.getEnvVarOrSysProp(STDOUT_PIPE_NAME);
		this.connection = getConnection(stdInName, stdOutName);
		connection.setMessageListener(this);
		setConnection(connection);
		connection.start();
	}

	private Connection getConnection(final String stdInName, final String stdOutName) {
		LOGGER.info("Instanciating a new {}", NamedPipeConnection.class.getName());
		return new NamedPipeConnection(stdOutName, stdInName);
	}

	public void setHandlers(List<RequestHandler<?, ?>> handlers) {
		this.handlers = handlers;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public void send(Message message) {
		TransportMessage tm = new TransportMessage(gson.toJson(message));
		connection.send(tm);
	}

	@Override
	public void messageReceived(TransportMessage message) {
		LOGGER.info("Message received: {}", message.getContent());
		Message msg = maybeParseMessage(message);
		if (msg == null)
			return;

		if (msg instanceof NotificationMessage) {
			NotificationMessage<?> nm = (NotificationMessage<?>) msg;
			try {
				dispatchNotification(nm);
			} catch (LSPException e) {
				e.printStackTrace();
			}
		}

		if (msg instanceof RequestMessage) {
			RequestMessage<?> rm = (RequestMessage<?>) msg;
			try {
				dispatchRequest(rm);
			} catch (LSPException e) {
				send(rm.respondWithError(e.getCode(), e.getMessage(), e.getData()));
			} catch (Exception e) {
				send(rm.respondWithError(ReservedCode.INTERNAL_ERROR.code(), e.getMessage(), null));
			}
		}
	}

	/**
	 * Parses the message notifies client if parse fails and returns null
	 * 
	 * @param message
	 * @param msg
	 * @return
	 */
	private Message maybeParseMessage(TransportMessage message) {
		ResponseError error = null;
		try {
			return gson.fromJson(message.getContent(), Message.class);
		} catch (LSPException e) {
			error = new ResponseError();
			error.setCode(e.getCode());
			error.setMessage(e.getMessage());
			error.setData(e.getData());
		} catch (Exception e) {
			error = new ResponseError();
			error.setCode(ReservedCode.PARSE_ERROR.code());
			error.setMessage(e.getMessage());
			error.setData(message.getContent());
		}
		if (error != null) {
			@SuppressWarnings("rawtypes")
			ResponseMessage rm = new ResponseMessage();
			rm.setError(error);
			send(rm);
		}
		return null;
	}

	private void dispatchRequest(RequestMessage<?> request) {
		for (Iterator<RequestHandler<?, ?>> iterator = handlers.iterator(); iterator.hasNext();) {
			@SuppressWarnings("unchecked")
			RequestHandler<Object, Object> requestHandler = (RequestHandler<Object, Object>) iterator.next();
			if (requestHandler.canHandle(request.getMethod())) {
				LOGGER.info("Dispatching request {} to handler {}", request, requestHandler);
				send(request.responseWith(requestHandler.handle(request.getParams())));
				return;
			}
		}
		throw new LSPException(ReservedCode.METHOD_NOT_FOUND.code(), request.getMethod() + " is not handled", null,
				null);
	}

	private void dispatchNotification(NotificationMessage<?> nm) {
		for (Iterator<RequestHandler<?, ?>> iterator = handlers.iterator(); iterator.hasNext();) {
			@SuppressWarnings("unchecked")
			RequestHandler<Object, Object> requestHandler = (RequestHandler<Object, Object>) iterator.next();
			if (requestHandler.canHandle(nm.getMethod())) {
				LOGGER.info("Dispatching notification {} to handler {}", nm, requestHandler);
				requestHandler.handle(nm.getParams());
				break;
			}
		}
	}

	public void shutdown() {
		connection.close();
	}
}
