package begin;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Consumer1 {
	
	public static void main(String[] args) {
		
		Logger logger = LoggerFactory.getLogger(Consumer1.class.getName());
		
		Properties prop = new Properties();
		
		//Set properties for consumer
		prop.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		prop.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		prop.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		prop.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "Consumer-Group-2");
		//This can have possible values of "earliest/latest/none"
		//Running the same consumer with the same group-id would not fetcht the records once again from the beginning.
		// The consumed offset for a consumer group is committed to a system topic . 
		//If to re consume the messages from the topic from the beginning, then change the group-id and then run the consumer
		prop.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		
		//Create a consumer
		KafkaConsumer<String,String> consumer = new KafkaConsumer<String,String>(prop);
		
		
		//Subscribe to a topic
		//Can be subscribed to a Topic altogether or a specific partition within a Topic
		// Or even for a list of Topics (given in a collection)
		consumer.subscribe(Collections.singleton("java-topic"));
		
		while(true) {
			// Poll for records. 
			// Records are consumed in sequential order from the last consumed Offset. 
			// To shift the offset to some other value we can use "seek" method of the KafkaConsumer
			
			// Poll waits for given timeout parameter value. If the records are not there till then the method return empty result set.
			// If the records are present then the method returns with the record immediately 
			ConsumerRecords<String,String> records = consumer.poll(Duration.ofMillis(100));
			
			for(ConsumerRecord<String,String> record : records) {
				logger.info("\n" + "Key: " + record.key() + "\n"  + "Value :" + record.value() + "\n" +  "Topic: " + record.topic() + "\n" + record.partition() + "\n" + record.offset() + "\n"  + record.timestamp() +"\n");
			}
		}
		
		
		
	}
	

}
