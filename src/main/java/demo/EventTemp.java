package demo;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class EventTemp {
    @Bean
    AListenerBean listenerBean () {
        return new AListenerBean();
    }


    public static void main (String[] args) {

        AnnotationConfigApplicationContext context =
                            new AnnotationConfigApplicationContext(
                            		EventTemp.class);
        context.publishEvent(new MyEvent(1, "test message 1"));
        context.publishEvent(new MyEvent(5, "test message 5"));

        context.close();
    }

    private static class AListenerBean {

        @EventListener(condition = "#myEvent.code == 5")
        public void handleContextEvent (MyEvent myEvent) {
            System.out.println("event received: " + myEvent);
        }
        
        @EventListener
        public void handleContextEventB (MyEvent myEvent) {
            System.out.println("event received: " + myEvent);
        }
    }
    
   
    private static class MyEvent {
        private String msg;
        private int code;


        public MyEvent (int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode () {
            return code;
        }

        public void setCode (int code) {
            this.code = code;
        }

        public void setMsg (String msg) {
            this.msg = msg;
        }

        public String getMsg () {
            return msg;
        }

        @Override
        public String toString () {
            return "MyEvent{" +
                                "msg='" + msg + '\'' +
                                ", code=" + code +
                                '}';
        }
    }
}
