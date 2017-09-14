package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;



@SpringBootApplication
public class Application {
	
	//intializing the jedis pool
	public static JedisPool pool = new JedisPool(new JedisPoolConfig(),
			"localhost", 6379);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
          
    }

    
}
