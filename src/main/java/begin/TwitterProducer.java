package begin;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class TwitterProducer {

	static BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		String consumerKey = "DctvGGcq4412aTwImdSqddj5Z";
		String consumerSecret = "D3XigpsBKSiUSQ7YP4xJZKlopCtlMfskTAX52PnCpGINh1PGXJ";
		String token = "1485623630-7yFBLEUKmjkgxajVgPSDwFySSUIkI8hVFaSdGin";
		String secret = "WKOCmzTdD25WVE2ayRWFheELOqKpC0OzsGP9WhRgwzBXK";

		final Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());

		Client twitter_client = clientProducer(consumerKey, consumerSecret, token, secret);

		twitter_client.connect();

		KafkaProducer producer = createProducer();

		while (!twitter_client.isDone()) {
			String msg = null;
			try {
				msg = msgQueue.poll(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (msg != null) {
				logger.info("Sending Message to Prodcuer :  " + msg);

				producer.send(new ProducerRecord<String, String>("twitter_topic", msg), new Callback() {
					public void onCompletion(RecordMetadata meta, Exception ex) {
						logger.info("Topic :" + meta.topic());
						logger.info("Partition" + meta.partition());
						logger.info("Offset" + meta.offset());
						logger.info("Timestamp" + meta.timestamp());
					}

				});

			}
		}

		logger.info("End of Twitter Tweets");

	}

	static public Client clientProducer(String consumerKey, String consumerSecret, String token, String secret) {

		// ****Creating a Twitter Client : We use hbc-core module instead of twitter4j

		/**
		 * Declare the host you want to connect to, the endpoint, and authentication
		 * (basic auth or oauth)
		 */
		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

		List<String> terms = Lists.newArrayList("Coronavirus");
		hosebirdEndpoint.trackTerms(terms);

		Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

		ClientBuilder builder = new ClientBuilder().name("Helson-Twitter-Client-01").hosts(hosebirdHosts)
				.authentication(hosebirdAuth).endpoint(hosebirdEndpoint)
				.processor(new StringDelimitedProcessor(msgQueue));

		Client hosebirdClient = builder.build();
		return hosebirdClient;

	}

	static KafkaProducer<String, String> createProducer() {

		Properties prop = new Properties();
		prop.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		prop.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		prop.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// Safe Producer config
		prop.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
		prop.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
		prop.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
		prop.setProperty(ProducerConfig.ACKS_CONFIG, "all");

		// High Throughput producer config
		prop.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
		prop.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
		prop.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024));

		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(prop);
		return producer;
	}

}
