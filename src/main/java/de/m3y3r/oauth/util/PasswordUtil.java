package de.m3y3r.oauth.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordUtil {

	private Logger log;

	public PasswordUtil() {
		log = Logger.getLogger(PasswordUtil.class.getName());
	}

	public boolean isPasswordOkay(byte[] hashedPassword, byte[] salt, byte[] clientSecret) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(clientSecret);
			md.update(salt);
			byte[] hashedPasswordFromInput = md.digest();
			log.log(Level.INFO, "hashedPasswordFromInput={0}", Arrays.toString(hashedPasswordFromInput));
			return Arrays.equals(hashedPassword, hashedPasswordFromInput);
		} catch (NoSuchAlgorithmException e) {
			log.log(Level.SEVERE, "WHAT?", e);
		}
		return false;
	}

}
