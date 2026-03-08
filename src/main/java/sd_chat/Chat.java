package sd_chat;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Properties;
import java.util.HashMap;


public class Chat implements MessageListener {
    private static String chatTopic = "chatTopic";             // Nome do tópico do chat
    private static String _topicCheckOn = "topicCheckOn";             // Nome do tópico do chat
    private Session session;
    private MessageProducer producer;

    private String username;
    private ChatApp chatApp;

    private HashMap<String, String> _usuariosOnline = new HashMap<>();
    private Boolean exit = false;

    public Chat(String[] args) throws JMSException,  Exception {
        ChatApp.main(args, this);

       // Carregar o contexto JNDI a partir do arquivo jndi.properties
        Properties properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream("jndi.properties"));
        
        // Criação do contexto inicial JNDI
        Context ctx = new InitialContext(properties);

        // Buscar a ConnectionFactory e o Tópico pelo JNDI
        ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup("ConnectionFactory");

        // Criação da conexão e sessão JMS
        Connection connection = connectionFactory.createConnection();
        connection.setClientID(username); // Define um ID exclusivo para cada cliente
        connection.start();

        // Cria sessão sem transação e com confirmação automática
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Cria ou acessa o tópico do chat
        Topic topic = session.createTopic(chatTopic);

        // Configura consumidor para receber mensagens do tópico de forma duravel
        MessageConsumer consumer = session.createDurableSubscriber(topic, username);
        consumer.setMessageListener(this); // Define o listener para receber mensagens

        // Cria o produtor para enviar mensagens ao tópico
        producer = session.createProducer(topic);

        //FILA PARA PV PRIVADO
        // Configuração da fila privada do usuário
        Queue privateQueue = session.createQueue("privateQueue_" + username);
        MessageConsumer privateConsumer = session.createConsumer(privateQueue);
        privateConsumer.setMessageListener(this); // Mesma função de listener para mensagens privadas

        //CONSUMIDOR PARA SABER QUEM ESTA ONLINE
        Topic topicCheckOn = session.createTopic(_topicCheckOn);
        MessageConsumer consumerOn = session.createConsumer(topicCheckOn);
        consumerOn.setMessageListener(this);
        sendPresenceStatus(true);

        sendHelloWorld();
        System.out.println(username + " entrou no chat...");
    }

    // Envia uma mensagem para o tópico
    public void sendHelloWorld() throws JMSException {
        // Cria a mensagem de presença
        TextMessage presenceMessage = session.createTextMessage(username + " entrou no chat");
        presenceMessage.setStringProperty("status", "input");
        producer.send(presenceMessage);
    }

    // Envia uma mensagem para o tópico
    public void sendMessage(String messageText) throws JMSException {
        TextMessage message = session.createTextMessage(messageText);
        //atraves do produtor de topic estamos mandando a mensagem
        producer.send(message);
    }


    // Envia uma mensagem privada para um usuário específico
    public void sendPrivateMessage(String recipient, String messageText) throws JMSException {
        Queue recipientQueue = session.createQueue("privateQueue_" + recipient);
        TextMessage message = session.createTextMessage("[De] " + username + ": " + messageText + "\n");
        TextMessage messageOwner = session.createTextMessage("[Para] " + recipient + ": " + messageText + "\n");
        MessageProducer privateProducer = session.createProducer(recipientQueue);
        privateProducer.send(message);
        privateProducer.close(); // Fecha o produtor depois do envio

        //so pra printar na tela que vc enviou uma mensagem privada
        chatApp.chatArea.append(((TextMessage)messageOwner).getText());
        chatApp.inputField.setText("");
    }

    //Envia mensagem do atual status do usuario se esta online ou offline
    public void sendPresenceStatus(boolean isOnline) throws JMSException {
        // Configura o tópico de presença
        Topic topicCheckOn = session.createTopic(Chat._topicCheckOn);
        MessageProducer producer = session.createProducer(topicCheckOn);
    
        // Cria a mensagem de presença
        TextMessage presenceMessage = session.createTextMessage();
        presenceMessage.setText(username);

        if(isOnline)
           // Adiciona uma propriedade para diferenciar o tipo de mensagem
            presenceMessage.setStringProperty("status", "on");
        else
            presenceMessage.setStringProperty("status", "off");

        producer.send(presenceMessage);
    
        // Fecha o produtor de mensagens
        producer.close();
    }

    // Listener que recebe as mensagens
    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                // Obtém a propriedade de tipo de mensagem
                String messageType = message.getStringProperty("status");
                if(messageType != null){
                    if ("on".equals(messageType)) {
                        _usuariosOnline.put(((TextMessage)message).getText(), ((TextMessage)message).getText());
                        chatApp.updateUserList();
                        return;
                    } else if ("off".equals(messageType)) {
                        _usuariosOnline.put(((TextMessage)message).getText(), ((TextMessage)message).getText());
                        _usuariosOnline.remove(((TextMessage)message).getText());
                        chatApp.updateUserList();
                        return;
                    } else if ("input".equals(messageType)) {
                        TextMessage textMessage = (TextMessage) message;
                        chatApp.onMessage("$ " + textMessage.getText());
                        sendPresenceStatus(true);
                        return;
                    }
                }
                TextMessage textMessage = (TextMessage) message;
                System.out.println("$ " + textMessage.getText());
                chatApp.onMessage("$ " + textMessage.getText());

            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }

    public ChatApp getChatApp(){
        return this.chatApp;
    }

    public void setChatApp(ChatApp chatApp) {
        this.chatApp = chatApp;
    }

    public HashMap<String, String> getUsuariosOnline() {
        return this._usuariosOnline;
    }

    public void setUsuariosOnline(HashMap<String, String> _usuariosOnline) {
        this._usuariosOnline = _usuariosOnline;
    }

    public void setExit(Boolean exit) {
        this.exit = exit;
    }

    public Boolean getExit() {
        return this.exit;
    }


    public static void main(String[] args) throws JMSException {
        try{
            // Cria a instância do chat para o usuário
            Chat chat = new Chat(args);
                
        } catch (InterruptedException e) {
            e.printStackTrace(); // Trata a exceção caso a thread seja interrompida
        }catch (JMSException e) {
            System.out.println("Erro JMS: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erro geral: " + e.getMessage());
        }
    }
}
