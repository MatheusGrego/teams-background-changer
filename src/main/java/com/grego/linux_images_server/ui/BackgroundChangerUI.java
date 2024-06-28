
package com.grego.linux_images_server.ui;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executors;

@SpringBootApplication
@ComponentScan(basePackages = "com.grego.linux_images_server")
public class BackgroundChangerUI extends JFrame {
    private final GradientPanel mainPanel;
    private final JButton startAppButton;
    private final JButton selectImageButton;
    private final JLabel titleLabel;
    private final JLabel serverStatusLabel;
    private final JTextArea logTextArea;
    private ConfigurableApplicationContext springContext;
    private Process teamsProcess;

    private static final String UPLOAD_DIRECTORY = "uploads";

    public BackgroundChangerUI() {
        File uploadDir = new File(UPLOAD_DIRECTORY);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        setTitle("Teams Background Changer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createLineBorder(new Color(75, 0, 130), 5));
        setContentPane(contentPanel);

        mainPanel = new GradientPanel();
        mainPanel.setLayout(null);

        JPanel headerPanel = new GradientHeaderPanel();
        headerPanel.setBounds(0, 0, 600, 50);
        headerPanel.setLayout(null);

        titleLabel = new JLabel("Teams Background Changer");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Josefin Sans", Font.BOLD, 16));
        titleLabel.setBounds(10, 10, 250, 30);
        headerPanel.add(titleLabel);

        JButton closeButton = new JButton("...");
        closeButton.setBackground(new Color(235, 5, 90));
        closeButton.setBounds(560, 10, 30, 30);
        closeButton.setFocusPainted(false);
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorderPainted(false);
        closeButton.addActionListener(e -> dispose());
        headerPanel.add(closeButton);

        mainPanel.add(headerPanel);

        serverStatusLabel = new JLabel("Spring Server: Stopped");
        serverStatusLabel.setForeground(Color.WHITE);
        serverStatusLabel.setFont(new Font("Josefin Sans", Font.PLAIN, 14));
        serverStatusLabel.setBounds(20, 60, 200, 30);
        mainPanel.add(serverStatusLabel);

        startAppButton = new JButton("Start Spring server");
        startAppButton.setBounds(100, 150, 180, 50);
        startAppButton.setBackground(new Color(83, 75, 105));
        startAppButton.setForeground(Color.WHITE);
        startAppButton.setFocusPainted(false);
        startAppButton.setFont(new Font("Josefin Sans", Font.PLAIN, 14));
        mainPanel.add(startAppButton);

        selectImageButton = new JButton("Select background");
        selectImageButton.setBounds(320, 150, 180, 50);
        selectImageButton.setBackground(new Color(83, 75, 105));
        selectImageButton.setForeground(Color.WHITE);
        selectImageButton.setFocusPainted(false);
        selectImageButton.setFont(new Font("Josefin Sans", Font.PLAIN, 14));
        mainPanel.add(selectImageButton);

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setBackground(new Color(30, 32, 43));
        logTextArea.setForeground(Color.WHITE);
        logTextArea.setFont(new Font("Josefin Sans", Font.PLAIN, 12));
        logTextArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setBounds(20, 220, 560, 150);
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        mainPanel.add(scrollPane);

        contentPanel.add(mainPanel, BorderLayout.CENTER);

        startAppButton.addActionListener(e -> toggleApp());

        selectImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle("Select Background Image");
            UIManager.put("FileChooser.openDialogTitleText", "Select Background Image");
            UIManager.put("FileChooser.cancelButtonText", "Cancel");
            UIManager.put("FileChooser.openButtonText", "Select");
            UIManager.put("FileChooser.fileNameLabelText", "File Name:");
            UIManager.put("FileChooser.filesOfTypeLabelText", "Type:");
            UIManager.put("FileChooser.lookInLabelText", "Look in:");
            UIManager.put("FileChooser.saveInLabelText", "Save in:");
            UIManager.put("FileChooser.folderNameLabelText", "Folder:");
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    uploadImage(selectedFile.toPath());
                    JOptionPane.showMessageDialog(this, "Image uploaded successfully!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to upload image!");
                }
            }
        });

        final int[] pointX = new int[1];
        final int[] pointY = new int[1];

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pointX[0] = e.getX();
                pointY[0] = e.getY();
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(getLocation().x + e.getX() - pointX[0], getLocation().y + e.getY() - pointY[0]);
            }
        });
    }

    private void toggleApp() {
        if (springContext == null) {
            Executors.newSingleThreadExecutor().submit(() -> {
                springContext = SpringApplication.run(BackgroundChangerUI.class);
                EventQueue.invokeLater(() -> {
                    serverStatusLabel.setText("Spring Server: Running");
                    startAppButton.setText("Stop Spring server");
                    logTextArea.append("Spring Server started...\n");
                });

                try {
                    teamsProcess = Runtime.getRuntime().exec("teams-for-linux --customBGServiceBaseUrl=http://localhost:8080 --isCustomBackgroundEnabled=true");
                    logTextArea.append("teams-for-linux started...\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            springContext.close();
            springContext = null;
            serverStatusLabel.setText("Spring Server: Stopped");
            startAppButton.setText("Start Spring server");
            logTextArea.append("Spring Server stopped...\n");
            if (teamsProcess != null) {
                teamsProcess.destroy();
                teamsProcess = null;
                logTextArea.append("teams-for-linux stopped...\n");
            }
        }
    }

    private void uploadImage(Path imagePath) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile = new HttpPost("http://localhost:8080/upload");
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", new FileInputStream(imagePath.toFile()), ContentType.APPLICATION_OCTET_STREAM, imagePath.getFileName().toString());
            uploadFile.setEntity(builder.build());

            HttpResponse response = httpClient.execute(uploadFile);
            String responseString = EntityUtils.toString(response.getEntity());
            logTextArea.append("Response: " + responseString + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BackgroundChangerUI ui = new BackgroundChangerUI();
            ui.setVisible(true);
        });
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();
            Color color1 = new Color(24, 24, 24);
            Color color2 = new Color(30, 32, 43);
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
        }
    }

    private static class GradientHeaderPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();
            Color color1 = new Color(235, 5, 90);
            Color color2 = new Color(235, 5, 190);
            GradientPaint gp = new GradientPaint(0, 0, color1, width, 0, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
        }
    }

    private static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(83, 75, 105);
            this.trackColor = new Color(30, 32, 43);
        }
    }
}
