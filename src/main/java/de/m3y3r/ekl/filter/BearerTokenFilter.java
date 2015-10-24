package de.m3y3r.ekl.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter("/ekl/*")
public class BearerTokenFilter implements Filter {


	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain fc)
			throws IOException, ServletException {

		fc.doFilter(sreq, sresp);
	}

	@Override
	public void init(FilterConfig fc) throws ServletException {
	}

}
