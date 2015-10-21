package de.m3y3r.oauth.authserver;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@XmlRootElement
public class ErrorResponse {

	private final String error;

	@XmlElement(name="error_description")
	private String errorDescription;

	@XmlElement(name="error_uri")
	private String errorUri;

	public enum ErrorType {INVALID_REQUEST, INVALID_CLIENT, INVALID_GRANT,
		UNAUTHORIZED_CLIENT, UNSUPPORTED_GRANT_TYPE, INVALID_SCOPE;
		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}

	public ErrorResponse(ErrorType errorType) {
		this.error = errorType.toString();
	}
	public String getError() {
		return error;
	}

	@JsonInclude(Include.NON_NULL)
	public String getErrorDescription() {
		return errorDescription;
	}
	public void setErrorDescription(String error_description) {
		this.errorDescription = error_description;
	}

	@JsonInclude(Include.NON_NULL)
	public String getErrorUri() {
		return errorUri;
	}
	public void setErrorUri(String error_uri) {
		this.errorUri = error_uri;
	}
}
