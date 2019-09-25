package editor.ui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import editor.common.Common;

/**
 * @author Hongten
 * @created Nov 20, 2014
 */
public class FontManagerUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = -37011351219515242L;

	private final MainUI parent;

	private JLabel currentFontDescJLabel;
	private JLabel currentFontJLabel;
	private JLabel descJLabel;
	private JSeparator line;
	private JComboBox<String> fontJComboBox;

	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	String fontNames[] = ge.getAvailableFontFamilyNames();
	
	public static String FONT_TYPE = Common.FONT_TYPE;
	public static int FONT_SIZE = Common.FONT_SIZE;
	public static String FONT_STYLE = Common.FONT_STYLE_DEFAULT;
	
	public FontManagerUI(String title, MainUI parent) {
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
		currentFontJLabel.setText(Common.CURRENT_FONT);

		fontJComboBox.setModel(new DefaultComboBoxModel<>(fontNames));
		int i = 0;
		for(String name : fontNames){
			if(FontManagerUI.FONT_TYPE.equals(name)){
				parent.fontNum = i;
			}
			i++;
		}
		fontJComboBox.setSelectedIndex(parent.fontNum);
		fontJComboBox.addActionListener(this);

		descJLabel.setText(Common.DESCRIPTION_WITH_COLON);

		currentFontDescJLabel.setFont(new Font(FontManagerUI.FONT_TYPE, parent.fontStyleNum, FontManagerUI.FONT_SIZE));
		currentFontDescJLabel.setText(Common.THIS_IS_A_SAMPLE);
		pageGroupLayout();
	}

	private void initElement() {
		currentFontJLabel = new JLabel();
		fontJComboBox = new JComboBox<>();
		descJLabel = new JLabel();
		currentFontDescJLabel = new JLabel();
		line = new JSeparator();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fontJComboBox) {
			updateSkin();
		}
	}

	public synchronized void updateSkin() {
		parent.fontNum = fontJComboBox.getSelectedIndex();
		FontManagerUI.FONT_TYPE = fontJComboBox.getSelectedItem().toString();
		currentFontDescJLabel.setFont(new Font(FontManagerUI.FONT_TYPE, parent.fontStyleNum, FontManagerUI.FONT_SIZE));
		currentFontDescJLabel.setText(Common.THIS_IS_A_SAMPLE);
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
								layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(currentFontJLabel)
										.addComponent(fontJComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(26, 26, 26)
						.addComponent(line, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(descJLabel).addGap(18, 18, 18).addComponent(currentFontDescJLabel).addContainerGap(47, Short.MAX_VALUE)));
	}

	private void horizontalGroupLayout(GroupLayout layout) {
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGap(21, 21, 21)
								.addGroup(
										layout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addComponent(currentFontDescJLabel)
												.addComponent(descJLabel)
												.addGroup(
														layout.createSequentialGroup().addComponent(currentFontJLabel).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																.addComponent(fontJComboBox, GroupLayout.PREFERRED_SIZE, 195, GroupLayout.PREFERRED_SIZE)))
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addComponent(line, GroupLayout.PREFERRED_SIZE, 355, GroupLayout.PREFERRED_SIZE).addGap(0, 0, Short.MAX_VALUE)));
	}

	public void display() {
		setVisible(true);
		this.setLocation(parent.pointX + 100, parent.pointY + 150);
	}
}
