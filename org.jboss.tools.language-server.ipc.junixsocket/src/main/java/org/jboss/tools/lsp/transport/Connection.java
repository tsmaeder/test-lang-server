package org.jboss.tools.lsp.transport;

import java.io.IOException;

/**
 * A connection.
 */
public interface Connection {

	/**
	 * Interface to implement to be notified of incoming messages
	 */
	public interface MessageListener {
		/**
		 * Call back method for when a message was received.
		 * @param message the incoming {@link TransportMessage}
		 */
		void messageReceived(TransportMessage message);
	}

	/**
	 * Sends the specified message.
	 *
	 * @param message
	 *            to send
	 */
	void send(TransportMessage message);

	/**
	 * Starts up the transport and acquire all needed resources. Does nothing if
	 * the connection has already been started.
	 *
	 * @throws IOException
	 */
	void start() throws IOException;
	
	/**
	 * Sets the message listener that this connection will notify 
	 * the incoming messages to. Can be set only once
	 * 
	 * @param listener
	 * @throws IllegalStateException - if set more than once.
	 */
	void setMessageListener(MessageListener listener);

	/**
	 * Shuts down the transport freeing all acquired resources. Does nothing if
	 * the connection has already been shut down.
	 */
	void close();

}
