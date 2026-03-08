package sd_chat;

import javax.jms.JMSException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;


public class ChatApp {
    public JTextArea chatArea;
    public JTextField inputField;
    private Chat chat;
    private JPanel userListPanel;

    public ChatApp(Chat chat) throws JMSException,  Exception {
        this.chat = chat;
        chat.setChatApp(this);

        //Criando o frame da minha aplicaçao
        JFrame frame = new JFrame("Chat Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 700);
        frame.setLayout(new BorderLayout());

         // Criação do painel para exibir o nome do usuário
        JPanel userPanel = new JPanel();
        JLabel userLabel = new JLabel("Usuário: " + chat.getUsername()); // Supondo que você tenha um campo username na classe Chat
        userLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userPanel.add(userLabel);
        frame.add(userPanel, BorderLayout.NORTH); // Adiciona o painel ao topo do frame


        // Área de texto para mensagens
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setBackground(new Color(240, 240, 240));
        chatArea.setFont(new Font("Arial", Font.PLAIN, 18));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);


        // Campo de entrada e botão de enviar
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        JButton sendButton = new JButton("Enviar");

        
        //Campo onde tratamos o envio de mensagens
        sendButton.addActionListener((ActionEvent e) -> {
            try {
                analise(inputField.getText());
                inputField.setText(""); // Limpa o campo de entrada após o envio
            } catch (JMSException ex) {
                System.out.println("Erro ao enviar mensagem: " + ex.getMessage());
                ex.printStackTrace();
            } catch (Exception ex)  {
                System.out.println("Erro ao enviar mensagem: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        
        //conifgurando o ENTER
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendButton.doClick(); // Simula um clique no botão
            }
        });
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);


        // Painel à direita para lista de usuários
        userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userListPanel.setBorder(BorderFactory.createTitledBorder("Usuários Online"));
        frame.add(userListPanel, BorderLayout.EAST);

        JScrollPane userListScrollPane = new JScrollPane(userListPanel);
        userListScrollPane.setPreferredSize(new Dimension(200, 0)); // Define a largura preferida para o painel de usuários
        frame.add(userListScrollPane, BorderLayout.EAST);


        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        // frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Evita o fechamento automático
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fecha a aplicação ao fechar a janela

        frame.addWindowListener(new java.awt.event.WindowAdapter() { 
            @Override
            public void windowClosing(WindowEvent e) {
                int response = JOptionPane.showConfirmDialog(frame, "Tem certeza de que deseja sair?", "Confirmar Saída",
                        JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    try{
                        analise("exit");
                        chat.setExit(true);
                        chat.sendPresenceStatus(false);
                        System.exit(0);
                    }catch(JMSException f){
                        System.out.println("Erro ao configurar conexão JMS: " + f.getMessage());
                        f.printStackTrace();
                    }catch(Exception f){
                        System.out.println("Erro ao configurar conexão JMS: " + f.getMessage());
                        f.printStackTrace();
                    }

                    frame.dispose();
                }
            }
        });
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Chat getChat() {
        return this.chat;
    }

    // Método para atualizar a lista de usuários no painel direito
    public void updateUserList() {
        userListPanel.removeAll(); // Limpa a lista atual
        for (String username : chat.getUsuariosOnline().keySet()) {
            JLabel userLabel = new JLabel(username);
            userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            userListPanel.add(userLabel);
        }
        userListPanel.revalidate(); // Atualiza o layout
        userListPanel.repaint(); // Re-renderiza o painel
    }

    public void analise(String messageText) throws JMSException,  Exception{
        // String messageText = scanner.nextLine();
            if (messageText.equalsIgnoreCase("exit")) {
                System.out.println(chat.getUsername() + " saiu do chat.");
                chat.sendMessage(chat.getUsername() + " saiu do chat!");
            }
            else if (messageText.startsWith("@")) {
                String[] parts = messageText.split(" ", 2);
                if (parts.length == 2) {
                    String recipient = parts[0].substring(1); // Extrai o destinatário após "@"
                    chat.sendPrivateMessage(recipient, parts[1]);
                } else {
                    System.out.println("Formato de mensagem privada inválido. Use: @destinatario mensagem");
                }
            }else{
                // chat.sendMessage(messageText);
                chat.sendMessage(chat.getUsername() + ": " + messageText);
            }
    }

    public void onMessage(String message) {
        // String message = inputField.getText();
        if (!message.trim().isEmpty()) {
            chatArea.append(message + "\n");
            inputField.setText("");
        }
    }

    public static void main(String[] args, Chat chat) {
        // Leitura do nome do usuário
        String username = JOptionPane.showInputDialog("Digite seu nome de usuário:");
        chat.setUsername(username);
        if (username != null) {
            SwingUtilities.invokeLater(() -> {
                try{
                    new ChatApp(chat);
                // chatApp.setVisible(true);
                }catch(JMSException e){
                    System.out.println("Erro ao configurar conexão JMS: " + e.getMessage());
                    e.printStackTrace();
                }catch(Exception e){
                    System.out.println("Erro ao configurar conexão JMS: " + e.getMessage());
                    e.printStackTrace();
                }

            });
        }
    }
}
