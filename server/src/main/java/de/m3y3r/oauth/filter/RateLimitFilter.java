package de.m3y3r.oauth.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

@Provider
@RateLimit
public class RateLimitFilter implements ContainerRequestFilter {

	private static final int MAX_STALE_PERIOD = 5000;
	private long minMillisPerRequest = 1000;

	private Logger log;
	private Map<String,Entry> entries;
	private volatile long lastInvocation;

	@Context
	private HttpServletRequest servletRequest;

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

	public RateLimitFilter() {
		log = Logger.getLogger(RateLimitFilter.class.getName());
		entries = Collections.synchronizedMap(new WeakHashMap<>());
		log.log(Level.INFO, "new rate limit filter");
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		long ts = System.currentTimeMillis();
		synchronized (entries) {
			if(lastInvocation > 0 && ts - lastInvocation > MAX_STALE_PERIOD) {
				entries.clear();
			}
			lastInvocation = ts;
		}
		String remoteAddr = servletRequest.getRemoteAddr();
		Entry e = maintain(remoteAddr, ts);

		long timeDiff = e.currentTs - e.prevTs;
		long millisPerReq = e.noHits > 0 ? timeDiff : 0;
		if(e.noHits > 0 && millisPerReq < minMillisPerRequest) {
			log.log(Level.INFO, "rate limit hit - no hits {2} - was {0}, goal {1}", new Object[] {millisPerReq, minMillisPerRequest, e.noHits});
			requestContext.abortWith(Response.status(Status.FORBIDDEN).build());
		}
	}

	/* FIXME */
	public static long getMinMillisPerRequest() {
		return 1000;
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