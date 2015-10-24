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
}
