package edu.spring.kafka.libraryproducer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.concurrent.ListenableFutureTask;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.spring.kafka.libraryproducer.domain.Book;
import edu.spring.kafka.libraryproducer.domain.EventType;
import edu.spring.kafka.libraryproducer.domain.LibraryEvent;
import edu.spring.kafka.libraryproducer.producer.LibraryEventProducer;
@WebMvcTest(controllers = LibraryController.class)
public class LibraryEventControllerUnitTest {

	@MockBean
	private LibraryEventProducer libraryEventProducer;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper mapper;
	
	@Test
	public void testPostLibraryEvent() throws JsonProcessingException, Exception
	{
		LibraryEvent libEvent = new LibraryEvent();
		libEvent.setBook(new Book(1234, "Test Book", "Test Author"));
		
		when(libraryEventProducer.sendEventWithProducerAsync(any(LibraryEvent.class))).thenReturn(new ListenableFutureTask(new Callable<LibraryEvent>() {
			@Override
			public LibraryEvent call() throws Exception {
				return libEvent;
			}
		}));
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/library/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(libEvent)))
					.andExpect(MockMvcResultMatchers.status().isCreated());
	}
	
	@Test
	public void testPutLibraryEvent() throws JsonProcessingException, Exception
	{
		LibraryEvent libEvent = new LibraryEvent();
		libEvent.setBook(new Book(1234, "Test Book", "Test Author"));
		
		when(libraryEventProducer.sendEventWithProducerAsync(any(LibraryEvent.class))).thenReturn(new ListenableFutureTask(new Callable<LibraryEvent>() {
			@Override
			public LibraryEvent call() throws Exception {
				return libEvent;
			}
		}));
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/library/books/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(libEvent)))
					.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	
	@Test
	public void testPostLibraryEventValidationError() throws JsonProcessingException, Exception
	{
		LibraryEvent libEvent = new LibraryEvent();
		libEvent.setEvent(EventType.NEW);
		libEvent.setBook(new Book(1234, null, "Test Author"));
		
		
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/library/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(libEvent)))
					.andExpect(MockMvcResultMatchers.status().is4xxClientError());
	}
}
