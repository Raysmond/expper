package com.expper.config;

import com.expper.service.RabbitConsumer;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Configuration
@AutoConfigureAfter(value = RedisConfiguration.class)
public class RabbitmqConfiguration {
    /**
     * The queue cannot contain '.', for example: "expper.post-create". Or it will cause the following
     * exception: (still don't know why) Failed to invoke target method 'handleCreatePost' with
     * argument type = [class [B], value = [{[B@43e59e85}]
     */
    public final static String QUEUE_CREATE_POST = "expper-post-create";

    public final static String QUEUE_PUBLIC_POST = "expper-post-public";

    public final static String QUEUE_GET_ARTICLE = "expper-crawl-article";

    public final static String QUEUE_UPDATE_POST_SCORE = "expper-update-post-score";

    public final static String QUEUE_SEND_EMAIL = "expper-send-email";

    public final static String QUEUE_ADD_MESSAGE = "expper-message";

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    Queue queue() {
        return new Queue(QUEUE_PUBLIC_POST, false);
    }

    @Bean
    Queue createPostQueue() {
        return new Queue(QUEUE_CREATE_POST, false);
    }

    @Bean
    Queue crawlArticleQueue() {
        return new Queue(QUEUE_GET_ARTICLE, false);
    }

    @Bean
    Queue updatePostScoreQueue() {
        return new Queue(QUEUE_UPDATE_POST_SCORE, false);
    }

    @Bean
    Queue sendEmailQueue() {
        return new Queue(QUEUE_SEND_EMAIL, false);
    }

    @Bean
    Queue messageQueue() {
        return new Queue(QUEUE_ADD_MESSAGE, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("expper-exchange");
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(QUEUE_PUBLIC_POST);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(
            queue(),
            messageQueue(),
            createPostQueue(),
            crawlArticleQueue(),
            updatePostScoreQueue(),
            sendEmailQueue());
        container.setMessageListener(listenerAdapter);
        container.setMaxConcurrentConsumers(4);
        return container;
    }

    @Bean
    RabbitConsumer receiver() {
        return new RabbitConsumer();
    }

    @Bean
    MessageListenerAdapter listenerAdapter(RabbitConsumer receiver) {
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(receiver);
        listenerAdapter.addQueueOrTagToMethodName(QUEUE_GET_ARTICLE, "getArticle");
        listenerAdapter.addQueueOrTagToMethodName(QUEUE_UPDATE_POST_SCORE, "updatePostScore");
        listenerAdapter.addQueueOrTagToMethodName(QUEUE_SEND_EMAIL, "sendEmail");
        listenerAdapter.addQueueOrTagToMethodName(QUEUE_ADD_MESSAGE, "addMessage");
        listenerAdapter.setMessageConverter(new SerializerMessageConverter());
        return listenerAdapter;
    }
}
