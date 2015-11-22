package de.m3y3r.oauth.authserver;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@XmlRootElement
public class TokenResponse {

	public enum TokenType { BEARER;
		@Override
		public String toString() {
			return this.name().toLowerCase();
			};
		}

	@XmlElement(name="access_token")
	private String accesToken;

	@XmlElement(name="token_type")
	private TokenType tokenType;

	@XmlElement(name="expires_in")
	private Long expiresIn;

	@JsonInclude(Include.NON_NULL)
	@XmlElement(name="scope")
	private String scope;

	public String getAccesToken() {
		return accesToken;
	}

	public void setAccesToken(String accesToken) {
		this.accesToken = accesToken;
	}

	public TokenType getTokenType() {
		return tokenType;
	}

	public void setTokenType(TokenType tokenType) {
		this.tokenType = tokenType;
	}

	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}
