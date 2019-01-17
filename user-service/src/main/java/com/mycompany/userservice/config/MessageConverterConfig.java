package com.mycompany.userservice.config;

import org.springframework.cloud.stream.annotation.StreamMessageConverter;
import org.springframework.cloud.stream.schema.avro.AvroSchemaMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;

@Configuration
public class MessageConverterConfig {

    @Bean
    @StreamMessageConverter
    MessageConverter messageConverter() {
        return new AvroSchemaMessageConverter();
    }

}
