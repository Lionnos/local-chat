package com.onner.client.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class TabbedPaneUI extends BasicTabbedPaneUI{
    private static final Color SELECTED_TAB_COLOR = new Color(255, 255, 255);
    private static final Color UNSELECTED_TAB_COLOR = new Color(240, 240, 240);
    private static final Color SELECTED_BORDER_COLOR = new Color(0, 120, 215);

    @Override
    protected void installDefaults() {
        super.installDefaults();
        highlight = new Color(0, 0, 0, 0);
        lightHighlight = new Color(0, 0, 0, 0);
        shadow = new Color(0, 0, 0, 0);
        darkShadow = new Color(0, 0, 0, 0);
        focus = new Color(0, 0, 0, 0);
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dibuja el fondo de la pestaña
        g2.setColor(isSelected ? SELECTED_TAB_COLOR : UNSELECTED_TAB_COLOR);
        g2.fillRoundRect(x, y, w, h, 10, 10);

        // Dibuja el borde inferior para la pestaña seleccionada
        if (isSelected) {
            g2.setColor(SELECTED_BORDER_COLOR);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(x, y + h - 1, x + w, y + h - 1);
        }

        g2.dispose();
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        // No hacemos nada aquí, ya que el fondo se dibuja en paintTabBorder
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        // No pintamos el borde del contenido para un aspecto más limpio
    }
}
