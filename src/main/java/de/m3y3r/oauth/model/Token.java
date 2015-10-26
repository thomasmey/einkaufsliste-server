package de.m3y3r.oauth.model;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Token {


	private final UUID id;
	private final Date entryTs;
	private int validInSeconds;
	private Context context;

	public Token() {
		this.context = new Context();
		this.entryTs = new Date();
		this.id = UUID.randomUUID();
		this.validInSeconds = 3600;
	}
	public Token(int validSeconds) {
		this();
		this.validInSeconds = validSeconds;
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
}