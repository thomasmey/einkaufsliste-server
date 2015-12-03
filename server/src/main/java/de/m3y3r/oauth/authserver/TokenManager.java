package de.m3y3r.oauth.authserver;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import de.m3y3r.oauth.model.Token;

@ApplicationScoped
public class TokenManager {

	private Map<String, Token> tokenByClientUser;
	private Map<UUID, Token> tokenById;
	private Map<Date, Token> tokenByTime;
	private Logger log;

	public TokenManager() {
		log = Logger.getLogger(TokenManager.class.getName());
		tokenByClientUser = new TreeMap<>();
		tokenById = new TreeMap<>();
		tokenByTime = new TreeMap<>();
	}

	public Token getToken(String clientId, String username) {
		expungeExpiredEntries();
		String key = createKey(clientId, username);
		synchronized (this) {
			return tokenByClientUser.get(key);
		}
	}

	private String createKey(String clientId, String username) {
		if(clientId == null || username == null)
			throw new IllegalArgumentException();

		String key = clientId + '-' + username;
		return key;
	}

	private synchronized void expungeExpiredEntries() {
		Date currentTime = new Date();
		Calendar c = Calendar.getInstance();
		Iterator<Token> i = tokenByTime.values().iterator();
		while(i.hasNext()) {
			Token t = i.next();
			c.setTime(t.getEntryTs());
			c.add(Calendar.MILLISECOND, (int) TimeUnit.SECONDS.toMillis(t.getValidInSeconds()));
			if(currentTime.compareTo(c.getTime()) >= 0 ) {
				log.log(Level.INFO, "removing old token {0} - entryTs {1}", new Object[] { t.getId(), t.getEntryTs()});
				i.remove();
				tokenByClientUser.remove(t.getExternalKey());
				tokenById.remove(t.getId());
			}
			break;
		}
	}

	public Token newToken(String clientId, String username) {
		String key = createKey(clientId, username);

		Token token = new Token(key);
		log.log(Level.INFO, "new token {0} for clientid {1} and user {2}", new Object[] {token.getId(), clientId, username });
		synchronized (this) {
			tokenByClientUser.put(token.getExternalKey(), token);
			tokenById.put(token.getId(), token);
			tokenByTime.put(token.getEntryTs(), token);
		}
		log.log(Level.INFO, "size {0}", tokenByClientUser.size());
		return token;
	}

	public Token getTokenByUuid(UUID uuid) {
		expungeExpiredEntries();
		synchronized (this) {
			return tokenById.get(uuid);
		}
	}
}
