package hello;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;

import redis.clients.jedis.Jedis;

public class getJsonClass {
	
	public static String rootkey;
	public static boolean flag = false;
	public static int index = 0;
	
	public JsonNode getJson1(String root_id,Jedis jedis) throws IOException
	{
		
		
		rootkey = root_id;
		JsonNode rootnode = getObjectJson(root_id,jedis);
		return rootnode;
		
	}
	public JsonNode getObjectJson(String root_id,Jedis jedis) throws IOException
	{
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootnode = mapper.createObjectNode();
		if(jedis.exists(root_id))
		{
			
			Map<String,String> rootmap = jedis.hgetAll(root_id);
			Iterator<Map.Entry<String, String>> iterator = rootmap.entrySet().iterator();
			
			
			while(iterator.hasNext())
			{
				Map.Entry pair = (Map.Entry)iterator.next();
				
				
					 if((!(pair.getValue().toString().equals(rootkey)) && jedis.exists(pair.getValue().toString()))  && !(jedis.type(pair.getValue().toString()).equals("list")) )
							{
						 	
								
								//check if the object is an array or contains another object
								String key = pair.getKey().toString();
								String value = pair.getValue().toString();
								JsonNode childnode = getObjectJson(value,jedis);
								rootnode.set(key, childnode);
							
								
							}
					 else if(jedis.type(pair.getValue().toString()).equals("list"))
					 {
							ArrayNode arraynode = mapper.createArrayNode();
							//iterating through the elements of the list
							for(int i = 0;i<jedis.llen(pair.getValue().toString());i++)
							{
								
								JsonNode childnode = getObjectJson(jedis.lindex(pair.getValue().toString(),i).toString(),jedis);
								arraynode.add(childnode);
								
							}
							
							
							rootnode.set(pair.getValue().toString(), JsonLoader.fromString(arraynode.toString()));
							
					 }
					else
					{
						
						rootnode.put(pair.getKey().toString(),pair.getValue().toString());
					}
				
			}
			
		}
		else
		{
			System.out.println("the key doesn't exist");
			System.exit(0);
		}
		return (JsonNode)rootnode;
				
		
	}
	

}
