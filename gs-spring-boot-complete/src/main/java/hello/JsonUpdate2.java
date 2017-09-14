package hello;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.JsonLoader;

import redis.clients.jedis.Jedis;



public class JsonUpdate2 {
	
	
	private Jedis jedis;
	private String root_id = "";
	
	public JsonUpdate2(Jedis jedis,String root_id)
	{
		this.jedis=jedis;
		this.root_id=root_id;
	}
	
	public String ValidateJsonandUpdate(JsonNode given_node) throws JsonProcessingException, IOException, ProcessingException
	{
		
		
		ProcessingReport report = null;
        
		//getting the schema of the array object
		 StringBuilder sb = new StringBuilder();
		 
			
		 
		 try {
	        	
				//code to get the json schema from a locally saved file and converting to a string
				
				BufferedReader br = new BufferedReader(new FileReader("/Users/sriman/Desktop/JsonSchema/sampleschemaarray.txt"));
			    try {
			        String line = br.readLine();

			        while (line != null) {
			            sb.append(line);
			            sb.append("\n");
			            line = br.readLine();
			        }
			        
			    } finally {
			        br.close();
			    }
			    
			    
				
				//code to parse the input json string with the schema 
		        JsonNode schemaNode = JsonLoader.fromString(sb.toString());
		        JsonSchemaFactory factory = JsonSchemaFactory.byDefault(); 
		        JsonSchema schema = factory.getJsonSchema(schemaNode);
		        report = schema.validate(given_node);
		        
		 } catch (JsonParseException jpex) {
		        System.out.println("Error. Something went wrong trying to parse json data: #<#<"+given_node+
		                ">#># or json schema: @<@<"+sb+">@>@. Are the double quotes included? "+jpex.getMessage());
		        //jpex.printStackTrace();
		    } catch (ProcessingException pex) {  
		        System.out.println("Error. Something went wrong trying to process json data: #<#<"+given_node+
		                ">#># with json schema: @<@<"+sb+">@>@ "+pex.getMessage());
		        //pex.printStackTrace();
		    } catch (IOException e) {
		        System.out.println("Error. Something went wrong trying to read json data: #<#<"+given_node+
		                ">#># or json schema: @<@<"+sb+">@>@");
		        //e.printStackTrace();
		    }
		  
		System.out.println("printing result");
	    System.out.println(report.isSuccess());
	    
	    
	    if(!report.isSuccess() && given_node.has("id") )
	    {
	    	//for updating a normal object
		    //getting the id and preparing it to query from DB
			String id1 = given_node.path("id").asText();
			if(!id1.equals(root_id))
			id1 = id1 + "-" + root_id;
	
			if(jedis.exists(id1))
			{
				
				patchToDB(id1,given_node);
				return "object successfuly updated";
				
			}
			
	    }
	    else if(report.isSuccess() && given_node.has("id"))
	    {
	    	System.out.println("rootid is: "+ root_id);
	    	//for updating the array objects
	    	String id1 = given_node.path("id").asText().toString();
	    	id1 = id1 + "-" + root_id;
	    	if(jedis.exists(id1))
	    	{
	    		
	    		//update an existing one by iterating over the objects
	    		System.out.println("the array object exists, so updating the existing one");
	    		Iterator<Map.Entry<String, JsonNode>> iterator = given_node.fields();
		        while (iterator.hasNext()) {
		        	
		        	Map.Entry<String, JsonNode> temp = iterator.next();
		        	JsonNode tempnode = temp.getValue();
		        	if(tempnode.isObject())
		        	{
		        		String id2 = tempnode.path("id").asText().toString();
		        		id2 = id2 + "-" + root_id;
		        		System.out.println("it is an object");
		        		patchToDB(id2,tempnode);
		        	}   
		        	
		        }	
	    	}
	    	else
	    	{
	    		//create a new one
	    		System.out.println("the array object doesn't exist, so creating a new one");
	    		JsonValidator jsonvalidator  = new JsonValidator(jedis);
	    		Map<String,String> newnode = jsonvalidator.process2(given_node);
	    		
	    		//inserting the new map into the redis 
	    		String key = given_node.path("id").asText().toString();
	    		key = key + "-" + root_id;
	    		jedis.hmset(key, newnode);
	    		
	    		
	    		// getting the list value and pushing to it
	    		 Map<String,String> searchlistmap = jedis.hgetAll(root_id);
	    		 String mylist = "";
	    		  Iterator<Map.Entry<String, String>> iterator = searchlistmap.entrySet().iterator();
				   while(iterator.hasNext())
					{
					
						Map.Entry pair = (Map.Entry)iterator.next();
						
								if(pair.getValue().toString().contains("temp"))
								{
									mylist = pair.getValue().toString();
									break;
								}
					}
	    		 
	    		 
	    		
	    		//adding the new map reference to the list
	    		jedis.lpush(mylist, key);
	    		
	    	}
	    	
	    	return "it is an array object and it is updated successfully";
	    }
	    else
	    {
	    	
	    	return "Please give the id of the json object to be updated";
	    }
	    
		return "object successfully updated";		
			
	}		
	
	//method to patch to the Redis DB for an existing id
	public void patchToDB(String id,JsonNode given_node)
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
		
	}
}

