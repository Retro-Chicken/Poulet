package editor.ui;

import editor.common.Common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.*;

/**
 * @author Hongten
 * @created Nov 20, 2014
 */
public class SkinManagerUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private final MainUI parent;

	private JLabel currentSkinJLabel;
	private JComboBox<String> sinkJComboBox;

	public String[] skins = {
			"Autumn",
			"Business Black Steel",
			"Challenger Deep",
			"Creme Coffee",
			"Creme Skin",
			"Ebony High Contrast",
			"Emerald Dusk",
			"Field Of Wheat",
			"Finding Nemo",
			"Green Magic",
			"Magma",
			"Mango",
			"Mist Silver",
			"Moderate",
			"Nebula Brick Wall",
			"Nebula",
			"Office Blue 2007",
			"Raven Graphite Glass",
			"Raven Graphite",
			"Raven",
			"Sahara"
	};

	public SkinManagerUI(String title, MainUI parent) {
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
		currentSkinJLabel = new JLabel();
		sinkJComboBox = new JComboBox<>();

		currentSkinJLabel.setText(Common.CURRENT_SKIN);

		sinkJComboBox.setModel(new DefaultComboBoxModel<>(skins));
		sinkJComboBox.setSelectedIndex(parent.skinNum - 1);
		sinkJComboBox.addActionListener(this);

		pageGroupLayout();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == sinkJComboBox) {
			updateSkin();
		}
	}

	public synchronized void updateSkin() {
		parent.skinNum = Arrays.asList(skins).indexOf(sinkJComboBox.getSelectedItem()) + 1;
		parent.setJUI();
	}

	/**
	 * If not necessary, please do not change
	 */
	private void pageGroupLayout() {
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
						.addGap(21, 21, 21)
						.addGroup(
								layout.createSequentialGroup()
								.addComponent(currentSkinJLabel)
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(sinkJComboBox, GroupLayout.PREFERRED_SIZE, 195, GroupLayout.PREFERRED_SIZE)
						).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				)
		);

		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
						.addGap(40, 40, 40)
						.addGroup(
								layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(currentSkinJLabel)
								.addComponent(sinkJComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						)
						.addGap(40, 40, 40)
				)
		);

		pack();
	}

	public void display() {
		this.setLocation(parent.pointX + 100, parent.pointY + 150);
	}
}
