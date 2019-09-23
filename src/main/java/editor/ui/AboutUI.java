package editor.ui;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import editor.common.Common;
import editor.util.HelpMenuUtil;
import editor.util.NotepadUtil;
//import org.apache.log4j.Logger;

/**
 * Location : MainUI --> Help --> About Notepad<br>
 * <p>
 * The <code>AboutUI</code> display the information about this application.<br>
 * <p>
 * i.e., Author, Application Name, Application description, Version, Blog.etc.<br>
 * <p>
 * If you have a try to double-click the row which name is 'Blog', then the dialog will be displaying in front of this page.<br>
 * The dialog is a access URL request dialog, and you will access the URL(<a href='http://www.cnblogs.com/hongten'>http://www.cnblogs.com/hongten</a>) if you click 'Yes'.<br>
 * <p>
 * If you want to use this class, you should do as below:<br>
 * <p><blockquote><pre>
 *     <code>AboutUI aboutUI = new AboutUI("About Notepad");</code>
 * </pre></blockquote><p>
 * 
 * @author Hongten
 * @created Nov 20, 2014
 */
public class AboutUI extends MainUI {
	
	private static final long serialVersionUID = 1L;
	
	//static Logger log = Logger.getLogger(AboutUI.class);

	private JButton aboutButton;
	private JTable aboutUITable;
	private JPanel mainPanel;
	private JScrollPane rightScrollPane;
	
	private HelpMenuUtil help;

	public AboutUI(String title) {
		super(title);
		initComponents();
		initSelf();
		setAlwaysOnTop(true);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				AboutUI.this.setVisible(false);
				help.destroyAboutUI();
			}
		});
	}

	public void initSelf() {
		this.setVisible(true);
		setResizable(false);
		this.setLocation(pointX + 100, pointY + 150);
	}

	private void initComponents() {
		initElement();
		initAboutButton();
		initAboutUITable();
		mainPanelLayout();
	}

	private void initAboutButton() {
		aboutButton.setIcon(new ImageIcon("src/main/java/editor/" + Common.RC_LOGO));
		aboutButton.setToolTipText(Common.ABOUT_NOTEPAD);
	}

	private void initAboutUITable() {
		Object[][] values = new Object[][] { { Common.AUTHOR, Common.AUTHOR_NAME }, { Common.APPLICATION_NAME, Common.NOTEPAD_APP }, { Common.APPLICATION_DESCRIPTION, Common.APPLICATION_DESCRIPTION_DETAIL }, { Common.VERSION, Common.VERSION_VALUE }, { Common.HOME_PAGE, Common.HOME_PAGE_URL} };

		String[] titles = new String[] { Common.ITEM, Common.DESCRIPTION };

		aboutUITable.setModel(new DefaultTableModel(values, titles) {
			private static final long serialVersionUID = 1L;
			boolean[] canEdit = new boolean[] { false, false };

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});

		aboutUITable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		aboutUITable.setOpaque(false);
		aboutUITable.setRowHeight(Common.TABLE_ROW_HEIGHT);
		aboutUITable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		aboutUITable.setSurrendersFocusOnKeystroke(true);
		aboutUITable.getTableHeader().setReorderingAllowed(false);
		aboutUITable.addMouseListener(new MouseListener() {

			public void mouseReleased(MouseEvent e) {

			}

			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
					matchUrlOperation();
				}
			}

			public void mouseExited(MouseEvent e) {

			}

			public void mouseEntered(MouseEvent e) {

			}

			public void mouseClicked(MouseEvent e) {

			}
		});
		rightScrollPane.setViewportView(aboutUITable);
	}

	private void matchUrlOperation() {
		int id = aboutUITable.getSelectedRow();
		String url = (String) aboutUITable.getValueAt(id, 1);
		if (url.equals(Common.HOME_PAGE_URL)) {
			askAccessBlogOperation();
		}
	}

	// Show a dialog to access URL request.
	// You will access the URL if you click 'Yes'.
	protected void askAccessBlogOperation() {
		int option = JOptionPane.showConfirmDialog(AboutUI.this, Common.ACCESS_URL + Common.HOME_PAGE_URL + Common.BLANK + Common.QUESTION_MARK, Common.ACCESS_URL_REQUEST, JOptionPane.YES_NO_OPTION);
		if (option == JOptionPane.YES_OPTION) {
			NotepadUtil.accessURL(Common.HOME_PAGE_URL);
		}
	}

	private void initElement() {
		mainPanel = new JPanel();
		aboutButton = new JButton();
		rightScrollPane = new JScrollPane();
		aboutUITable = new JTable();
	}
	
	public void setHelpMenuUtil(HelpMenuUtil helpMenuUtil){
		this.help = helpMenuUtil;
	}

	/**
	 * If not necessary, please do not change
	 */
	private void mainPanelLayout() {
		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);
		mainPanelLayout.setHorizontalGroup(
				mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						mainPanelLayout.createSequentialGroup().addContainerGap()
						.addComponent(aboutButton)
						.addGap(18, 18, 18)
						.addComponent(rightScrollPane, GroupLayout.PREFERRED_SIZE, 243, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				)
		);
		mainPanelLayout.setVerticalGroup(
				mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						mainPanelLayout.createSequentialGroup().addContainerGap()
						.addGroup(
								mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
								.addComponent(rightScrollPane, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
								.addComponent(aboutButton, GroupLayout.PREFERRED_SIZE, 256, GroupLayout.PREFERRED_SIZE)
						).addGap(0, 0, Short.MAX_VALUE)
				)
		);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap()
				)
		);
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap()
				)
		);

		pack();
	}
}
