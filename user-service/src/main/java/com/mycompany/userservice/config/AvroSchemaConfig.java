package com.mycompany.userservice.config;

import org.springframework.cloud.stream.annotation.StreamMessageConverter;
import org.springframework.cloud.stream.schema.avro.AvroSchemaMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.MimeType;

@Configuration
public class AvroSchemaConfig {

    @Bean
    @StreamMessageConverter
    MessageConverter userMessageConverter() {
        return new AvroSchemaMessageConverter(MimeType.valueOf("application/*+avro"));
    }

}
