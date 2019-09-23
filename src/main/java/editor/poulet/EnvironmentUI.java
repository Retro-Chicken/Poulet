package editor.poulet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static editor.ui.MainUI.pointX;
import static editor.ui.MainUI.pointY;

public class EnvironmentUI extends JFrame implements ActionListener {
    private JTable environment;

    private JButton remove;
    private JButton reset;

    private JButton add;
    private JButton apply;

    public EnvironmentUI(String title) {
        super(title);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        //updateEnvironment();
        environment = new JTable() {
            private static final long serialVersionUID = 1L;

            @Override
            public Class getColumnClass(int column) {
                switch(column) {
                    case 0:
                        return String.class;
                    case 1:
                        return Boolean.class;
                    default:
                        return null;
                }
            }
        };
        environment.getSelectionModel().addListSelectionListener(e -> remove.setEnabled(!environment.getSelectionModel().isSelectionEmpty()));
        environment.setPreferredScrollableViewportSize(new Dimension(
                environment.getPreferredScrollableViewportSize().width,
                environment.getPreferredScrollableViewportSize().height/2
        ));
        panel.add(new JScrollPane(environment));

        remove = new JButton("Remove");
        remove.addActionListener(this);
        reset = new JButton("Reset");
        reset.addActionListener(this);

        add = new JButton("Add");
        add.addActionListener(this);
        apply = new JButton("Apply");
        apply.addActionListener(this);

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(2, 2));
        buttons.add(add);
        buttons.add(remove);
        buttons.add(reset);
        buttons.add(apply);
        panel.add(buttons);

        this.add(panel);

        this.setVisible(false);
        this.setFocusable(true);
        setResizable(false);

        //setAlwaysOnTop(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                EnvironmentUI.this.setVisible(false);
            }
        });

        pack();
    }

    private void updateEnvironment() {
        String[] titles = new String[]{ "Directory", "Recursive" };
        Object[][] contents = new Object[EnvironmentUtil.directories.size()][2];
        for(int i = 0; i < EnvironmentUtil.directories.size(); i++) {
            try {
                contents[i][0] = EnvironmentUtil.directories.get(i).getCanonicalPath();
            } catch (IOException e) {
                contents[i][0] = EnvironmentUtil.directories.get(i).getAbsolutePath();
            }
            contents[i][1] = EnvironmentUtil.recursive.get(i);
        }

        environment.setModel(new DefaultTableModel(contents, titles) {
            private static final long serialVersionUID = 1L;
            boolean[] canEdit = new boolean[] { false, true };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        environment.getModel().addTableModelListener(e -> {
            apply.setEnabled(true);
            reset.setEnabled(true);
        });
    }

    public void display() {
        updateEnvironment();
        remove.setEnabled(false);
        apply.setEnabled(false);
        reset.setEnabled(false);
        this.setLocation(pointX + 100, pointY + 150);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(apply)) {
            exportTableData();
            apply.setEnabled(false);
            reset.setEnabled(false);
        } else if(e.getSource().equals(add)) {
            addDirectory();
        } else if(e.getSource().equals(reset)) {
            updateEnvironment();
            apply.setEnabled(false);
            reset.setEnabled(false);
        } else if(e.getSource().equals(remove)) {
            ((DefaultTableModel) environment.getModel()).removeRow(environment.getSelectedRow());
        }
    }

    private void exportTableData() {
        EnvironmentUtil.setEnvironment(getCurrentDirectories(), getCurrentRecursive());
    }

    private List<File> getCurrentDirectories() {
        List<File> directories = new ArrayList<>();
        for(int i = 0; i < environment.getRowCount(); i++) {
            directories.add(new File((String) environment.getValueAt(i, 0)));
        }
        return directories;
    }

    private List<Boolean> getCurrentRecursive() {
        List<Boolean> recursive = new ArrayList<>();
        for(int i = 0; i < environment.getRowCount(); i++) {
            recursive.add((Boolean) environment.getValueAt(i, 1));
        }
        return recursive;
    }

    private void addDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(PouletCommon.ADD_DIRECTORY);
        int ret = chooser.showOpenDialog(null);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File directory = chooser.getSelectedFile();

            if(getCurrentDirectories().contains(directory))
                return;

            String path;
            try {
                path = directory.getCanonicalPath();
            } catch(IOException e) {
                path = directory.getAbsolutePath();
            }

            ((DefaultTableModel) environment.getModel()).addRow(new Object[]{ path, false });
        }
    }
}
