package de.m3y3r.oauth.authserver;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@XmlRootElement
public class ErrorResponse {

	private final String error;

	private String error_description;

	private String error_uri;

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
	public String getError_description() {
		return error_description;
	}
	public void setError_description(String error_description) {
		this.error_description = error_description;
	}

	@JsonInclude(Include.NON_NULL)
	public String getError_uri() {
		return error_uri;
	}
	public void setError_uri(String error_uri) {
		this.error_uri = error_uri;
	}
}
