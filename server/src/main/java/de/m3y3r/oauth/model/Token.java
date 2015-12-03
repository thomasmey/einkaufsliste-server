package de.m3y3r.oauth.model;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Token {


	private final UUID id;
	private final Date entryTs;
	private final int validInSeconds;
	private final String externalKey;
	private Context context;

	public Token(String externalKey) {
		this(3600, externalKey);
	}
	public Token(int validSeconds, String externalKey) {
		this.context = new Context();
		this.entryTs = new Date();
		this.id = UUID.randomUUID();
		this.validInSeconds = validSeconds;
		this.externalKey = externalKey;
	}
	public UUID getId() {
		return id;
	}
	public Date getEntryTs() {
		return entryTs;
	}
	public int getValidInSeconds() {
		return validInSeconds;
	}
	public Context getContext() {
		return context;
	}
	public long getExpiresIn() {
		Date date = new Date();
		long diff = (date.getTime() - entryTs.getTime()) / TimeUnit.SECONDS.toMillis(1);
		return validInSeconds - diff;
	}
	public String getExternalKey() {
		return externalKey;
	}
}