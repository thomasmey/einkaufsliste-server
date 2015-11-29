package de.m3y3r.oauth.authserver;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import de.m3y3r.oauth.model.Token;

@ApplicationScoped
public class TokenManager {

	private Map<String, Token> tokenByClientUser;
	private Map<UUID, Token> tokenById;
	private Logger log;
	
	public TokenManager() {
		log = Logger.getLogger(TokenManager.class.getName());
		tokenByClientUser = Collections.synchronizedMap(new WeakHashMap<>());
		tokenById = Collections.synchronizedMap(new WeakHashMap<>());
	}

	public Token getToken(String clientId, String username) {
		String key = clientId + '-' + username;
		return tokenByClientUser.get(key);
	}

	public Token newToken(String clientId, String username) {
		Token token = new Token();
		log.log(Level.INFO, "new token {0} for clientid {1} and user {2}", new Object[] {token.getId(), clientId, username });
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
