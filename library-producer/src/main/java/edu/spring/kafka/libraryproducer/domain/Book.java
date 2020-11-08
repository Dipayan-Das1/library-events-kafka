package edu.spring.kafka.libraryproducer.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Book {
	@NotNull(message="ISBN must not be null")
	private Integer isbn;
	@NotBlank(message="Book Name can not be blank")
	private String name;
	@NotBlank(message="Author Name can not be blank")
	private String author;
}
