package editor.poulet;

import editor.common.Common;
import editor.ui.MainUI;
import editor.util.FileMenuUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

public class PouletUI extends MainUI {
    private static JTextArea console;
    private JScrollPane consoleScroll;

    private JMenu run;
    private JMenuItem runFile;

    public PouletUI(String title) {
        super(title);
    }

    public void init() {
        initMenu();
        menuRun();
        initTextArea();
        initConsole();

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        JLabel textLabel = new JLabel("File Text", SwingConstants.LEFT);
        textLabel.setFont(new Font(textLabel.getFont().getFontName(), 1, 12));
        textPanel.add(textLabel, BorderLayout.NORTH);
        textPanel.add(textAreaScroll, BorderLayout.CENTER);

        JPanel consolePanel = new JPanel();
        consolePanel.setLayout(new BorderLayout());
        JLabel consoleLabel = new JLabel("Console", SwingConstants.LEFT);
        consoleLabel.setFont(new Font(consoleLabel.getFont().getFontName(), 1, 12));
        consolePanel.add(consoleLabel, BorderLayout.NORTH);
        consolePanel.add(consoleScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textPanel, consolePanel);
        this.add(splitPane);

        this.setResizable(true);
        this.setBounds(new Rectangle(150, 100, 800, 550));
        this.setVisible(true);

        splitPane.setDividerLocation(0.75);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                FileMenuUtil file = new FileMenuUtil(Common.EMPTY);
                file.exit(PouletUI.this);
            }
        });

        setMainUIXY();
    }

    private void initConsole() {
        this.console = new JTextArea(Common.EMPTY);
        console.setEditable(false);
        console.setLineWrap(true);
        console.setFont(new Font(PouletCommon.CONSOLE_FONT, PouletCommon.CONSOLE_FONT_STYLE_NUM, PouletCommon.CONSOLE_FONT_SIZE));

        consoleScroll = new JScrollPane(console);
    }

    private void menuRun() {
        run = new JMenu(PouletCommon.RUN);

        runFile = new JMenuItem(PouletCommon.RUN_FILE);
        runFile.addActionListener(this);
        runFile.setAccelerator(KeyStroke.getKeyStroke(Common.R, InputEvent.CTRL_DOWN_MASK));
        run.add(runFile);

        menuBar.add(run, menuBar.getComponentCount() - 2);
    }

    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if(e.getSource() == runFile) {
            console.setText(PouletCommon.RUNNING);

            StringWriter buffer = new StringWriter();
            PrintWriter writer = new PrintWriter(buffer);

            long startTime = System.currentTimeMillis();
            PouletRunner.runString(textArea.getText(), writer);
            long elapsedTime = System.currentTimeMillis() - startTime;

            String contents = buffer.toString();
            console.setText(contents);
            console.append("\n\n" + PouletCommon.FINISHED_IN + " " + elapsedTime + PouletCommon.ELAPSED_TIME_UNIT);
        }
    }
}
