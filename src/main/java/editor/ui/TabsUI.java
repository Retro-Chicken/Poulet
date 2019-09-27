package editor.ui;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

public class TabsUI extends JFrame implements ActionListener, PropertyChangeListener {
    private final MainUI parent;

    private JRadioButton tabs, spaces;
    private JFormattedTextField spaceCount;

    public TabsUI(String title, MainUI parent) {
        super(title);
        this.parent = parent;
        this.setVisible(false);
        setResizable(false);
        setAlwaysOnTop(true);

        tabs = new JRadioButton("Use Tabs");
        tabs.addActionListener(this);
        spaces = new JRadioButton("Use Spaces: ");
        spaces.addActionListener(this);

        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(1);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        spaceCount = new JFormattedTextField(formatter);
        spaceCount.setValue(parent.tabSpaces);
        spaceCount.addPropertyChangeListener(this);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(tabs);
        buttonGroup.add(spaces);

        this.setLayout(new GridLayout(2, 2));
        this.add(tabs);
        this.add(new JPanel());
        this.add(spaces);
        this.add(spaceCount);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(tabs) || e.getSource().equals(spaces)) {
            parent.useTabs = tabs.isSelected();
            setSpacing();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if(e.getSource().equals(spaceCount)) {
            parent.tabSpaces = (Integer) spaceCount.getValue();
            setSpacing();
        }
    }

    private void setSpacing() {
        parent.getTextAreas().stream().forEach(x -> x.setTabSize(
                parent.useTabs ? null : parent.tabSpaces
        ));
    }

    private void updateInfo() {
        spaceCount.setValue(parent.tabSpaces);
        tabs.setSelected(parent.useTabs);
        spaces.setSelected(!parent.useTabs);
    }

    public void display() {
        updateInfo();
        setVisible(true);
        this.setLocation(parent.pointX + 100, parent.pointY + 150);
    }
}
