package begin;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerWithAssignSeek {

	public static void main(String[] args) {

		Logger logger = LoggerFactory.getLogger(ConsumerWithAssignSeek.class.getName());

		Properties prop = new Properties();

		// Set properties for consumer
		prop.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		prop.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		prop.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

		// prop.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		// Create a consumer
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(prop);

		TopicPartition partition = new TopicPartition("java-topic", 2);

		long offsetToReadFrom = 24L;

		// Assigning the specific partition to read from
		consumer.assign(Arrays.asList(partition));

		consumer.seek(partition, offsetToReadFrom);

		long noOfMessagesToRead = 10L;
		long countOfMessagesRead = 0L;
		boolean keepOnReading = true;

		while (keepOnReading) {

			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

			for (ConsumerRecord<String, String> record : records) {
				countOfMessagesRead += 1;
				logger.info("\n" + "Key: " + record.key() + "\n" + "Value :" + record.value() + "\n" + "Topic: "
						+ record.topic() + "\n" + record.partition() + "\n" + record.offset() + "\n"
						+ record.timestamp() + "\n");

				if (countOfMessagesRead >= noOfMessagesToRead) {
					keepOnReading = false;
					break;
				}
			}
		}

		consumer.close();

	}

}
