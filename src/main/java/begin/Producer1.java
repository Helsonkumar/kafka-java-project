package begin;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class Producer1 {
	
	public static void main(String args[]) {
		
		String bootstrap = "127.0.0.1:9092";
		//Set Producer Configuration
		Properties prop = new Properties();
		prop.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
		prop.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		prop.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		
		//Create the producer 
		KafkaProducer<String,String> producer = new KafkaProducer<String,String>(prop);	
		
		//Create the producer record
		
		ProducerRecord<String,String> record = new ProducerRecord<String,String>("java-topic","Hi..This is a message from Java Program");
		
		producer.send(record);
		producer.close();
		
	}

}
