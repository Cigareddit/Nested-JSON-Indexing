package hello;



import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQ extends EndPoint {

	private static String endpointName_val;

	public RabbitMQ(String endpointName) throws IOException, TimeoutException {
		
		super(endpointName);
		this.endpointName_val = endpointName;
		// TODO Auto-generated constructor stub
	}



	public boolean postMessageToQueue(String message) {
		boolean isMessagePosted = false;
		try {
			
			System.out.println("value of routing_key:"+endpointName_val);
			channel.basicPublish("", endpointName_val, null, message.getBytes());
			
			isMessagePosted = true;
		} catch (IOException e) {
			System.err.println("Error Writing to Message Queue:");
			e.printStackTrace();
		}
		return isMessagePosted;
	}


}