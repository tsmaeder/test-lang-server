
package org.jboss.tools.lsp.messages;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class DocumentHighlight {

    @SerializedName("range")
    @Expose
    private Range range;
    
    /**
     * The highlight kind, default is [text](#DocumentHighlightKind.Text).
     * 
     */
    @SerializedName("kind")
    @Expose
    private Integer kind;

    /**
     * 
     * @return
     *     The range
     */
    public Range getRange() {
        return range;
    }

    /**
     * 
     * @param range
     *     The range
     */
    public void setRange(Range range) {
        this.range = range;
    }

    public DocumentHighlight withRange(Range range) {
        this.range = range;
        return this;
    }

    /**
     * The highlight kind, default is [text](#DocumentHighlightKind.Text).
     * 
     * @return
     *     The kind
     */
    public Integer getKind() {
        return kind;
    }

    /**
     * The highlight kind, default is [text](#DocumentHighlightKind.Text).
     * 
     * @param kind
     *     The kind
     */
    public void setKind(Integer kind) {
        this.kind = kind;
    }

    public DocumentHighlight withKind(Integer kind) {
        this.kind = kind;
        return this;
    }

	@Override
	public String toString() {
		return "DocumentHighlight [range=" + range + ", kind=" + kind + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((range == null) ? 0 : range.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocumentHighlight other = (DocumentHighlight) obj;
		if (kind == null) {
			if (other.kind != null)
				return false;
		} else if (!kind.equals(other.kind))
			return false;
		if (range == null) {
			if (other.range != null)
				return false;
		} else if (!range.equals(other.range))
			return false;
		return true;
	}
    
}
