import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Producer {

    // either connect to the remote ActiveMQ running on the PI, or on the localhost
    private static String url = "tcp://localhost:61616"; // "failover:(tcp://localhost:61616,mijnpi.local:61616)";
    private static String subject = "testQueue1"; // Queue Name

    public static void main(String[] args) throws JMSException {
        System.out.println("Starting connection to " + url);
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        System.out.println("Creating queue " + subject);
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(subject);
        System.out.println("Creating producer");
        MessageProducer producer = session.createProducer(destination);
        TextMessage message = session.createTextMessage("Hello welcome come to testQueue1 ActiveMQ!");
        producer.send(message);
        System.out.println("Sent Message '" + message.getText() + "'");
        connection.close();
    }

}

