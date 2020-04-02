package elastic;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Elastic_Consumer {

	public static void main(String[] args) throws IOException, InterruptedException {

		Logger logger = LoggerFactory.getLogger(Elastic_Consumer.class.getName());
		RestHighLevelClient client = createClient();
		String jsonString = "{\"foo\": \"boobar\"}";

		KafkaConsumer<String, String> consumer = createConsumer();

		while (true) {

			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));

			logger.info("No of records in the current batch :" + records.count());

			Thread.sleep(1000);

			BulkRequest bulkRequest = new BulkRequest();

			for (ConsumerRecord<String, String> record : records) {

				String id_val = record.topic() + "_" + record.partition() + "_" + record.offset();

				@SuppressWarnings("deprecation")
				IndexRequest request = new IndexRequest("helson_twitter", "tweet").source(record.value(),
						XContentType.JSON);

				// Sending bulk request to Elastic Search
				bulkRequest.add(request);

			}

			BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

			logger.info("Commiting Offset");
			consumer.commitSync();
			logger.info("Offsets have been commited");
		}
	}

	public static RestHighLevelClient createClient() {

		String hostname = "helson-2020-java-1990020618.ap-southeast-2.bonsaisearch.net";
		String username = "zn0b05jshv";
		String pwd = "himq7z2dc9";

		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, pwd));

		RestClientBuilder builder = RestClient.builder(new HttpHost(hostname, 443, "https"))
				.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {

					public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
						// TODO Auto-generated method stub
						return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
					}
				});

		RestHighLevelClient client = new RestHighLevelClient(builder);
		return client;

	}

	public static KafkaConsumer<String, String> createConsumer() {

		Logger logger = LoggerFactory.getLogger(Elastic_Consumer.class.getName());

		Properties prop = new Properties();

		// Set properties for consumer
		prop.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		prop.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		prop.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		prop.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "Elastic-Consumer");

		prop.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		prop.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		// prop.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");

		// Create a consumer
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(prop);

		consumer.subscribe(Arrays.asList("twitter_topic"));

		return consumer;

	}
}
