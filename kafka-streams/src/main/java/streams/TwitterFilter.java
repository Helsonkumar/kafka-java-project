package streams;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import com.google.gson.JsonParser;

public class TwitterFilter {

	public static void main(String[] args) {

		// Create properties
		Properties prop = new Properties();
		prop.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		prop.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "Twitter_Filter_App");
		prop.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
		prop.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());

		// Create Topology
		StreamsBuilder streamsBuilder = new StreamsBuilder();

		// Input Topic
		KStream<String, String> inputTopic = streamsBuilder.stream("twitter_topic");
		KStream<String, String> filteredStreams = inputTopic.filter((k, json) -> extractUserFollowers(json) > 10000);

		// Send the filtered Streams to another topic
		filteredStreams.to("filtered-tweets");

		// Build the Topology
		KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), prop);

		// Start the application
		kafkaStreams.start();

	}

	private static Integer extractUserFollowers(String tweetJson) {

		try {

			return new JsonParser().parse(tweetJson).getAsJsonObject().get("user").getAsJsonObject()
					.get("followers_count").getAsInt();
		} catch (NullPointerException ex) {
			return 0;
		}

	}

}
