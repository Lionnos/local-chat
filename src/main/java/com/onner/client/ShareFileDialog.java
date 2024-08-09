package com.onner.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;

public class ShareFileDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private JTextArea textArea;

    public ShareFileDialog(JFrame parent, PrintWriter out, String currentUserName, String userName) {
        super(parent, "Compartir Archivo con " + userName, true);
        setSize(400, 300);
        setLayout(new BorderLayout());

        // Contenedor principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.decode("#D5D9DF"));
        add(mainPanel, BorderLayout.CENTER);

        // Panel superior para el botón "Compartir Archivo"
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.decode("#D5D9DF"));
        JButton btnSelectFile = new JButton("Compartir Archivo");
        topPanel.add(btnSelectFile, BorderLayout.WEST);

        // Label "Compartir Archivo"
        JLabel lblFiles = new JLabel("Archivos compartidos");
        lblFiles.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblFiles.setPreferredSize(new Dimension(lblFiles.getPreferredSize().width, 23));
        topPanel.add(lblFiles, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Área de texto para mostrar los archivos compartidos
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        mainPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Panel inferior para el botón "Salir y Cancelar Compartición"
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.decode("#D5D9DF"));
        JButton btnExit = new JButton("Salir y Cancelar Compartición");
        bottomPanel.add(btnExit, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Acción del botón "Compartir Archivo"
        btnSelectFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Abre un JFileChooser para seleccionar un archivo
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos", "*"));
                int returnValue = fileChooser.showOpenDialog(ShareFileDialog.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    sendFile(selectedFile, currentUserName, userName, out);
                }
            }
        });

        // Acción del botón "Salir y Cancelar Compartición"
        btnExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setLocationRelativeTo(parent);
    }

    // Envia el archivo seleccionado al servidor
    private void sendFile(File selectedFile, String currentUserName, String userName, PrintWriter out) {
        try {
            byte[] fileBytes = readFileToByteArray(selectedFile);
            if (fileBytes == null || fileBytes.length == 0) {
                System.out.println("El contenido del archivo que se intenta enviar es nulo o vacío.");
            } else {
                // Codifica el archivo en base64 y envía el mensaje al servidor
                String encodedFile = Base64.getEncoder().encodeToString(fileBytes);
                out.println("FILE:" + currentUserName + "@" + userName + ":" + selectedFile.getName() + ":" + encodedFile);
                textArea.append(selectedFile.getName() + "\n");
                System.out.println("SHARE: Archivo enviado: " + selectedFile.getName() + ", Tamaño: " + fileBytes.length);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Lee un archivo y lo convierte en un array de bytes
    private byte[] readFileToByteArray(File file) throws IOException {
        FileInputStream fis = null;
        byte[] byteArray = new byte[(int) file.length()];
        try {
            fis = new FileInputStream(file);
            fis.read(byteArray);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return byteArray;
    }
}
