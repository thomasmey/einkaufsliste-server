package de.m3y3r.oauth.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

/**
 * According to "Oracle JRockit - The Definitve Guide"
 * WeakHashMap are ideal for caches.
 * @author thomas
 *
 */
@WebFilter("/oauth/token")
public class RateLimitFilter implements Filter {

	private static final int MAX_STALE_PERIOD = 5000;
	private Map<String,Entry> entries;
	private long minMillisPerRequest = 1000;
	private volatile long lastInvocation;

	@Override
	public void destroy() {
		entries = null;
	}

	@Override
	public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain fc)
			throws IOException, ServletException {

		long ts = System.currentTimeMillis();
		synchronized (entries) {
			if(lastInvocation > 0 && ts - lastInvocation > MAX_STALE_PERIOD) {
				entries.clear();
			}
			lastInvocation = ts;
		}
		String remoteAddr = sreq.getRemoteAddr();
		Entry e = maintain(remoteAddr, ts);

		long timeDiff = e.currentTs - e.prevTs;
		long millisPerReq = e.noHits > 0 ? timeDiff : 0;
		if(e.noHits > 0 && millisPerReq < minMillisPerRequest) {
			HttpServletResponse hresp = (HttpServletResponse) sresp;
			hresp.sendError(HttpServletResponse.SC_FORBIDDEN);
		} else {
			fc.doFilter(sreq, sresp);
		}
	}

	Entry maintain(String ipAddr, long ts) {

		Entry e = entries.get(ipAddr);
		if(e != null) {
		// already existing ip address
			e.noHits++;
		} else {
		// new ip address
			e = new Entry();
			e.firstTs = ts;
			entries.put(ipAddr, e);
		}

		e.prevTs = e.currentTs;
		e.currentTs = ts;
		return e;
	}

	@Override
	public void init(FilterConfig fc) throws ServletException {
		entries = Collections.synchronizedMap(new WeakHashMap<>());
	}
}

class Entry {

	long firstTs;
	long prevTs;
	long currentTs;

	int noHits;

	public Entry() {}

	@Override
	public String toString() {
		return "Entry [noHits=" + noHits + ", entryTs=" + firstTs + "]";
	}
}