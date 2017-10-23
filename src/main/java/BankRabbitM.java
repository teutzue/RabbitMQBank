import java.io.IOException;
import java.util.concurrent.TimeoutException;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

public class BankRabbitM {

    private static final String BANK_QUEUE_1 = "bank1";

    public static void main(String[] argv) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = null;
        try {
            connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            channel.queueDeclare(BANK_QUEUE_1, false, false, false, null);

            channel.basicQos(1);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();

                    String response = "";
                    Request req;
                    Response res;

                    try {
                        String message = new String(body, "UTF-8");
                        System.out.println(message);
                        Gson gson = new Gson();
                        req = gson.fromJson(message, Request.class);
                        res = new Response(req.getSsn(), getInterestRate(req));
                        ObjectMapper mapper = new ObjectMapper();
                        response = mapper.writeValueAsString(res);
                        System.out.println(" [x] " + response);
                    } catch (RuntimeException e) {
                        System.out.println(" [.] " + e.toString());
                    } finally {
                        channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                        channel.basicAck(envelope.getDeliveryTag(), false);
                        // RabbitMq consumer worker thread notifies the RPC server owner thread
                        synchronized (this) {
                            this.notify();
                        }
                    }
                }
            };

            channel.basicConsume(BANK_QUEUE_1, false, consumer);
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (consumer) {
                    try {
                        consumer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                try {
                    connection.close();
                } catch (IOException _ignore) {
                }
        }
    }

    private static float getInterestRate(Request request) {
        float interest = 0;
        if (request.getCreditScore() > 500) {
            if (request.getDuration() > 120) {
                if (request.getLoan() > 15) {
                    interest = 4.6f;
                } else {
                    interest = 6.8f;
                }
            } else {
                if (request.getLoan() > 15) {
                    interest = 3.6f;
                } else {
                    interest = 5.8f;
                }
            }
        } else {
            if (request.getDuration() > 120) {
                if (request.getLoan() > 15) {
                    interest = 5.6f;
                } else {
                    interest = 7.8f;
                }
            } else {
                if (request.getLoan() > 15) {
                    interest = 4.6f;
                } else {
                    interest = 6.8f;
                }
            }
        }

        return interest;
    }
}

