package editor.ui;

import editor.common.Common;
import editor.util.EditMenuUtil;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;


/**
 * @author Hongten
 * @created Nov 20, 2014
 */
public class FindManagerUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private final MainUI parent;
	private final EditMenuUtil edit;

	private JPanel bGJPanel;
	private JRadioButton backwardJRadioButton;
	private JButton cancelJButton;
	private JCheckBox caseSensitiveJCheckBox;
	private JButton findNextJButton;
	private JLabel findWhatJLabel;
	private JRadioButton forwardJRadioButton;
	private JTextField keyWordJTextField;

	public static boolean isForward = true;
	public static boolean isCaseSensitive = false;

	public FindManagerUI(String title, EditMenuUtil edit, MainUI parent) {
		super(title);
		this.parent = parent;
		this.edit = edit;
		initComponents();
		setResizable(false);
		setVisible(false);
		setAlwaysOnTop(true);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});
	}

	/**
	 * If not necessary, do not change the order.
	 */
	private void initComponents() {
		initElements();
		initFindWhat();
		initCaseSensitive();
		initFindNext();
		initCancel();
		initDirection();
		initLayout();
	}
	
	private void initElements() {
		bGJPanel = new JPanel();
		findWhatJLabel = new JLabel();
		keyWordJTextField = new JTextField();
		caseSensitiveJCheckBox = new JCheckBox();
		findNextJButton = new JButton();
		cancelJButton = new JButton();
		forwardJRadioButton = new JRadioButton();
		backwardJRadioButton = new JRadioButton();
	}

	private void initDirection() {
		forwardJRadioButton.setSelected(true);
		forwardJRadioButton.setText(Common.FORWARD);
		forwardJRadioButton.addActionListener(this);

		backwardJRadioButton.setText(Common.BACKWARD);
		backwardJRadioButton.addActionListener(this);
	}

	private void initCancel() {
		cancelJButton.setText(Common.CANCEL);
		cancelJButton.setMaximumSize(new Dimension(87, 23));
		cancelJButton.setMinimumSize(new Dimension(87, 23));
		cancelJButton.setPreferredSize(new Dimension(87, 23));
		cancelJButton.addActionListener(this);
	}

	private void initFindNext() {
		findNextJButton.setText(Common.FIND_NEXT);
		findNextJButton.addActionListener(this);
	}

	private void initCaseSensitive() {
		caseSensitiveJCheckBox.setText(Common.CASE_SENSITIVE);
		caseSensitiveJCheckBox.addActionListener(this);
	}

	private void initFindWhat() {
		findWhatJLabel.setText(Common.FIND_WHAT);
		
		if (null == parent.getSelectedTextArea().getSelectedText() || Common.EMPTY.equals(parent.getSelectedTextArea().getSelectedText().trim())) {
			keyWordJTextField.setText(parent.findWhat);
		} else if(null != parent.getSelectedTextArea().getSelectedText() && !Common.EMPTY.equals(parent.getSelectedTextArea().getSelectedText().trim())){
			keyWordJTextField.setText(parent.getSelectedTextArea().getSelectedText());
		}else{
			keyWordJTextField.setText(parent.findWhat);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == backwardJRadioButton) {
			directionOfOperation(false);
		} else if (e.getSource() == forwardJRadioButton) {
			directionOfOperation(true);
		} else if (e.getSource() == findNextJButton) {
			findNextOperation();
		} else if (e.getSource() == cancelJButton) {
			setVisible(false);
		} else if (e.getSource() == caseSensitiveJCheckBox) {
			caseSensitiveSwitch();
		}
	}

	private void findNextOperation() {
		parent.findWhat = keyWordJTextField.getText();
		if (Common.EMPTY.equals(parent.findWhat)) {
			JOptionPane.showMessageDialog(FindManagerUI.this, Common.WHAT_DO_YOU_WANT_TO_FIND, Common.NOTEPAD, JOptionPane.INFORMATION_MESSAGE);
			keyWordJTextField.setFocusable(true);
		}
		edit.findNext(this);
	}

	/**
	 * Case Sensitive Switch 
	 */
	private void caseSensitiveSwitch() {
		if (null == caseSensitiveJCheckBox.getSelectedObjects()) {
			isCaseSensitive = false;
		} else {
			isCaseSensitive = true;
		}
	}

	/**
	 * Direction of Operation<br>
	 * <li>Forward  : <code>directionOfOperation(true);</code></li>
	 * <li>Backward : <code>directionOfOperation(false);</code></li>
	 * @param b <code>b = true;</code> Forward is selected; <code>b = false;</code> Backward is selected.<br>
	 */
	private void directionOfOperation(boolean b) {
		isForward = b;
		forwardJRadioButton.setSelected(b);
		backwardJRadioButton.setSelected(!b);
	}

	/**
	 * If not necessary, do not change.
	 */
	private void initLayout() {
		GroupLayout bGJPanelLayout = new GroupLayout(bGJPanel);
		bGJPanel.setLayout(bGJPanelLayout);
		bGJPanelLayout.setHorizontalGroup(bGJPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(bGJPanelLayout.createSequentialGroup().addContainerGap().addGroup(bGJPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(bGJPanelLayout.createSequentialGroup().addComponent(findWhatJLabel).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(keyWordJTextField, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE)).addGroup(bGJPanelLayout.createSequentialGroup().addComponent(caseSensitiveJCheckBox).addGap(18, 18, 18).addComponent(forwardJRadioButton).addGap(18, 18, 18).addComponent(backwardJRadioButton))).addGap(18, 18, 18).addGroup(bGJPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(findNextJButton, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(cancelJButton, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
		bGJPanelLayout.setVerticalGroup(bGJPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(bGJPanelLayout.createSequentialGroup().addGap(14, 14, 14).addGroup(bGJPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(findWhatJLabel).addComponent(keyWordJTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(findNextJButton)).addGap(18, 18, 18).addGroup(bGJPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(cancelJButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(caseSensitiveJCheckBox).addComponent(forwardJRadioButton).addComponent(backwardJRadioButton)).addContainerGap()));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(bGJPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(bGJPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addContainerGap()));
		pack();
	}

	public void display() {
		this.setLocation(parent.pointX + 100, parent.pointY + 150);
		this.setVisible(true);
	}
}
