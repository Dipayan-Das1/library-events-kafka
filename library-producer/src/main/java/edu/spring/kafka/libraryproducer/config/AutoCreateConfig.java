package edu.spring.kafka.libraryproducer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Profile("local")
@Configuration
public class AutoCreateConfig {

	/*
	 * Create library-events topic
	 */
	@Bean
	public NewTopic libraryTopic()
	{
		return TopicBuilder.name("library-events").partitions(2).replicas(2).build();
	}
}
