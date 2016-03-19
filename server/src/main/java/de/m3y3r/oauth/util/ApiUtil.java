package de.m3y3r.oauth.util;

import java.util.List;

import javax.json.JsonObject;

import de.m3y3r.ekl.api.NotAuthorisedException;
import de.m3y3r.oauth.model.Token;

public class ApiUtil {

	public static boolean checkMandatoryAttributes(JsonObject j, List<String> a) {
		return a.stream().allMatch((k) -> { return j.containsKey(k); } );
	}

	public static void checkAuthorisation(Token token, int ownerId) throws NotAuthorisedException {
		if(ownerId != token.getContext().getUser().getInt("id"))
			throw new NotAuthorisedException();
	}

}
