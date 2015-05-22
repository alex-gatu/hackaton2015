package ro.endava.hackathon2015;

import java.util.HashMap;
import java.util.HashSet;

import com.sun.net.httpserver.Headers;

public class HttpHelper {
	public static void mergeObject(HashMap from, HashMap to) {
		for (Object id : from.keySet())
			if (id instanceof String)
				to.put(id,  from.get(id));
	}
}
