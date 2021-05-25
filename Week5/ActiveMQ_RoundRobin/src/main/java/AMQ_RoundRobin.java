import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class AMQ_RoundRobin {

    // either connect to the remote ActiveMQ running on the PI, or on the localhost
    private static String URL = "tcp://localhost:61616"; // "failover:(tcp://localhost:61616,mijnpi.local:61616)";
    private static String QUEUE_NAME = "testQueue"; // Queue Name
    private static String SERVICE_HOST = "localhost";

    public static void main(String[] args) throws JMSException, IOException, InterruptedException {

        int clientId = 0;
        int numClients = 4;

        // process command line arguments, decide to assume master role or worker role
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--verbosityLevel")) {
                i++;
                Timer.verbosityLevel = Integer.valueOf(args[i]);
            } else if (args[i].equals("--numClients")) {
                i++;
                numClients = Integer.valueOf(args[i]);
            } else if (args[i].equals("--clientId")) {
                i++;
                clientId = Integer.valueOf(args[i]);

                // launch the client
                clientMain(clientId, numClients);
                return;
            }
        }

        // if no client launched, continue as the client-0
        Scanner input = new Scanner(System.in);

        System.out.printf("Welcome Round-robin JMS test on %s\n", URL);
        System.out.printf("\nPlease provide your input and output verbosity level [-1,0,1,2]: ");
        Timer.verbosityLevel = input.nextInt();
        if (Timer.verbosityLevel >= 0) {
            System.out.print("Please provide the number of client processes: ");
            numClients = input.nextInt();
        }

        // launch separate worker processes
        Timer.echo(-1, "\nLaunching %d clients at %s\n",
                numClients, SERVICE_HOST);
        Process[] workers = launchWorkersAtLocalHost(numClients);

        clientMain(0, numClients);

        // wait until all workers have finished
        shutdownWorkers(workers);
    }

    private static void clientMain(int clientId, int numClients) throws JMSException {
        Connection connection = startConnection(clientId, URL);
        //Session session = startSession(clientId, URL);
        Session session = createSession(connection);
        MessageProducer producer = createProducerQueue(clientId, session, QUEUE_NAME + (clientId+1) % numClients);
        MessageConsumer consumer = createConsumerQueue(clientId, session, QUEUE_NAME + clientId);

        Timer.echo(2, "Client-%d has launched and connected to its queues\n", clientId);
        if (clientId == 0) {
            TextMessage message = session.createTextMessage(String.format("client-%d says hello.", clientId));
            producer.send(message);
            TextMessage result = (TextMessage)consumer.receive();
            Timer.echo(-1, "Client-%d has recieved: %s\n", clientId, result.getText());
        } else {
            TextMessage message = (TextMessage)consumer.receive();
            Timer.echo(-1, "Client-%d has recieved: %s\n", clientId, message.getText());
            TextMessage newMsg = session.createTextMessage(String.format("client-%d says '%s'", clientId, message.getText()));
            producer.send(newMsg);
        }

        //session.close();
        connection.close();
    }

    private static Connection startConnection(int clientId, String url) throws JMSException {
        Timer.echo(2, "Client-%d is starting connection to %s\n", clientId, url);
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }

    private static Session createSession(Connection connection) throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return session;
    }
    private static Session startSession(int clientId, String url) throws JMSException {
        Connection connection = startConnection(clientId, url);
        return createSession(connection);
    }

    private static MessageProducer createProducerQueue(int clientId, Session session, String queueName) throws JMSException {
        Destination destination = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(destination);
        Timer.echo(1, "Client-%d has created producer queue to %s\n", clientId, queueName);
        return producer;
    }

    private static MessageConsumer createConsumerQueue(int clientId, Session session, String queueName) throws JMSException {
        Destination destination = session.createQueue(queueName);
        MessageConsumer consumer = session.createConsumer(destination);
        Timer.echo(1, "Client-%d has created consumer queue from %s\n", clientId, queueName);
        return consumer;
    }

    private static Process[] launchWorkersAtLocalHost(int numClients) throws IOException {
        Timer.start();
        Process[] workers = new Process[numClients];
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = System.getProperty("java.class.path");
        // launch the worker processes
        for (int childId = 1; childId < numClients; childId++) {

            // restart the current main with child worker command line arguments
            ProcessBuilder child = new ProcessBuilder(
                    javaBin, "-classpath", classPath, AMQ_RoundRobin.class.getCanonicalName(),
                    "--verbosityLevel", String.valueOf(Timer.verbosityLevel),
                    "--numClients", String.valueOf(numClients),
                    "--clientId", String.valueOf(childId)
            );

            workers[childId] = child.inheritIO().start();
        }
        Timer.measure(1,"%d client processes have been launched\n", numClients);
        return workers;
    }

    private static void shutdownWorkers(Process[] workers) throws InterruptedException {
        Timer.echo(1, "Waiting for %d clients to complete\n", workers.length);
        for (int childId = 1; childId < workers.length; childId++) {
            workers[childId].waitFor();
        }
        Timer.measure(-1, "All client processes have finished\n");
    }
}

