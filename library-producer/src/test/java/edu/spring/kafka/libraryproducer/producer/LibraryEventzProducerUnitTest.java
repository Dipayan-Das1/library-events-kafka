package edu.spring.kafka.libraryproducer.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.spring.kafka.libraryproducer.domain.Book;
import edu.spring.kafka.libraryproducer.domain.EventType;
import edu.spring.kafka.libraryproducer.domain.LibraryEvent;
@ExtendWith(MockitoExtension.class)
public class LibraryEventzProducerUnitTest {

	@InjectMocks
	private LibraryEventProducer libEventproducer;
	
	@Mock
	private KafkaTemplate<Integer, String> kafkaTemplate;
	@Spy
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Test
	public void testSendEventWithProducerAsyncException() throws JsonProcessingException
	{
		LibraryEvent libEvent = new LibraryEvent();
		libEvent.setEvent(EventType.NEW);
		libEvent.setBook(new Book(1234, "Test Book", "Test Author"));
		SettableListenableFuture future = new SettableListenableFuture();
		future.setException(new RuntimeException("Test kafka exception"));
		when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);
		ListenableFuture<SendResult<Integer, String>>  result = libEventproducer.sendEventWithProducerAsync(libEvent);
		assertThrows(ExecutionException.class, () -> {
			result.get();
		} );
	}
	
	@Test
	public void testSendEventWithProducerAsync() throws JsonProcessingException, InterruptedException, ExecutionException
	{
		LibraryEvent libEvent = new LibraryEvent();
		libEvent.setEvent(EventType.NEW);
		libEvent.setBook(new Book(1234, "Test Book", "Test Author"));
		SettableListenableFuture future = new SettableListenableFuture();
		ProducerRecord<Integer, String> producerRecord = new ProducerRecord<Integer, String>("library-events", null, objectMapper.writeValueAsString(libEvent));
		RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("library-events", 0), 1L, 2L, System.currentTimeMillis(),100l, 32, 1000);
		SendResult<Integer, String> result = new SendResult<Integer, String>(producerRecord, recordMetadata);
		future.set(result);
		when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);
		ListenableFuture<SendResult<Integer, String>>  response = libEventproducer.sendEventWithProducerAsync(libEvent);
		String value = response.get().getProducerRecord().value();
		LibraryEvent event = objectMapper.readValue(value, LibraryEvent.class);
		assertEquals("Test Book", event.getBook().getName());
	}
	
	
	
}
