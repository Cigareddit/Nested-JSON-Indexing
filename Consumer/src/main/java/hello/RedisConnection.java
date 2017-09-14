package hello;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnection {
	private Jedis jedis;
	private static JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);

	public Jedis getRedisConnection() {
		// Jedis jedis = null;
		try {
			jedis = pool.getResource();

		} finally {

		}
		return jedis;
	}
	
	public JSONObject getObjectFromRedis(String id) throws IOException, ParseException{
		JSONObject parentJson = new JSONObject();
		
		JSONObject returnJSON = (JSONObject) index_test(id, parentJson);
		return returnJSON;
		
		//System.out.println(jsonNode.toString());
		
		
		
		
		
		
	}
	
	

	
	
	

	public Object index_test(String id, Object object) {

		// Jedis jedis = getRedisConnection();
		Map<String, String> jsonReconstruct = null;
		ArrayList<String> jsonList = new ArrayList<String>();
		JSONObject parentJson = null;
		JSONArray parentArray = null;

		if (object instanceof JSONArray) {
			System.out.println("hello51");
			
			//new code
			for(int i = 0;i<jedis.llen(id);i++)
			{
				jsonList.add(jedis.lindex(id, i));
				
			}
			
			//jsonList = jedis.smembers(id);
			parentArray = (JSONArray) object;

			for (Object entry : jsonList) {
				// System.out.println(stock);

				if (jedis.type(entry.toString()).equals("hash")) {
					System.out.println("hello10");
					System.out.println("id at this stage" + id);
					System.out.println("Jedis object type=" + jedis.type(entry.toString()));
					JSONObject childJson = new JSONObject();
					parentArray.add(index_test(entry.toString(), childJson));

					System.out.println("object");
				} else if (jedis.type(entry.toString()).equals("list")) {
					System.out.println("id at this stage" + id);
					System.out.println("Jedis object type=" + jedis.type(entry.toString()));

					JSONArray jsonArray = new JSONArray();
					parentArray.add(index_test(entry.toString(), jsonArray));

					System.out.println("Array");
				} else {
					System.out.println("hello11");
					System.out.println("id at this stage" + id);
					parentArray.add(entry.toString());
				}

			}
			return parentArray;

		} else if (object instanceof JSONObject) {
		
			jsonReconstruct = jedis.hgetAll(id);
			parentJson = (JSONObject) object;

			for (Map.Entry<String, String> entry : jsonReconstruct.entrySet()) {

				// if (entry.getKey().equals("_id") &&
				// entry.getValue().equals(parentID)) {
				if (entry.getKey().equals("id")) {
					System.out.println("hello3");
					System.out.println("id at this stage" + id);
					parentJson.put(entry.getKey(), entry.getValue());
				} else {
					System.out.println("id at this stage" + id);
					System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());

					if (jedis.type(entry.getValue().toString()).equals("hash")) {
						System.out.println("hello4");
						System.out.println("Jedis object type=" + jedis.type(entry.getValue().toString()));
						JSONObject childJson = new JSONObject();

						parentJson.put(entry.getKey(), index_test(entry.getValue(), childJson));
						System.out.println("object");
					} else if (jedis.type(entry.getValue().toString()).equals("list")) {
						System.out.println("hello6");
						System.out.println("id at this stage" + id);
						System.out.println("Jedis object type=" + jedis.type(entry.getValue().toString()));

						// parentJson
						JSONArray jsonArray = new JSONArray();
						parentJson.put(entry.getKey(), index_test(entry.getValue(), jsonArray));
						// JSONObject childJson = new JSONObject();
						// childJson=reconstructJson(entry.getValue(),
						// childJson);
						// jsonArray.add(childJson);
						// parentJson.put(entry.getKey(),jsonArray );

						System.out.println("Array");
					} else {
						System.out.println("hello7");
						System.out.println("id at this stage" + id);
						parentJson.put(entry.getKey(), entry.getValue());
					}

					// jsonObject.put(entry.getKey(), entry.getValue());
				}

			}
			return parentJson;

		} else {
					System.out.println("hello9");
		}
		return parentJson;

		// jsonObject.putAll(jsonReconstruct);

	}

	
	
}
