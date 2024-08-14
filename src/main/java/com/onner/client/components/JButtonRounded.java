package com.onner.client.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class JButtonRounded extends JButton {
    private static final int CIRCLE_DIAMETER = 20;
    private static final int CIRCLE_X_OFFSET = 5;
    private static final int CIRCLE_Y_OFFSET = 5;
    private int counter = 0;
    private SoundProcess soundProcess;

    public JButtonRounded(String text) {
        super(text);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(true);
        setBackground(Color.decode("#25D366"));
        setForeground(Color.WHITE);
        setFont(new Font("Arial", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
        super.paintComponent(g);
        drawCounter(g2d);
    }

    private void drawCounter(Graphics2D g2d) {
        if (counter > 0) {
            g2d.setColor(Color.decode("#e67e22"));
            int x = getWidth() - CIRCLE_DIAMETER - CIRCLE_X_OFFSET;
            int y = CIRCLE_Y_OFFSET;
            g2d.fillOval(x, y, CIRCLE_DIAMETER, CIRCLE_DIAMETER);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String counterText = String.valueOf(counter);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(counterText);
            int textHeight = fm.getAscent();
            g2d.drawString(counterText, x + (CIRCLE_DIAMETER - textWidth) / 2, y + (CIRCLE_DIAMETER + textHeight) / 2 - 2);
        }
    }

    public void incrementCounter(SoundProcess soundProcess) {
        soundProcess.startSoundChat();
        counter++;
        repaint();
    }

    public void resetCounter() {
        this.counter = 0;
    }

    public void setButtonBackgroundColor(String colorHex) {
        setBackground(Color.decode(colorHex));
    }
}
