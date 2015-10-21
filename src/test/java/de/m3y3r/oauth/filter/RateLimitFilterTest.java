package de.m3y3r.oauth.filter;

import javax.servlet.ServletException;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class RateLimitFilterTest {

	@Test
	public void testRateLimitOneIp() throws ServletException, InterruptedException {
		RateLimitFilter f = new RateLimitFilter();
		f.init(null);

		long minMillisPerRequest = 10;
		for(int i = 0, n = 100; i < n; i++) {
			long ts = System.currentTimeMillis();
			Entry e = f.maintain("localhost", ts);

			long timeDiff = ts - e.firstTs;
			long millisPerReq = e.noHits > 0 ? timeDiff / e.noHits : 0;
			if(e.noHits > 0) {
				Assert.assertThat(millisPerReq, Matchers.greaterThanOrEqualTo(minMillisPerRequest));
			}
			Thread.sleep(minMillisPerRequest);
		}
	}

	@Test
	public void testRateLimitTwoThousandIp() throws ServletException, InterruptedException {
		RateLimitFilter f = new RateLimitFilter();
		f.init(null);

		long minMillisPerRequest = 10;
		int maxIps = 2000; // doesn't fit in limit cache
		for(int i = 0, n = maxIps; i < n; i++) {
			long ts = System.currentTimeMillis();
			String ip = "localhost-" + i;
			Entry e = f.maintain(ip, ts);

			long timeDiff = ts - e.firstTs;
			long millisPerReq = e.noHits > 0 ? timeDiff / e.noHits : 0;
			if(e.noHits > 0) {
				Assert.assertThat(millisPerReq, Matchers.greaterThanOrEqualTo(minMillisPerRequest));
			}
		}
	}

	@Test
	public void testRateLimitOneThousandIp() throws ServletException, InterruptedException {
		RateLimitFilter f = new RateLimitFilter();
		f.init(null);

		long minMillisPerRequest = 100;
		int maxIps = 500; // fits in limit cache
		for(int i = 0, n = maxIps; i < n; i++) {
			long ts = System.currentTimeMillis();
			String ip = "localhost-" + i;
			Entry e = f.maintain(ip, ts);

			long timeDiff = ts - e.firstTs;
			long millisPerReq = e.noHits > 0 ? timeDiff / e.noHits : 0;
			if(e.noHits > 0) {
				Assert.assertThat(millisPerReq, Matchers.greaterThanOrEqualTo(minMillisPerRequest));
			}
		}
		Thread.sleep(minMillisPerRequest);
		for(int i = 0, n = maxIps; i < n; i++) {
			long ts = System.currentTimeMillis();
			String ip = "localhost-" + i;
			Entry e = f.maintain(ip, ts);

			long timeDiff = ts - e.firstTs;
			long millisPerReq = e.noHits > 0 ? timeDiff / e.noHits : 0;
			if(e.noHits > 0) {
				Assert.assertThat(millisPerReq, Matchers.greaterThanOrEqualTo(minMillisPerRequest));
			}
		}
	}

	@Test
	public void testRateLimitTwoThousandIp2() throws ServletException, InterruptedException {
		RateLimitFilter f = new RateLimitFilter();
		f.init(null);

		long minMillisPerRequest = 100;
		int maxIps = 2000; // doesn't fit in limit cache
		for(int i = 0, n = maxIps; i < n; i++) {
			long ts = System.currentTimeMillis();
			String ip = "localhost-" + i;
			Entry e = f.maintain(ip, ts);

			long timeDiff = ts - e.firstTs;
			long millisPerReq = e.noHits > 0 ? timeDiff / e.noHits : 0;
			if(e.noHits > 0) {
				Assert.assertThat(millisPerReq, Matchers.greaterThanOrEqualTo(minMillisPerRequest));
			}
		}
		Thread.sleep(minMillisPerRequest);
		for(int i = 0, n = maxIps; i < n; i++) {
			long ts = System.currentTimeMillis();
			String ip = "localhost-" + i;
			Entry e = f.maintain(ip, ts);

			long timeDiff = ts - e.firstTs;
			long millisPerReq = e.noHits > 0 ? timeDiff / e.noHits : 0;
			if(e.noHits > 0) {
				Assert.assertThat(millisPerReq, Matchers.greaterThanOrEqualTo(minMillisPerRequest));
			}
		}
	}
}
