/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatapplication;

/**
 *
 * @author TranLong
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;

public class ServerGUI extends JFrame {
    private JTextField messageField;
    private JTextArea chatArea;
    private PrintWriter out;
    private Socket clientSocket;

    public ServerGUI() {
        setTitle("Chat Application - Server");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 2));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setPreferredSize(new Dimension(500, 200));
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(chatPanel, BorderLayout.WEST);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        inputPanel.add(messageField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JButton sendButton = new JButton("Send Message");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        buttonPanel.add(sendButton);

        JButton sendImageButton = new JButton("Send Image");
        sendImageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendImage();
            }
        });
        buttonPanel.add(sendImageButton);

        JButton sendFileButton = new JButton("Send File");
        sendFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendFile();
            }
        });
        buttonPanel.add(sendFileButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(inputPanel, BorderLayout.CENTER);

        add(panel);
        setVisible(true);

        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            chatArea.append("Server started. Waiting for clients...\n");

            clientSocket = serverSocket.accept();
            chatArea.append("Client connected: " + clientSocket + "\n");
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            Thread readThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String message;
                        while ((message = in.readLine()) != null) {
                            if (message.startsWith("IMAGE:")) {
                                receiveImage();
                            } else if (message.startsWith("FILE:")) {
                                receiveFile();
                            } else {
                                chatArea.append("Client: " + message + "\n");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = messageField.getText();
        chatArea.append("Server: " + message + "\n");
        out.println(message);
        messageField.setText("");
    }

    private void receiveImage() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            File selectedFile = (File) objectInputStream.readObject();
            chatArea.append("Client sent an image: " + selectedFile.getName() + "\n");
            previewImage(selectedFile);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            File selectedFile = (File) objectInputStream.readObject();
            chatArea.append("Client sent a file: " + selectedFile.getName() + "\n");
            downloadFile(selectedFile);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void previewImage(File imageFile) {
        ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
        JLabel label = new JLabel();
        label.setIcon(icon);
        JOptionPane.showMessageDialog(this, label, "Preview Image", JOptionPane.PLAIN_MESSAGE);
    }

    private void downloadFile(File file) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose directory to save file");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File directory = fileChooser.getSelectedFile();
            String filePath = directory.getAbsolutePath() + File.separator + file.getName();
            try {
                Files.copy(file.toPath(), new File(filePath).toPath());
                JOptionPane.showMessageDialog(this, "File downloaded successfully!", "Download Status", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error downloading file!", "Download Status", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void sendImage() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            chatArea.append("You sent an image: " + selectedFile.getName() + "\n");

            try {
                OutputStream outputStream = clientSocket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(selectedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            chatArea.append("You sent a file: " + selectedFile.getName() + "\n");

            try {
                OutputStream outputStream = clientSocket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(selectedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ServerGUI();
            }
        });
    }
}


