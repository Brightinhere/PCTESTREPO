import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;

public class Service {

    private static final String URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "testQueue1";
    private static final boolean SEND = false;

    public static void main(String[] args) throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(QUEUE_NAME);
        if (SEND) {
            MessageProducer producer = session.createProducer(destination);
            TextMessage message = session.createTextMessage("Hello World!");
            producer.send(message);
        } else {
            MessageConsumer consumer = session.createConsumer(destination);
            TextMessage message = (TextMessage) consumer.receive();
            System.out.println(message.getText());
        }
        connection.close();
    }
}
