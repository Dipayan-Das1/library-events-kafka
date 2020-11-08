package edu.spring.kafka.libraryproducer.controller;

import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.spring.kafka.libraryproducer.domain.EventType;
import edu.spring.kafka.libraryproducer.domain.LibraryEvent;
import edu.spring.kafka.libraryproducer.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/api/library/books")
public class LibraryController {
	
	@Autowired
	private LibraryEventProducer libraryEventProducer;
	
	@PostMapping(produces=MediaType.APPLICATION_JSON_VALUE,consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LibraryEvent> createBook(@RequestBody @Valid  LibraryEvent libraryEvent) throws Exception
	{
		//libraryEventProducer.sendEvent(libraryEvent);
		libraryEvent.setLibraryEventId(null);
		libraryEvent.setEvent(EventType.NEW);
		libraryEventProducer.sendEventWithProducerAsync(libraryEvent);
		return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
	}
	
	@PutMapping(path="/{id}",produces=MediaType.APPLICATION_JSON_VALUE,consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LibraryEvent> updateBook(@PathVariable("id") String id,@RequestBody @Valid  LibraryEvent libraryEvent) throws Exception
	{
		try
		{
			libraryEvent.setLibraryEventId(Integer.parseInt(id));	
		}
		catch(NumberFormatException ex)
		{
			throw new IllegalArgumentException(ex.getMessage());
		}
		
		libraryEvent.setEvent(EventType.UPDATE);
		libraryEventProducer.sendEventWithProducerAsync(libraryEvent);
		return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity handleException(MethodArgumentNotValidException ex)
	{
		String message = ex.getBindingResult().getFieldErrors().stream().map(err -> {
			return err.getDefaultMessage();
		}).collect(Collectors.joining(","));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity handleException(IllegalArgumentException ex)
	{
		String message = ex.getMessage();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
	}

}
