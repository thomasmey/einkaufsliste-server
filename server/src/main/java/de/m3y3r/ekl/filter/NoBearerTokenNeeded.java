package de.m3y3r.ekl.filter;

import java.lang.annotation.Retention;

import javax.ws.rs.NameBinding;
import java.lang.annotation.RetentionPolicy;

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface NoBearerTokenNeeded {}
