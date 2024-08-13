package com.onner.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import javax.swing.border.*;

import com.onner.client.components.JButtonRounded;
import com.onner.client.components.SoundPlayer;
import com.onner.client.components.SoundProcess;
import com.onner.client.components.TabbePane;

public class RunClient extends JFrame {
    int PORT = 9003;
    String IP = "127.0.0.1";

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private TabbePane tabbedPane;
    private PrintWriter out;
    private Map<String, JPanel> userTextAreas = new HashMap<>();
    private Map<String, JTextField> textFields = new HashMap<>();
    private JPanel buttonPanel;
    private String currentUserName;
    private Socket socket;
    private Map<String, byte[]> receivedFiles = new HashMap<>();
    private Map<String, String> receivedFileNames = new HashMap<>();
    private Map<String, StringBuilder> messageHistory = new HashMap<>();
    private Map<String, JButtonRounded> contactButtons = new HashMap<>();
    private JPanel panelChat;
    private static SoundProcess soundProcess;
    private static Thread soundThread;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    RunClient frame = new RunClient();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        SoundPlayer soundPlayer = new SoundPlayer();
        soundProcess = new SoundProcess(soundPlayer);
        soundThread = new Thread(soundProcess);
        soundThread.start();
    }

    public RunClient() {
        currentUserName = JOptionPane.showInputDialog(this, "Ingresa tu nombre de usuario:");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 652, 424);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu mnNewMenu = new JMenu("Chat");
        menuBar.add(mnNewMenu);
        JMenuItem mntmNewMenuItem = new JMenuItem("Salir");
        mnNewMenu.add(mntmNewMenuItem);

        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.setBackground(Color.decode("#D5D9DF"));
        setContentPane(contentPane);

        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBackground(Color.decode("#365c7e"));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(8, 0, 0, 0, Color.decode("#B8BCC4")),
                BorderFactory.createMatteBorder(0, 0, 18, 0, Color.decode("#365c7e"))
        ));
        contentPane.add(leftPanel, BorderLayout.WEST);
        leftPanel.setLayout(new BorderLayout(0, 0));

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BorderLayout(0, 0));
        containerPanel.setBackground(Color.decode("#B8BCC4"));
        containerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        leftPanel.add(containerPanel, BorderLayout.CENTER);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        containerPanel.add(buttonPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setBorder(BorderFactory.createMatteBorder(18, 7, 18, 7, Color.decode("#bef8e4")));
        rightPanel.setLayout(new BorderLayout(0, 0));
        contentPane.add(rightPanel, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(Color.decode("#365c7e"));
        topPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        rightPanel.add(topPanel, BorderLayout.NORTH);

        JLabel lblUserNameTop = new JLabel(currentUserName, JLabel.CENTER);
        lblUserNameTop.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblUserNameTop.setForeground(Color.BLACK);
        topPanel.add(lblUserNameTop, BorderLayout.CENTER);

        tabbedPane = new TabbePane();
        tabbedPane.getTabbedPane().setTabPlacement(JTabbedPane.TOP);
        rightPanel.add(tabbedPane.getTabbedPane(), BorderLayout.CENTER);

        connectToServer(currentUserName);
    }

    private class MessagePanel extends JPanel {
        public MessagePanel(String message, boolean isOwnMessage) {
            setLayout(new BorderLayout());
            JLabel messageLabel = new JLabel("<html><p style='width: 150px;'>" + message + "</p></html>");
            messageLabel.setOpaque(true);
            messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            if (isOwnMessage) {
                setLayout(new FlowLayout(FlowLayout.RIGHT));
                messageLabel.setBackground(new Color(220, 248, 198)); // Light green for own messages
            } else {
                setLayout(new FlowLayout(FlowLayout.LEFT));
                messageLabel.setBackground(Color.WHITE); // White for other messages
            }

            add(messageLabel);
        }
    }

    private void createChatTab(String userName) {
        panelChat = new JPanel();
        panelChat.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.decode("#e4f9f8")));
        tabbedPane.getTabbedPane().addTab(userName, null, panelChat, null);
        panelChat.setLayout(new BorderLayout(0, 0));

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(messagePanel);
        panelChat.add(scrollPane, BorderLayout.CENTER);

        if (messageHistory.containsKey(userName)) {
            String[] messages = messageHistory.get(userName).toString().split("\n");
            for (String message : messages) {
                boolean isOwnMessage = message.startsWith("Yo: ");
                MessagePanel msgPanel = new MessagePanel(message, isOwnMessage);
                messagePanel.add(msgPanel);
            }
        }

        userTextAreas.put(userName, messagePanel);

        btnInputOuputFile(userName);
        sendMessage(userName);

        JPanel tabHeader = new JPanel(new BorderLayout());
        tabHeader.setOpaque(false);
        JLabel tabTitle = new JLabel(userName);
        JButton closeButton = new JButton("✖");
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = tabbedPane.getTabbedPane().indexOfTabComponent(tabHeader);
                if (index != -1) {
                    tabbedPane.getTabbedPane().remove(index);
                    userTextAreas.remove(userName);
                    textFields.remove(userName);
                }
            }
        });
        tabHeader.add(tabTitle, BorderLayout.CENTER);
        tabHeader.add(closeButton, BorderLayout.EAST);

        tabbedPane.getTabbedPane().setTabComponentAt(tabbedPane.getTabbedPane().indexOfComponent(panelChat), tabHeader);
    }

    public void btnInputOuputFile(String userName){
        JPanel panelButtons = new JPanel();
        panelButtons.setBackground(Color.decode("#D5D9DF"));
        panelButtons.setLayout(new BorderLayout());
        panelButtons.setBorder(BorderFactory.createMatteBorder(0, 0, 10, 0, Color.decode("#D5D9DF")));
        panelChat.add(panelButtons, BorderLayout.NORTH);

        JPanel buttonContainer = new JPanel();
        buttonContainer.setBackground(Color.decode("#D5D9DF"));
        buttonContainer.setLayout(new BorderLayout(20, 0));
        panelButtons.add(buttonContainer, BorderLayout.EAST);

        JButton btnReceiveFile = new JButton("Recibir Archivo");
        btnReceiveFile.setPreferredSize(new Dimension(140, 30));
        btnReceiveFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ReceiveFileDialog receiveFileDialog = new ReceiveFileDialog(RunClient.this, receivedFiles, receivedFileNames, currentUserName, userName, out);
                receiveFileDialog.setVisible(true);
            }
        });
        buttonContainer.add(btnReceiveFile, BorderLayout.WEST);

        // Botón para compartir archivos
        JButton btnShareFile = new JButton("Compartir Archivo");
        btnShareFile.setPreferredSize(new Dimension(140, 30));
        btnShareFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ShareFileDialog shareFileDialog = new ShareFileDialog(RunClient.this, out, currentUserName, userName);
                shareFileDialog.setVisible(true);
            }
        });
        buttonContainer.add(btnShareFile, BorderLayout.EAST);
    }

    public void sendMessage(String userName){
        JPanel panelSend = new JPanel();
        panelChat.add(panelSend, BorderLayout.SOUTH);
        panelSend.setLayout(new BorderLayout(0, 0));

        JTextField textField = new JTextField();
        textField.setFont(new Font("Monospaced", Font.PLAIN, 16));
        panelSend.add(textField, BorderLayout.CENTER);
        textField.setColumns(10);
        textField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String message = textField.getText();
                    if (!message.isEmpty()) {
                        out.println(currentUserName + "@" + userName + ": " + message);
                        appendMessage(userName, "Yo: " + message);
                        textField.setText("");
                        //Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        });

        textFields.put(userName, textField);
    }

    private void appendMessage(String userName, String message) {
        if (!messageHistory.containsKey(userName)) {
            messageHistory.put(userName, new StringBuilder());
        }
        messageHistory.get(userName).append(message).append("\n");

        if (userTextAreas.containsKey(userName)) {
            JPanel messagePanel = userTextAreas.get(userName);
            //boolean isOwnMessage = message.startsWith("Yo:");
            boolean isOwnMessage = message.startsWith(userName);
            MessagePanel msgPanel = new MessagePanel(message, isOwnMessage);
            messagePanel.add(msgPanel);
            messagePanel.revalidate();
            messagePanel.repaint();

            JScrollPane scrollPane = (JScrollPane) messagePanel.getParent().getParent();
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        }

        int selectedIndex = tabbedPane.getTabbedPane().getSelectedIndex();
        String selectedTabTitle = selectedIndex != -1 ? tabbedPane.getTabbedPane().getTitleAt(selectedIndex) : "";
        if (!selectedTabTitle.equals(userName) && contactButtons.containsKey(userName)) {
            JButtonRounded button = contactButtons.get(userName);
            button.incrementCounter(soundProcess);
            //button.setButtonBackgroundColor("#FF5733"); // Optionally change the button color if needed
        }
    }

    private JButtonRounded createContactButton(String name, int tabIndex) {
        JButtonRounded button = new JButtonRounded(name); // Cambiado a JButtonRounded
        button.setFont(new Font("Tahoma", Font.PLAIN, 16));
        button.setBackground(Color.decode("#27ae60"));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = getTabIndexByName(name);
                if (index == -1) {
                    createChatTab(name);
                    index = getTabIndexByName(name);
                }
                tabbedPane.getTabbedPane().setSelectedIndex(index);
                button.setBackground(Color.decode("#27ae60"));

                button.resetCounter();
            }
        });
        return button;
    }

    private int getTabIndexByName(String name) {
        for (int i = 0; i < tabbedPane.getTabbedPane().getTabCount(); i++) {
            if (tabbedPane.getTabbedPane().getTitleAt(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private void connectToServer(String userName) {
        try {
            socket = new Socket(IP, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(userName);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            if (message.startsWith("USER_LIST")) {
                                updateUsersList(message);
                            } else if (message.startsWith("FILE_AVAILABLE:")) {
                                handleFileAvailable(message);
                            } else if (message.startsWith("FILE:")) {
                                handleFileReception(message);
                            } else {
                                String[] parts = message.split(":", 2);
                                if (parts.length == 2) {
                                    String[] userParts = parts[0].split("@");
                                    if (userParts.length == 2) {
                                        String sender = userParts[0].trim();
                                        String receiver = userParts[1].trim();
                                        String text = parts[1].trim();
                                        if (receiver.equals(currentUserName)) {
                                            appendMessage(sender, sender + ": " + text);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUsersList(String userListMessage) {
        String[] users = userListMessage.split(" ");
        for (int i = 1; i < users.length; i++) {
            String userName = users[i];
            if (!userName.equals(currentUserName) && !contactButtons.containsKey(userName)) {
                JButtonRounded newButton = createContactButton(userName, tabbedPane.getTabbedPane().getTabCount());
                contactButtons.put(userName, newButton);
                buttonPanel.add(newButton);
                createChatTab(userName);
            }
        }
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    private void handleFileAvailable(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String key = parts[1];
            String[] keyParts = key.split(":");
            if (keyParts.length == 3) {
                String fileName = keyParts[2];
                receivedFileNames.put(key, fileName);
                System.out.println("CLIENT: Archivo disponible: " + fileName + ", Key: " + key);
            }
        }
    }

    private void handleFileReception(String message) {
        String[] parts = message.split(":", 4);
        if (parts.length == 4) {
            String senderReceiver = parts[1];
            String fileName = parts[2];
            byte[] fileContent = Base64.getDecoder().decode(parts[3]);
            if (fileContent == null || fileContent.length == 0) {
                System.out.println("CLIENT: El contenido del archivo recibido es nulo o vacío. Key: " + senderReceiver + ":" + fileName);
            } else {
                try {
                    String key = senderReceiver + ":" + fileName;
                    receivedFiles.put(key, fileContent);
                    receivedFileNames.put(key, fileName);
                    File tempFile = new File("temp_" + fileName);
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(fileContent);
                    }
                    System.out.println("CLIENT: Archivo recibido y guardado temporalmente: " + fileName + ", Key: " + key + ", Tamaño: " + fileContent.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

