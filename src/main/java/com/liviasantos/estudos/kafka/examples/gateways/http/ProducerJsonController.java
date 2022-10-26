package com.liviasantos.estudos.kafka.examples.gateways.http;

import com.liviasantos.estudos.kafka.examples.gateways.kafka.json.TopicMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Profile("cenario-com-json")
public class ProducerJsonController {

    private final KafkaTemplate<String, TopicMessage> kafkaTemplate;

    @PostMapping("/topics/{topic}/messages")
    public ResponseEntity<Void> sendToTopic(@PathVariable final String topic, @RequestBody final TopicMessage message) {
        kafkaTemplate
                .send(topic, message)
                .addCallback(handleSuccess(), handleFailure());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/topics/{topic}/keys/{key}/messages")
    public ResponseEntity<Void> sendToTopicAtKey(@PathVariable final String topic,
                                                 @PathVariable final String key,
                                                 @RequestBody final TopicMessage message) {
        kafkaTemplate
                .send(topic, key, message)
                .addCallback(handleSuccess(), handleFailure());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/topics/{topic}/keys/{key}/partitions/{partition}/messages")
    public ResponseEntity<Void> sendToTopicAtKeyAndPartition(@PathVariable final String topic,
                                                             @PathVariable final String key,
                                                             @PathVariable final Integer partition,
                                                             @RequestBody final TopicMessage message) {
        kafkaTemplate.send(topic, partition, key, message);
        return ResponseEntity.accepted().build();
    }

    private SuccessCallback<? super SendResult<String, TopicMessage>> handleSuccess() {
        return (SuccessCallback<SendResult<String, TopicMessage>>) successResult ->
                log.info("Message successfully sent: {}", successResult.getProducerRecord());
    }

    private FailureCallback handleFailure() {
        return throwable -> log.error("Fail to send message:", throwable);
    }
}
