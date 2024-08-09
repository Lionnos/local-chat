package com.onner.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Vector;

public class ReceiveFileDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private Map<String, byte[]> receivedFiles;
    private Map<String, String> receivedFileNames;
    private String currentUserName;
    private String userName;
    private JList<String> fileList;
    private JProgressBar progressBar;

    // Constructor del diálogo de recepción de archivos
    public ReceiveFileDialog(JFrame parent, Map<String, byte[]> receivedFiles, Map<String, String> receivedFileNames, String currentUserName, String userName, PrintWriter out) {
        super(parent, "Archivos Recibidos de " + userName, true);
        this.receivedFiles = receivedFiles;
        this.receivedFileNames = receivedFileNames;
        this.currentUserName = currentUserName;
        this.userName = userName;
        setSize(400, 300);
        getContentPane().setLayout(new BorderLayout());

        // Contenedor principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.decode("#D5D9DF"));
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // Panel superior para el botón "Actualizar Lista" y la barra de progreso
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.decode("#D5D9DF"));
        JButton btnUpdateList = new JButton("Actualizar Lista");
        topPanel.add(btnUpdateList, BorderLayout.EAST);

        // Barra de progreso
        progressBar = new JProgressBar();
        progressBar.setForeground(Color.ORANGE);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        topPanel.add(progressBar, BorderLayout.WEST);

        // Label "Archivos disponibles"
        JLabel lblFiles = new JLabel("Archivos disponibles");
        lblFiles.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblFiles.setPreferredSize(new Dimension(lblFiles.getPreferredSize().width, 23));
        topPanel.add(lblFiles, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Lista para mostrar los archivos recibidos
        fileList = new JList<>();
        fileList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        mainPanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        // Panel inferior para los botones "Guardar Archivo" y "Salir"
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.decode("#D5D9DF"));
        JButton btnSave = new JButton("Guardar Archivo");
        JButton btnExit = new JButton("Salir");
        bottomPanel.add(btnExit);

        // Acción del botón "Salir"
        btnExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottomPanel.add(btnSave);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Acción del botón "Actualizar Lista"
        btnUpdateList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateFileList();
            }
        });

        // Acción del botón "Guardar Archivo"
        btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });

        setLocationRelativeTo(parent);
    }

    // Actualiza la lista de archivos disponibles en el diálogo
    private void updateFileList() {
        Vector<String> files = new Vector<>();
        for (Map.Entry<String, String> entry : receivedFileNames.entrySet()) {
            String key = entry.getKey();
            String fileName = entry.getValue();
            if (key.startsWith(userName + "@")) {
                files.add(fileName);
            }
        }
        fileList.setListData(files);

        // Imprimir contenido Base64 del primer archivo de la lista como ejemplo
        if (!files.isEmpty()) {
            String selectedFileName = files.get(0);
            String key = userName + "@" + currentUserName + ":" + selectedFileName;
            byte[] fileContent = receivedFiles.get(key);
            if (fileContent != null && fileContent.length > 0) {
                System.out.println("RECEIVE: Contenido del archivo listo! ");
            } else {
                System.out.println("RECEIVE: No se recibio el archivo: " + key);
            }
        }
    }

    // Guarda el archivo seleccionado por el usuario
    private void saveFile() {
        String selectedFile = fileList.getSelectedValue();
        if (selectedFile != null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(selectedFile));
            int returnValue = fileChooser.showSaveDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File saveFile = fileChooser.getSelectedFile();
                String key = userName + "@" + currentUserName + ":" + selectedFile;
                byte[] fileContent = receivedFiles.get(key);
                if (fileContent != null && fileContent.length > 0) {
                    progressBar.setVisible(true);
                    progressBar.setValue(0);
                    progressBar.setStringPainted(true);
                    progressBar.setForeground(Color.decode("#EB891B"));
                    progressBar.setBorder(BorderFactory.createEmptyBorder());

                    new Thread(() -> {
                        try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                            int totalBytes = fileContent.length;
                            int bufferSize = 1024;
                            int bytesWritten = 0;
                            for (int i = 0; i < totalBytes; i += bufferSize) {
                                int bytesToWrite = Math.min(bufferSize, totalBytes - i);
                                fos.write(fileContent, i, bytesToWrite);
                                bytesWritten += bytesToWrite;

                                final int progress = (int) ((double) bytesWritten / totalBytes * 100);
                                SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                            }

                            SwingUtilities.invokeLater(() -> {
                                progressBar.setValue(100);
                                JOptionPane.showMessageDialog(ReceiveFileDialog.this, "Transferencia finalizada correctamente.");
                                progressBar.setVisible(false);

                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                } else {
                    System.out.println("RECEIVE: El contenido del archivo es nulo. Key: " + key);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo de la lista para guardar.");
        }
    }
}
