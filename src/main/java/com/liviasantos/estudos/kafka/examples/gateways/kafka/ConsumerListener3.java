package com.liviasantos.estudos.kafka.examples.gateways.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("cenario-com-retry")
public class ConsumerListener3 {

    @RetryableTopic(attempts = "5",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000)
    )
    @KafkaListener(topics = "example.topic.3")
    public void onMessage(@Header(value = KafkaHeaders.RECEIVED_PARTITION_ID) String partition,
                          @Header(KafkaHeaders.OFFSET) String offset,
                          @Header(name = KafkaHeaders.RECEIVED_MESSAGE_KEY, required = false) String key,
                          @Payload String message) {
        log.info("Message received on partition [{}], offset [{}] with key [{}]: [{}]",
                partition,
                offset,
                key,
                message);
        throw new RuntimeException("error while reading message");
    }

    @DltHandler
    public void processMessage(final String message) {
        log.info("Message will be send to DLT: {}", message);
    }
}
