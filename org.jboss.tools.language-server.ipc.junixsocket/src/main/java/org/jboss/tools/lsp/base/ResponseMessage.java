
package org.jboss.tools.lsp.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Response Message sent as a result of a request.
 * 
 */
public class ResponseMessage <R>
    extends Message
{

	private transient String method;
	
    /**
     * The request id.
     * 
     */
    @SerializedName("id")
    @Expose
    private Integer id;
    /**
     * The result of a request. This can be omitted in the case of an error.
     * 
     */
    @SerializedName("result")
    @Expose
    private R result;
    /**
     * The error object in case a request fails
     * 
     */
    @SerializedName("error")
    @Expose
    private ResponseError error;

    /**
     * The request id.
     * 
     * @return
     *     The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * The request id.
     * 
     * @param id
     *     The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * The result of a request. This can be omitted in the case of an error.
     * 
     * @return
     *     The result
     */
    public R getResult() {
        return result;
    }

    /**
     * The result of a request. This can be omitted in the case of an error.
     * 
     * @param result
     *     The result
     */
    public void setResult(R result) {
        this.result = result;
    }

    /**
     * The error object in case a request fails
     * 
     * @return
     *     The error
     */
    public ResponseError getError() {
        return error;
    }

    /**
     * The error object in case a request fails
     * 
     * @param error
     *     The error
     */
    public void setError(ResponseError error) {
        this.error = error;
    }

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	void setMethod(String method) {
		this.method = method;
	}

}
