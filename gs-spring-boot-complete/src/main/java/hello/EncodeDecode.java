package hello;

import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

import redis.clients.jedis.Jedis;

import com.fasterxml.jackson.databind.JsonNode;

public class EncodeDecode {
	
	
	
	public void encode(JsonNode jsonobject, Jedis jedis)
	{
		 
		   
		   String md5Hex = DigestUtils.md5Hex(jsonobject.toString());
		   md5Hex = (char)34 + md5Hex + (char)34;   
		   String key = jsonobject.path("id").asText().toString() + "-" + "ETAG";
		   jedis.set(key, md5Hex); 
		   
		   
		
	}
	
	public void updateencode(String id, Jedis jedis) throws IOException
	{
		 	//get the latest jsonobject and get the md5Hex of the node
			getJsonClass gjson = new getJsonClass();
			JsonNode jsonNode = gjson.getJson1(id, jedis);
			String md5Hex = DigestUtils.md5Hex(jsonNode.toString());
			md5Hex = (char)34 + md5Hex + (char)34;   
			String key = id + "-" + "ETAG";
			jedis.set(key, md5Hex); 
		   
		   
		
	}

}
