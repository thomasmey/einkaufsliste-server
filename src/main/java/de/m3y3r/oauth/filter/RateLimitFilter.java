package de.m3y3r.oauth.filter;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

@WebFilter("/oauth/token")
public class RateLimitFilter implements Filter {

	private static final int MAX_STALE_PERIOD = 5000;
	private static final int MAX_ENTRIES = 1000;
	private Entry[] entries;
	private long minMillisPerRequest = 1000;
	private int noEntries;
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
				noEntries = 0;
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

		Entry e = new Entry(ipAddr);
		synchronized (entries) {
			int slot = Arrays.binarySearch(entries, 0, noEntries, e);
			// already existing ip address
			if(slot >= 0) {
				e = entries[slot];
				e.noHits++;
			// new ip address
			} else {
				e.firstTs = ts;
				slot = -slot - 1;

				// are slots free?
				if(noEntries < MAX_ENTRIES) {
					// shit all entries one position to right
					for(int i=noEntries; i > slot; i--) {
						entries[i] = entries[i - 1];
					}
					entries[slot] = e;
					noEntries++;
				// all slots are taken
				} else {
					// find oldest entry
					//FIXME: linear search
					long oldestEntry = ts;
					for(int i = 0; i < noEntries; i++) {
						if(entries[i].firstTs < oldestEntry) {
							oldestEntry = entries[i].firstTs;
							slot = i;
						}
					}
					entries[slot] = e;
					Arrays.sort(entries);
				}
			}
		}
		e.prevTs = e.currentTs;
		e.currentTs = ts;
		return e;
	}

	@Override
	public void init(FilterConfig fc) throws ServletException {
		entries = new Entry[MAX_ENTRIES];
	}

}

class Entry implements Comparable<Entry>{

	long firstTs;
	long prevTs;
	long currentTs;

	String ipAddr;
	int noHits;

	public Entry(String remoteAddr) {
		ipAddr = remoteAddr;
	}

	public Entry() {}

	@Override
	public int compareTo(Entry o) {
		return ipAddr.compareTo(o.ipAddr);
	}

	@Override
	public String toString() {
		return "Entry [ipAddr=" + ipAddr + ", noHits=" + noHits + ", entryTs=" + firstTs + "]";
	}
}