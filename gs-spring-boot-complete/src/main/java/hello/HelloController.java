package hello;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import redis.clients.jedis.Jedis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.exceptions.ProcessingException;



@RestController
@RequestMapping("/")
public class HelloController {
	
	RabbitMQ rabbitMQ = null;
	public static Jedis jedis;
   
    public String index() {
        return "Greetings from Spring Boot!";
    }
    
    
    

	public HelloController() throws IOException, TimeoutException {

		this.jedis = getRedisConnection();
		rabbitMQ = new RabbitMQ("uio");

	}

	public Jedis getRedisConnection() {
		Jedis jedis = null;
		jedis = Application.pool.getResource();
		return jedis;
	}

	
	//post method
	
	@RequestMapping(value = "/insert", method = RequestMethod.POST)
	@ResponseBody
	public String storeJson(@RequestBody String jsonObject, HttpServletRequest request, HttpServletResponse response) throws org.json.simple.parser.ParseException, JsonParseException, JsonMappingException, IOException, ParseException 
			{
		
		
		//getting the authorization header and checking if it is valid
		String userToken = request.getHeader("Authorization");
		JSONObject jsonobj = checkValidAccess(userToken);

		if (!jsonobj.isEmpty() && (jsonobj.get("role").equals("user") || jsonobj.get("role").equals("admin"))) {
				//now validating the schema
				JsonValidator jv = new JsonValidator(jedis);
				if(jv.jsonValidate(jsonObject))
				{
					
					rabbitMQ.postMessageToQueue(JsonValidator.rootid);
					return "object successfully inserted";
				}
				return "JSON SYNTAX ERROR or THE ID ALREADY EXISTS";
			}//end of inner if
			else {
			response.setStatus(401);
			return "Not Authorized";
		}

	}
	
	
	//retrieve method
	
	@RequestMapping(value = "/display/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> getJson(@PathVariable("id") String id,@RequestHeader("If-NoNe-Match") String etag)
			throws IOException, ParseException {
		
		
		HttpHeaders headers = new HttpHeaders();
		//only return if etag doesn't matches
		if(jedis.get(id + "-" + "ETAG").toString().equals(etag))
		{
			headers.setETag(etag);
			return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
			
		}
		else
		{
					getJsonClass gjson = new getJsonClass();
					JsonNode jsonNode = gjson.getJson1(id, jedis);
					headers.setETag(jedis.get(id + "-" + "ETAG"));
					return new ResponseEntity<>(jsonNode,headers, HttpStatus.OK);
		}
		
		 
		
			

	}
	
	
	
	@ExceptionHandler({ org.springframework.http.converter.HttpMessageNotReadableException.class })
	@RequestMapping(value = "/ecommerce/token", method = RequestMethod.POST)
	@ResponseBody
	public String getToken(@Valid @RequestBody(required = false) JSONObject jsonObject, HttpServletResponse response)
			throws JSONException, FileNotFoundException {

		if (null != jsonObject) {

			String token = Encryptor.encryptData(jsonObject.toString());


			return token;

		} else {
			response.setStatus(400);
			return null;
		}

	}

	public JSONObject checkValidAccess(String token)
			throws JsonParseException, JsonMappingException, IOException, ParseException, org.json.simple.parser.ParseException {
		JSONObject jsonobj = new JSONObject();
		if (null != token && (token.length() % 2) == 0) {

			String json = Encryptor.decryptData(token);
			JSONParser jp = new JSONParser();

			jsonobj = (JSONObject) jp.parse(json);

			String role = (String) jsonobj.get("role");
			String id = (String) jsonobj.get("id");


		}

		return jsonobj;

	}

	
	
	
	//delete method
	
		@RequestMapping(value = "/del/{id}", method = RequestMethod.DELETE)
		@ResponseBody
		public String deleteJson(@PathVariable("id") String id)
				throws IOException, ParseException {

			jedis.del(id);
			String idToDelete= "delete"+"-"+id;
			rabbitMQ.postMessageToQueue(idToDelete);
			return "Value for this key successfully deleted";	

		}
		
	
	//update or patch or merge method
		
		@RequestMapping(value = "/upd/{id}", method = RequestMethod.PATCH)
		@ResponseBody
		public String updateJson(@PathVariable("id") String id,@RequestBody JsonNode given_node) throws JsonProcessingException, IOException, ProcessingException 
				{
			
			JsonUpdate2 updatedjson2 = new JsonUpdate2(jedis,id);
			String result = updatedjson2.ValidateJsonandUpdate(given_node);
			if(result!=null)
			{
				
				EncodeDecode pass = new EncodeDecode();
				pass.updateencode(id,jedis);
				String idToUpdate= "update"+"-"+id;
				rabbitMQ.postMessageToQueue(idToUpdate);
				return result;
			}
			return "No value exists for this key";
			
		}

}
