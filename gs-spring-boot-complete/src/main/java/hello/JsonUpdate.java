package hello;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.JsonLoader;

import redis.clients.jedis.Jedis;



public class JsonUpdate {
	
	
	public String ValidateJsonandUpdate(String jsonObject,String root_id, Jedis jedis) throws JsonProcessingException, IOException, ProcessingException
	{
		Map<String,String> rootmap = jedis.hgetAll(root_id);
		ObjectMapper mapper = new ObjectMapper();
        JsonNode given_node = mapper.readTree(jsonObject);
        
        
        
	    //checking if the id exists in the given jsonobject
	    if(given_node.has("id"))
	    {
		    //getting the id and preparing it to query from DB
			String id = given_node.path("id").asText();
			if(!id.equals(root_id))
			id = id + "-" + root_id;
	
			if(jedis.exists(id))
			{
				
				Map<String,String> oldmap = jedis.hgetAll(id);
				Map<String,String> newmap = new HashMap<String, String>();
				
				//iterating over the given_node and inserting it to the newmap
				final Iterator<Map.Entry<String, JsonNode>> iterator = given_node.fields();
		        while (iterator.hasNext()) {
		            
		        	Map.Entry<String, JsonNode> temp = iterator.next();
		        	newmap.put(temp.getKey(), temp.getValue().asText());
		        	
		        	
		        }
				
		        //copying the remaining key-value pairs to the newmap
				Iterator it = oldmap.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			        if(!newmap.containsKey(pair.getKey()))
			        {
			        	newmap.put(pair.getKey().toString(), pair.getValue().toString());
			        }
			    }
			    
			    //deleting the old entry for this key and inserting a new map
			    jedis.del(id);
				jedis.hmset(id, newmap);
				
			}//end of inner if
			
	    }//end of outer if
	    else
	    {
	    	System.out.println("Please give the id of the json object to be updated");
	    }
	    
	   
	
		return "object successfully updated";
	}
	
	
	

}
