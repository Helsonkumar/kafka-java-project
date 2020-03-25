package begin;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerWithCallBackKey {

	public static void main(String[] args) {

		final Logger logger = LoggerFactory.getLogger(ProducerWithCallBackKey.class);

		Properties prop = new Properties();
		prop.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		prop.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		prop.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(prop);

		// If key is not given for a record being sent , then the record is randomly
		// distributed in Round Robin fashion
		
		// If a key is given then the record with the same key is always guaranteed to
		// be stored in the specific partition
		for (int i = 0; i < 10; i++) {

			String key = "Id_" + Integer.toString(i);

			ProducerRecord<String, String> record = new ProducerRecord<String, String>("java-topic", key,
					Integer.toString(i));

			logger.info("Key:" + key);

			// Send is an Asynchronous call and it returns back after the message is placed
			// in the buffer which holds the records waiting in queue to be sent to the
			// partition.
			
			// But the CallBack loop is executed once the record is actually acknowledged
			// after getting written in the partition
			producer.send(record, new Callback() {
				public void onCompletion(RecordMetadata meta, Exception ex) {

					if (ex == null) {
						logger.info("\n" + "---------------------" + "\n" + meta.topic() + "\n" + "partition:" + meta.partition() + "\n" + "Offset: "
								+ meta.offset() + "\n" + "Timestamp :" + meta.timestamp() + "\n" + "-------------------");
					} else {
						logger.error("Error while producing " + ex);
					}

				}

			});
		}

		producer.close();
		producer.flush();

	}

}
