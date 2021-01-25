package com.nibado.example.saga.mock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

/*
 * A class that basically simulates Kafka.
 */

@Component
public class TopicFactory {
    private final Map<String, Topic> topics = new HashMap<>();

    public Topic getTopic(String topicId) {
        return topics.computeIfAbsent(topicId, key -> new Topic());
    }

    public void subScribe(String topicId, Consumer<Message> callback) {
        getTopic(topicId).consumers.add(callback);
    }

    @Scheduled(fixedRate = 100L)
    public void process() {
        topics.values().forEach(Topic::processOne);
    }

    public static class Topic {
        private final Deque<Message> queue = new ArrayDeque<>();
        private final List<Consumer<Message>> consumers = new ArrayList<>();

        public void write(String id, byte[] data) {
            synchronized (queue) {
                queue.offer(new Message(id, data));
            }
        }

        private void processOne() {
            synchronized (queue) {
                var data = queue.poll();
                if(data != null) {
                    consumers.forEach(c -> c.accept(data));
                }
            }
        }
    }

    public record Message(String id, byte[] data) {}
}
