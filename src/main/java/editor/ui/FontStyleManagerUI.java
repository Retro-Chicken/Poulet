package editor.ui;

import editor.common.Common;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

/**
 * @author Hongten
 * @created Nov 20, 2014
 */
public class FontStyleManagerUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = -37011351219515242L;

	private final MainUI parent;

	private JLabel currentFontStyleDescJLabel;
	private JLabel currentFontStyleJLabel;
	private JLabel descJLabel;
	private JSeparator line;
	private JComboBox<String> fontStyleJComboBox;

	String fontStyles[] = {"Regular", "Bold", "Italic", "Bold Italic"};
	
	public FontStyleManagerUI(String title, MainUI parent) {
		super(title);
		this.parent = parent;
		initComponents();
		this.setVisible(false);
		setResizable(false);
		setAlwaysOnTop(true);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});
	}

	private void initComponents() {
		initElement();
		currentFontStyleJLabel.setText(Common.CURRENT_FONT_STYLE);

		fontStyleJComboBox.setModel(new DefaultComboBoxModel<>(fontStyles));
		int i = 0;
		for(String 	style : fontStyles){
			if(style.equals(FontManagerUI.FONT_STYLE)){
				parent.fontStyleNum = i;
			}
			i++;
		}
		fontStyleJComboBox.setSelectedIndex(parent.fontStyleNum);
		fontStyleJComboBox.addActionListener(this);

		descJLabel.setText(Common.DESCRIPTION_WITH_COLON);
		// do here...
		currentFontStyleDescJLabel.setFont(new Font(FontManagerUI.FONT_TYPE, parent.fontStyleNum, FontManagerUI.FONT_SIZE));
		currentFontStyleDescJLabel.setText(Common.SAMPLE);
		pageGroupLayout();
	}

	private void initElement() {
		currentFontStyleJLabel = new JLabel();
		fontStyleJComboBox = new JComboBox<String>();
		descJLabel = new JLabel();
		currentFontStyleDescJLabel = new JLabel();
		line = new JSeparator();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fontStyleJComboBox) {
			updateSkin();
		}
	}

	public synchronized void updateSkin() {
		parent.fontStyleNum = fontStyleJComboBox.getSelectedIndex();
		FontManagerUI.FONT_STYLE = (String) fontStyleJComboBox.getSelectedItem();
		currentFontStyleDescJLabel.setFont(new Font(FontManagerUI.FONT_TYPE, parent.fontStyleNum, FontManagerUI.FONT_SIZE));
		currentFontStyleDescJLabel.setText(Common.SAMPLE);
		for(JTextArea area : parent.getTextAreas())
			area.setFont(new Font(FontManagerUI.FONT_TYPE, parent.fontStyleNum, FontManagerUI.FONT_SIZE));
		parent.setJUI();
	}

	/**
	 * If not necessary, please do not change
	 */
	private void pageGroupLayout() {
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		horizontalGroupLayout(layout);
		verticalGroupLayout(layout);
		pack();
	}

	private void verticalGroupLayout(GroupLayout layout) {
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addGap(40, 40, 40)
						.addGroup(
								layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(currentFontStyleJLabel)
										.addComponent(fontStyleJComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(26, 26, 26)
						.addComponent(line, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(descJLabel).addGap(18, 18, 18).addComponent(currentFontStyleDescJLabel).addContainerGap(47, Short.MAX_VALUE)));
	}

	private void horizontalGroupLayout(GroupLayout layout) {
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGap(21, 21, 21)
								.addGroup(
										layout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addComponent(currentFontStyleDescJLabel)
												.addComponent(descJLabel)
												.addGroup(
														layout.createSequentialGroup().addComponent(currentFontStyleJLabel).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																.addComponent(fontStyleJComboBox, GroupLayout.PREFERRED_SIZE, 195, GroupLayout.PREFERRED_SIZE)))
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addComponent(line, GroupLayout.PREFERRED_SIZE, 355, GroupLayout.PREFERRED_SIZE).addGap(0, 0, Short.MAX_VALUE)));
	}

	public void display() {
		setVisible(true);
		this.setLocation(parent.pointX + 100, parent.pointY + 150);
	}
}
