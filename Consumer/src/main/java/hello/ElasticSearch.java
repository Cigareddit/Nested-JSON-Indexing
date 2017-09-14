package hello;

import java.io.IOException;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Update;

public class ElasticSearch {

	private final static String HOST_NAME = "localhost";
	private final static String INDEX_NAME = "ecommerce_app";
	private final static String TYPE = "product_plan";
	// private JestClient client = getClient();

	public ElasticSearch() {

	}

	public void indexData(JSONObject jsonObject, String id) {
		// IndexResponse response = client.
		String source = jsonObject.toString();

		Index index = new Index.Builder(jsonObject).index(INDEX_NAME).type(TYPE).id(id).build();
		
		try {
			Consumer_Rabbit.getClient().execute(index);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteIndex(String id) throws IOException {
		Delete index = new Delete.Builder(id).index(INDEX_NAME).type(TYPE).build();
		Consumer_Rabbit.getClient().execute(index);

	}

	public void updateIndex(JSONObject jsonObject, String id) throws IOException {
		String source = jsonObject.toString();
		Update index = new Update.Builder(jsonObject).index(INDEX_NAME).type(TYPE).id(id).build();
		Consumer_Rabbit.getClient().execute(index);

	}

	
	
}