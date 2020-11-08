package edu.spring.kafka.libraryproducer.domain;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class LibraryEvent {	
private Integer libraryEventId;
@NotNull(message = "Book must not be null")
@Valid
private Book book;
private EventType event;
}
