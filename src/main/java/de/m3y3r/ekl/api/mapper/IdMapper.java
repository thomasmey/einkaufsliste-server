package de.m3y3r.ekl.api.mapper;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IdMapper {

	public String idtoUuid(Integer id) {
		return id.toString();
	}
}
