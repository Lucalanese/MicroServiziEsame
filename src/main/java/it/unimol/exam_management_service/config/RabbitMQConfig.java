package it.unimol.exam_management_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange examExchange() {
        return new TopicExchange("exam-events");
    }

    @Bean
    public Queue examCreatedQueue() {
        return new Queue("exam.created.queue");
    }

    @Bean
    public Queue examUpdatedQueue() {
        return new Queue("exam.updated.queue");
    }

    @Bean
    public Queue examDeletedQueue() {
        return new Queue("exam.deleted.queue");
    }

    @Bean
    public Queue enrollmentRequestedQueue() {
        return new Queue("exam.enrollment.requested.queue");
    }

    @Bean
    public Queue enrollmentApprovedQueue() {
        return new Queue("exam.enrollment.approved.queue");
    }

    @Bean
    public Queue enrollmentRejectedQueue() {
        return new Queue("exam.enrollment.rejected.queue");
    }

    @Bean
    public Queue gradeRecordedQueue() {
        return new Queue("exam.grade.recorded.queue");
    }

    @Bean
    public Queue gradeUpdatedQueue() {
        return new Queue("exam.grade.updated.queue");
    }

    @Bean
    public Queue examCompletedQueue() {
        return new Queue("exam.completed.queue");
    }

    @Bean
    public Binding examCreatedBinding(Queue examCreatedQueue, TopicExchange examExchange) {
        return BindingBuilder.bind(examCreatedQueue).to(examExchange).with("exam.created");
    }

    @Bean
    public Binding examUpdatedBinding(Queue examUpdatedQueue, TopicExchange examExchange) {
        return BindingBuilder.bind(examUpdatedQueue).to(examExchange).with("exam.updated");
    }

    @Bean
    public Binding examDeletedBinding(Queue examDeletedQueue, TopicExchange examExchange) {
        return BindingBuilder.bind(examDeletedQueue).to(examExchange).with("exam.deleted");
    }

    @Bean
    public Binding enrollmentRequestedBinding(Queue enrollmentRequestedQueue, TopicExchange examExchange) {
        return BindingBuilder.bind(enrollmentRequestedQueue).to(examExchange).with("exam.enrollment.requested");
    }

    @Bean
    public Binding enrollmentApprovedBinding(Queue enrollmentApprovedQueue, TopicExchange examExchange) {
        return BindingBuilder.bind(enrollmentApprovedQueue).to(examExchange).with("exam.enrollment.approved");
    }

    @Bean
    public Binding enrollmentRejectedBinding(Queue enrollmentRejectedQueue, TopicExchange examExchange) {
        return BindingBuilder.bind(enrollmentRejectedQueue).to(examExchange).with("exam.enrollment.rejected");
    }

    @Bean
    public Binding gradeRecordedBinding(Queue gradeRecordedQueue, TopicExchange examExchange) {
        return BindingBuilder.bind(gradeRecordedQueue).to(examExchange).with("exam.grade.recorded");
    }

    @Bean
    public Binding gradeUpdatedBinding(Queue gradeUpdatedQueue, TopicExchange examExchange) {
        return BindingBuilder.bind(gradeUpdatedQueue).to(examExchange).with("exam.grade.updated");
    }

    @Bean
    public Binding examCompletedBinding(Queue examCompletedQueue, TopicExchange examExchange) {
        return BindingBuilder.bind(examCompletedQueue).to(examExchange).with("exam.completed");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}