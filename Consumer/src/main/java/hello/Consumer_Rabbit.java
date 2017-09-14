package hello;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import redis.clients.jedis.Jedis;

public class Consumer_Rabbit {
	
	private static final String TASK_QUEUE_NAME = "uio";
	private static RedisConnection redis_conn = new RedisConnection();
	private static ElasticSearch es = new ElasticSearch();
	// private static JestClient js = getClient();
	// private static ElasticSearch es = new ElasticSearch();
	// public static Channel channel;

	public static void main(String[] argv) throws Exception {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		final Connection connection = factory.newConnection();
		final Channel channel = connection.createChannel();
		final Jedis jedis = redis_conn.getRedisConnection();
		channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
		channel.basicQos(1);
		es = new ElasticSearch();

		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");

				// es.indexData(message, id);
				System.out.println(" [x] Received '" + message + "'");

				try {
					
					String[] data = message.split("-");
					if (data[0].equals("update")) {
						
						JSONObject jsonObj = redis_conn.getObjectFromRedis(data[1]);
						es.indexData(jsonObj,data[1]);
						System.out.println(jedis.hgetAll((data[1]).toString()));
					} else if (data[0].equals("delete")) {

						es.deleteIndex(data[1]);
						System.out.println(jedis.hgetAll((data[1]).toString()));
					} else {

						JSONObject jsonObj = redis_conn.getObjectFromRedis(message);
						es.indexData(jsonObj, message);
						System.out.println(jedis.hgetAll((message).toString()));
					}

			
					// add the code to post to index

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					System.out.println(" [x] Done");
					channel.basicAck(envelope.getDeliveryTag(), false);
				}
			}
		};
		boolean autoAck = false;
		channel.basicConsume(TASK_QUEUE_NAME, autoAck, consumer);
	}

	public static JestClient getClient() {
		// Construct a new Jest client according to configuration via factory
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:9200").multiThreaded(true).build());
		JestClient client = factory.getObject();
		return client;
	}

}
