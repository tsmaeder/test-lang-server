
package org.jboss.tools.lsp.ext;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StatusReport {
	public StatusReport(ServiceStatus type, String message) {
		this.type = type;
		this.message = message;
	}

	/**
	 * The message type. See {
	 * 
	 */
	@SerializedName("type")
	@Expose
	private ServiceStatus type;
	/**
	 * The actual message
	 * 
	 */
	@SerializedName("message")
	@Expose
	private String message;

	/**
	 * The message type. See {
	 * 
	 * @return The type
	 */
	public ServiceStatus getType() {
		return type;
	}

	/**
	 * The actual message
	 * 
	 * @return The message
	 */
	public String getMessage() {
		return message;
	}

}
