package demo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class test {
    static final Logger logger = LoggerFactory.getLogger(test.class);
    // tell Spring to handle events asynchronously (not in the caller's thread) by redefining the
    // ApplicationEventMulticaster bean with id applicationEventMulticaster. With java config the method name can specify the id.
    @Bean
    ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        eventMulticaster.setErrorHandler(TaskUtils.LOG_AND_SUPPRESS_ERROR_HANDLER);
        return eventMulticaster;
    }

    static class TodoCreatedEvent {

        private String title;

        public TodoCreatedEvent(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return "TodoCreatedEvent{" +
                    "title='" + title + '\'' +
                    '}';
        }
    }

    @Component
    static class TodoEventListener {

        @EventListener
        void handle(TodoCreatedEvent event) {
            logger.info("'{}' handling todo '{}'.... print new Market Value of Portfolio", Thread.currentThread(), event);
        }

        @EventListener
        void updateDB(TodoCreatedEvent event) {
        	if(event.title.contains("2"))
        		logger.info("'{}' handling todo '{}'.... Update H2 DB with new values", Thread.currentThread(), event);
        }


    }

    @Component
    static class TodoEventProducer {

        final ApplicationEventPublisher publisher;

        public TodoEventProducer(ApplicationEventPublisher publisher) {
            this.publisher = publisher;
        }

        public void create(String todo) {
            logger.info("thread '{}' creating todo '{}'", Thread.currentThread(), todo);
            publisher.publishEvent(new TodoCreatedEvent(todo));
        }

    }

}
