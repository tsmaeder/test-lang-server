package org.jboss.tools.lsp.ipc;

public enum MessageType {
	/**
	 * An error message
	 */
	Error(1),
	/**
	 * A warning message
	 */
	Warning(2),
	/**
	 * An info message
	 */
	Info(3),
	/**
	 * A basic logging message
	 */
	Log(4);

	int type;

	private MessageType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public static MessageType from(final String type) {
		switch (type.toUpperCase()) {
		case "ERROR":
			return Error;
		case "WARNING":
			return Warning;
		case "INFO":
			return Info;
		default:
			return Log;
		}
	}

}
