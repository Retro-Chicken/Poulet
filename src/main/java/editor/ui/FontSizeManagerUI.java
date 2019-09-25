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
public class FontSizeManagerUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = -37011351219515242L;

	private final MainUI parent;

	private JLabel currentFontSizeDescJLabel;
	private JLabel currentFontSizeJLabel;
	private JLabel descJLabel;
	private JSeparator line;
	private JComboBox<String> fontSizeJComboBox;

	String fontSizes[] = {"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"};
	
	public FontSizeManagerUI(String title, MainUI parent) {
		super(title);
		this.parent = parent;
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

	private void initComponents() {
		initElement();
		currentFontSizeJLabel.setText(Common.CURRENT_FONT_SIZE);

		fontSizeJComboBox.setModel(new DefaultComboBoxModel<String>(fontSizes));
		int i = 0;
		for(String size : fontSizes){
			if(Integer.valueOf(size) == FontManagerUI.FONT_SIZE){
				parent.fontSizeNum = i;
			}
			i++;
		}
		fontSizeJComboBox.setSelectedIndex(parent.fontSizeNum);
		fontSizeJComboBox.addActionListener(this);

		descJLabel.setText(Common.DESCRIPTION_WITH_COLON);

		currentFontSizeDescJLabel.setFont(new Font(FontManagerUI.FONT_TYPE, parent.fontStyleNum, FontManagerUI.FONT_SIZE));
		currentFontSizeDescJLabel.setText(Common.SAMPLE);
		pageGroupLayout();
	}

	private void initElement() {
		currentFontSizeJLabel = new JLabel();
		fontSizeJComboBox = new JComboBox<String>();
		descJLabel = new JLabel();
		currentFontSizeDescJLabel = new JLabel();
		line = new JSeparator();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fontSizeJComboBox) {
			updateSkin();
		}
	}

	public synchronized void updateSkin() {
		parent.fontNum = fontSizeJComboBox.getSelectedIndex();
		FontManagerUI.FONT_SIZE = Integer.valueOf((String) fontSizeJComboBox.getSelectedItem());
		currentFontSizeDescJLabel.setFont(new Font(FontManagerUI.FONT_TYPE, Font.PLAIN, FontManagerUI.FONT_SIZE));
		currentFontSizeDescJLabel.setText(Common.SAMPLE);
		for(JTextArea area : parent.getTextAreas())
			area.setFont(new Font(FontManagerUI.FONT_TYPE, Font.PLAIN, FontManagerUI.FONT_SIZE));
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
								layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(currentFontSizeJLabel)
										.addComponent(fontSizeJComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(26, 26, 26)
						.addComponent(line, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(descJLabel).addGap(18, 18, 18).addComponent(currentFontSizeDescJLabel).addContainerGap(47, Short.MAX_VALUE)));
	}

	private void horizontalGroupLayout(GroupLayout layout) {
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGap(21, 21, 21)
								.addGroup(
										layout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addComponent(currentFontSizeDescJLabel)
												.addComponent(descJLabel)
												.addGroup(
														layout.createSequentialGroup().addComponent(currentFontSizeJLabel).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																.addComponent(fontSizeJComboBox, GroupLayout.PREFERRED_SIZE, 195, GroupLayout.PREFERRED_SIZE)))
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addComponent(line, GroupLayout.PREFERRED_SIZE, 355, GroupLayout.PREFERRED_SIZE).addGap(0, 0, Short.MAX_VALUE)));
	}

	public void display() {
		setVisible(true);
		this.setLocation(parent.pointX + 100, parent.pointY + 150);
	}
}
