package de.m3y3r.oauth.authserver;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.enterprise.context.ApplicationScoped;

import de.m3y3r.oauth.model.Token;

@ApplicationScoped
public class TokenManager {

	private Map<String, Token> tokenByClientUser;
	private Map<UUID, Token> tokenById;

	public TokenManager() {
		tokenByClientUser = Collections.synchronizedMap(new WeakHashMap<>());
		tokenById = Collections.synchronizedMap(new WeakHashMap<>());
	}

	public Token getToken(String clientId, String username) {
		String key = clientId + '-' + username;
		return tokenByClientUser.get(key);
	}

	public Token newToken(String clientId, String username) {
		Token token = new Token();
		String key = clientId + '-' + username;
		synchronized (tokenByClientUser) {
			tokenByClientUser.put(key, token);
			tokenById.put(token.getId(), token);
		}
		return token;
	}

	public Token getTokenByUuid(UUID uuid) {
		return tokenById.get(uuid);
	}
}
