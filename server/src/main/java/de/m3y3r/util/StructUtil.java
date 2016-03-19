package de.m3y3r.util;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructUtil {

	@SafeVarargs
	public static <T> List<T> l(T... e) {
		return Arrays.asList(e);
	}

	public static <K,V> Map.Entry<K,V> e(K k, V v) {
		return new AbstractMap.SimpleEntry<K,V>(k, v);
	}

	@SafeVarargs
	public static <K,V> Map<K,V> m(Map.Entry<K, V>... e) {
		HashMap<K, V> m = new HashMap<>();
		for(Map.Entry<K, V> a: e) 
			m.put(a.getKey(), a.getValue());
		return m;
	}

	public static <K,V> Map<K,V> m(K k, V v) {
		HashMap<K, V> m = new HashMap<>();
		m.put(k, v);
		return m;
	}
}
