package edu.spring.kafka.libraryproducer.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.spring.kafka.libraryproducer.domain.Book;
import edu.spring.kafka.libraryproducer.domain.EventType;
import edu.spring.kafka.libraryproducer.domain.LibraryEvent;

@ActiveProfiles(profiles = "test")
@EmbeddedKafka(topics = {"library-events"},partitions = 2)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.admin.bootstrap.servers=${spring.embedded.kafka.brokers}"})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class LibraryEventControllerTest {

	@Autowired
	TestRestTemplate testRestTemplate;
	@Autowired
	EmbeddedKafkaBroker embeddedKafkaBroker;
	@Autowired
	private ObjectMapper mapper;
	
	private Consumer<Integer,String> consumer;
	
	@BeforeEach
	public void setup()
	{
		Map<String,Object> config = new HashMap<>(KafkaTestUtils.consumerProps("group1","true", embeddedKafkaBroker));
		consumer = new DefaultKafkaConsumerFactory<>(config,new IntegerDeserializer() , new StringDeserializer()).createConsumer();
		embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
	}
	
	@AfterEach
	public void tearDown()
	{
		consumer.close();
	}
	
	@Test
	public void postLibraryEvent() throws JsonMappingException, JsonProcessingException
	{
		LibraryEvent libEvent = new LibraryEvent();
		libEvent.setBook(new Book(1234, "Test Book", "Test Author"));
		ResponseEntity<Void> response = testRestTemplate.postForEntity("/api/library/books", libEvent, Void.class);
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);
		ConsumerRecord<Integer, String> record = KafkaTestUtils.getSingleRecord(consumer, "library-events");
		String value = record.value();
		LibraryEvent event = mapper.readValue(value, LibraryEvent.class);
		assertEquals(1234, event.getBook().getIsbn());
		System.out.println(value);
	}
	
	@Test
	public void updateLibraryEvent() throws JsonMappingException, JsonProcessingException
	{
		LibraryEvent libEvent = new LibraryEvent();
		libEvent.setBook(new Book(1234, "Test Book", "Test Author"));
		
		ResponseEntity response = testRestTemplate.exchange("/api/library/books/1", HttpMethod.PUT, new HttpEntity<LibraryEvent>(libEvent), LibraryEvent.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		ConsumerRecord<Integer, String> record = KafkaTestUtils.getSingleRecord(consumer, "library-events");
		String value = record.value();
		LibraryEvent event = mapper.readValue(value, LibraryEvent.class);
		assertEquals(1234, event.getBook().getIsbn());
		assertEquals(1, event.getLibraryEventId());
		System.out.println(value);
	}
	
}
