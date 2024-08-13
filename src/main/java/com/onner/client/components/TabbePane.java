package com.onner.client.components;

import java.awt.FlowLayout;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;


public class TabbePane {
    private JTabbedPane tabbedPane;
    public TabbePane(){
        tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new TabbedPaneUI());
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public void addTabbedPane(String title, JPanel component) {
        addTab(title, component);
    }
    private void addTab(String title, JComponent component) {
        tabbedPane.addTab(null, component);
        int index = tabbedPane.getTabCount() - 1;
        tabbedPane.setTabComponentAt(index, new TabComponent(tabbedPane, title));
    }
}

class TabComponent extends JPanel {
    private final JTabbedPane pane;

    public TabComponent(final JTabbedPane pane, String title) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.pane = pane;
        setOpaque(false);

        JLabel label = new JLabel(title);
        add(label);

        JButton button = new JButton();
        button.setOpaque(false);
        button.setSize(20,20);
        ImageIcon icono=new ImageIcon("E:/chatSocket/src/main/java/resources/close.png");
        button.setIcon(new ImageIcon(icono.getImage().getScaledInstance( button.getSize().width,  button.getSize().height, Image.SCALE_SMOOTH)));

        button.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 1));
        button.setContentAreaFilled(false);
        button.addActionListener(e -> {
            int i = pane.indexOfTabComponent(TabComponent.this);
            if (i != -1) {
                pane.remove(i);
            }
        });
        add(button);
    }
}