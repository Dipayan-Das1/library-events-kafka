package edu.spring.kafka.libraryproducer.producer;

import java.util.Arrays;
import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.spring.kafka.libraryproducer.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LibraryEventProducer {

	@Autowired
	private KafkaTemplate<Integer, String> kafkaTemplate;
	@Autowired
	private ObjectMapper objectMapper;
	
	String topic = "library-events";

	/*
	 * Asynchronous message processing
	 */
	public void sendEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
		Integer key = libraryEvent.getLibraryEventId();
		ListenableFuture<SendResult<Integer, String>> response = kafkaTemplate.sendDefault( key, objectMapper.writeValueAsString(libraryEvent));
		
		response.addCallback((SendResult<Integer, String> result) -> {
			log.info("Successfully sent message for key {} in partition {} ",result.getProducerRecord().key(),result.getRecordMetadata().partition());
			
			
		}, (Throwable ex) -> {
				log.error("Error while sending message ",ex);
				throw new RuntimeException(ex);
				
			}
		);
	}
	
	public ListenableFuture<SendResult<Integer, String>> sendEventWithProducerAsync(LibraryEvent libraryEvent) throws JsonProcessingException {
		Integer key = libraryEvent.getLibraryEventId();
		
		
		Header header = new RecordHeader("event-source", "scanner".getBytes());
		List<Header> headers = Arrays.asList(header);
		
		//headers can ve null
		ProducerRecord<Integer,String> producer = new ProducerRecord<Integer, String>(topic, null, key, objectMapper.writeValueAsString(libraryEvent), headers);
		ListenableFuture<SendResult<Integer, String>> response = kafkaTemplate.send(producer);
		
		response.addCallback((SendResult<Integer, String> result) -> {
			log.info("Successfully sent message for key {} in partition {} ",result.getProducerRecord().key(),result.getRecordMetadata().partition());
			
			
		}, (Throwable ex) -> {
				log.error("Error while sending message ",ex);
				throw new RuntimeException(ex);
				
			}
		);
		return response;
	}
	
	/*
	 * Synchronous message processing
	 */
	public SendResult<Integer, String> sendEventSync(LibraryEvent libraryEvent) throws Exception {
		Integer key = libraryEvent.getLibraryEventId();
		try
		{
		SendResult<Integer, String> result = kafkaTemplate.sendDefault( key, objectMapper.writeValueAsString(libraryEvent)).get();
		//.get allows timeout param
		log.info("Successfully sent message for key {} in partition {} ",result.getProducerRecord().key(),result.getRecordMetadata().partition());
		return result;
		}
		catch(Exception e)
		{
			log.error("Error while sending message ",e);
			throw e;
		}		
	}

}
