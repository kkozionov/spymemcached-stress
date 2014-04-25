package org.sugis.memcache;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.joda.time.Duration;

import com.google.common.collect.ImmutableList;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class MemcacheStress implements Runnable {
	private static final Random R = new Random();
	private static final List<String> KEY_LIST =
		Collections.synchronizedList(new ArrayList<String>());
	private static long shutdownTime;
	private static long startTime;
	private static final MemcachedClient MC;

	private static DateTimeFormatter formatter = DateTimeFormat.forPattern("H:m:s d MMMM, yyyy");
	static {
		try {
			MC = new MemcachedClient(new DefaultConnectionFactory(),
					ImmutableList.of(
						new InetSocketAddress(
							Inet4Address.getByAddress(new byte[] { 127,0,0,1 } ), 11211)));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


    public static void main( String[] args ) {
		startTime = System.currentTimeMillis();
    	shutdownTime = startTime + Duration.standardDays(2).getMillis();
		System.out.println("Started " + formatDate(startTime));
		System.out.println(" Finish " + formatDate(shutdownTime));
    	for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
    		new Thread(new MemcacheStress()).start();
    }

    @Override
	public void run() {
    	try {
    		runUnsafe();
    	} catch (Throwable e) {
    		e.printStackTrace();
			System.out.println("Failed " + formatDate(System.currentTimeMillis()));
    	}
    }

	public void runUnsafe() throws IOException {
		long gets = 0;
		long sets = 0;
		while (System.currentTimeMillis() < shutdownTime) {
			if (R.nextDouble() < .1 || KEY_LIST.isEmpty()) {
				// 1/10 add a new key
				String key = "" + R.nextLong();
				Long value = R.nextLong();
				MC.set(key, (int) Duration.standardHours(1).getStandardSeconds(), value);
				KEY_LIST.add(key);
				sets++;
			}
			String key = KEY_LIST.get(R.nextInt(KEY_LIST.size()));
			MC.get(key);
			gets++;
		}
		System.out.println("Finished " + formatDate(System.currentTimeMillis()));
		System.out.println(gets + " gets and " + sets + " sets");
		System.exit(0);
	}

	private static String formatDate(final long date) {
		return formatter.print(date);
	}
}
