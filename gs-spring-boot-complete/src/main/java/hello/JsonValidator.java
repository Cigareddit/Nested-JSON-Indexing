package hello;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
















import redis.clients.jedis.Jedis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ProcessingMessage;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.JsonLoader;

public class JsonValidator {
	
	public static int n = 0;
	public static int k = 0;
	public String listname = new String();
	public Jedis jedis;
	public static String rootid;
	
	public JsonValidator(Jedis jedis)
	{
		this.n = 0;
		this.jedis = jedis;
	}
		
	public boolean jsonValidate(String jsonObject) throws ParseException 
	{
		 
		
		 ProcessingReport report = null;
		 boolean result = false;
		 StringBuilder sb = new StringBuilder();
		
		 
		try {
	        	
			//code to get the json schema from a locally saved file and converting to a string
			sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new FileReader("/Users/sriman/Desktop/JsonSchema/sampleschema.txt"));
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
	        JsonNode data = JsonLoader.fromString(jsonObject);        
	        JsonSchemaFactory factory = JsonSchemaFactory.byDefault(); 
	        JsonSchema schema = factory.getJsonSchema(schemaNode);
	        report = schema.validate(data);
	        
	        
	        if(!jedis.exists(data.path("id").asText().toString()))
	        {
	        	System.out.println("hello");
		        //call the function to process the json data to store it in redis
		      // process(jsonObject);
		        rootid = data.path("id").asText();
		        jedis.hmset(rootid, process2(data));
		        
		        //encoding for etag and storing it to the redis
		        EncodeDecode pass = new EncodeDecode();
				pass.encode(data,jedis);
	        }
	        else
	        {
	        	return false;
	        }
			
	       
	        
    
	    } catch (JsonParseException jpex) {
	        System.out.println("Error. Something went wrong trying to parse json data: #<#<"+jsonObject+
	                ">#># or json schema: @<@<"+sb+">@>@. Are the double quotes included? "+jpex.getMessage());
	        //jpex.printStackTrace();
	    } catch (ProcessingException pex) {  
	        System.out.println("Error. Something went wrong trying to process json data: #<#<"+jsonObject+
	                ">#># with json schema: @<@<"+sb+">@>@ "+pex.getMessage());
	        //pex.printStackTrace();
	    } catch (IOException e) {
	        System.out.println("Error. Something went wrong trying to read json data: #<#<"+jsonObject+
	                ">#># or json schema: @<@<"+sb+">@>@");
	        //e.printStackTrace();
	    }
        
        if (report != null) {
            Iterator<ProcessingMessage> iter = report.iterator();
            while (iter.hasNext()) {
                ProcessingMessage pm = iter.next();
                System.out.println("Processing Message: "+pm.getMessage());
            }
            result = report.isSuccess();
        }
        System.out.println(" Result=" +result);
        return result;
        
	    
	    
		  
		  
		  
		} 
	
	
	
	
	public HashMap<String,String> process2(JsonNode jsonObject) 
	{
		
		HashMap<String,String> rootmap = new HashMap<String, String>();
        Iterator<Map.Entry<String, JsonNode>> iterator = jsonObject.fields();
        while (iterator.hasNext()) {
            
        	Map.Entry<String, JsonNode> temp = iterator.next();
        	JsonNode tempnode = temp.getValue();
        	String keytostore = temp.getKey();
        	System.out.println("keytostore is:"+keytostore);
        	
        	if(tempnode.isArray())
        	{
        		if(listname.length()==0)
        		{
        			System.out.println("I am here");
        			k++;
        			listname = "temp" + Integer.toString(k);
        		}
        		
        		//storing the objects in the list
        		for(JsonNode objNode : tempnode)
        		{
        			
        			HashMap<String,String> temp1 = process2(objNode);
        			String str = "obj" + Integer.toString(n);
            		jedis.hmset(objNode.path("id").asText() + "-" + rootid, temp1);
            		System.out.println(listname);
        			jedis.lpush(listname,objNode.path("id").asText() + "-" + rootid);
        			n++;
        		}
        		
        		//String key = "object" + Integer.toString(k); 
        		rootmap.put(keytostore, listname);
        			
        	}
        	else if(tempnode.isObject())
        	{
        		// see if the node contains another object
        		k++;
        		
        		//String key = "object" + Integer.toString(k); 
        		HashMap<String,String> temp1 = process2(tempnode);
        		rootmap.put(keytostore,tempnode.path("id").asText() + "-" + rootid);
        		jedis.hmset(tempnode.path("id").asText() + "-" + rootid, temp1);
        		
        	}
        	else
        	{
        		
        		rootmap.put(keytostore,temp.getValue().asText());
        	}
        	
   	        	
        }
        
        
        return rootmap;
			
		
	}
	
	
	
	}
	
	

