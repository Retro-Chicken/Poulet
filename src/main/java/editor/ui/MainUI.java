/**
 * 
 */
package editor.ui;

import editor.client.Client;
import editor.common.Common;
import editor.util.*;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.undo.UndoManager;

/**
 * @author Hongten
 * @created Nov 19, 2014
 */
public class MainUI extends NotepadUI {

	private static final long serialVersionUID = 1L;

	protected static JMenuBar menuBar;
	protected static JSeparator line;
	// Menus
	protected static JMenu file, edit, format, view, help, viewHelp;
	// PopupMenu
	protected static JPopupMenu textAreaPopupMenu;
	// File Items
	protected static JMenuItem news, open, save, saveAs, properties, exit;
	// Edit Items
	protected static JMenuItem undo, copy, paste, cut, find, findNext, replace, selectAll, timeDate;
	// PopupMenu
	protected static JMenuItem popUndo, popCopy, popPaste, popCut, popSelectAll, popTimeDate;
	// Format Items
	protected static JMenuItem wordWrap, resetFont, font, fontSize, fontStyle;
	// View Items
	protected static JMenuItem skin;
	// Help Items
	protected static JMenuItem about, homePage, source;
	// textArea
	public static JTabbedPane tabs;

	//protected static UndoManager undoManager;

	public static Map<String, String> filePaths = new HashMap<>();
	public static Map<String, Boolean> saveStates = new HashMap<>();
	public static Map<String, String> savedTexts = new HashMap<>();
	protected static Map<String, UndoManager> undoManagers = new HashMap<>();
	//public static String filePath = Common.EMPTY;
	//protected static boolean saved = false;
	public static boolean lineWrap = Common.DEFAULT_WORD_WRAP;
	// Default position is (0, 0)
	public static int pointX = 0;
	public static int pointY = 0;
	//public static String savedText = Common.EMPTY;
	public static int fontNum = Common.FONT_NUM;
	public static int fontSizeNum = Common.FONT_SIZE_NUM;
	public static int fontStyleNum = Common.FONT_STYLE_NUM;
	public static String findWhat = Common.EMPTY;

	protected void setMainUIXY() {
		pointX = getMainUIX();
		pointY = getMainUIY();
	}
	
	protected int getMainUIY() {
		return (int) getLocation().getY();
	}

	protected int getMainUIX() {
		return (int) getLocation().getX();
	}

	public MainUI(String title) {
		super(title);
		setTitle(title);
	}

	public void init() {
		initMenu();
		tabs = new JTabbedPane();
		addTab(Common.UNTITLED, Common.EMPTY);
		this.add(tabs);

		this.setResizable(true);
		this.setBounds(new Rectangle(150, 100, 800, 550));
		this.setVisible(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				FileMenuUtil file = new FileMenuUtil(Common.EMPTY);
				file.exit(MainUI.this);
			}
		});
		
		setMainUIXY();
	}

	public static void addTab(String title, String text) {
		addTab(title, text, Common.EMPTY);
	}

	public static void addTab(String title, String text, String fileName) {
		filePaths.put(title, fileName);
		JTextArea contents = createTextArea(title, text);
		JScrollPane scrollPane = new JScrollPane(contents);
		tabs.add(title, scrollPane);
		tabs.setTabComponentAt(tabs.indexOfTab(title), new ButtonTabComponent(tabs));
	}

	protected void initMenu() {
		menuBar = new JMenuBar();
		menuFile();
		menuEdit();
		menuFormat();
		menuView();
		menuHelp();
		setJMenuBar(menuBar);
		initTextAreaPopupMenu();
		setDisabledMenuAtCreating(false);
	}

	protected void menuFile() {
		file = new JMenu(Common.FILE);

		news = new JMenuItem(Common.NEW);
		news.addActionListener(this);
		news.setAccelerator(KeyStroke.getKeyStroke(Common.N, InputEvent.CTRL_DOWN_MASK));
		file.add(news);

		open = new JMenuItem(Common.OPEN);
		open.addActionListener(this);
		open.setAccelerator(KeyStroke.getKeyStroke(Common.O, InputEvent.CTRL_DOWN_MASK));
		file.add(open);

		save = new JMenuItem(Common.SAVE);
		save.addActionListener(this);
		save.setAccelerator(KeyStroke.getKeyStroke(Common.S, InputEvent.CTRL_DOWN_MASK));
		file.add(save);

		saveAs = new JMenuItem(Common.SAVE_AS);
		saveAs.addActionListener(this);
		saveAs.setAccelerator(KeyStroke.getKeyStroke(Common.S, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		file.add(saveAs);

		line = new JSeparator();
		file.add(line);

		properties = new JMenuItem(Common.PROPERTIES);
		properties.addActionListener(this);
		file.add(properties);

		line = new JSeparator();
		file.add(line);

		exit = new JMenuItem(Common.EXIT);
		exit.addActionListener(this);
		file.add(exit);

		menuBar.add(file);
	}

	protected void menuEdit() {
		edit = new JMenu(Common.EDIT);

		undo = new JMenuItem(Common.UNDO);
		undo.addActionListener(this);
		undo.setAccelerator(KeyStroke.getKeyStroke(Common.Z, InputEvent.CTRL_DOWN_MASK));
		edit.add(undo);

		line = new JSeparator();
		edit.add(line);

		cut = new JMenuItem(Common.CUT);
		cut.addActionListener(this);
		cut.setAccelerator(KeyStroke.getKeyStroke(Common.X, InputEvent.CTRL_DOWN_MASK));
		edit.add(cut);
		
		copy = new JMenuItem(Common.COPY);
		copy.addActionListener(this);
		copy.setAccelerator(KeyStroke.getKeyStroke(Common.C, InputEvent.CTRL_DOWN_MASK));
		edit.add(copy);

		paste = new JMenuItem(Common.PASTE);
		paste.addActionListener(this);
		paste.setAccelerator(KeyStroke.getKeyStroke(Common.V, InputEvent.CTRL_DOWN_MASK));
		edit.add(paste);

		line = new JSeparator();
		edit.add(line);

		find = new JMenuItem(Common.FIND);
		find.addActionListener(this);
		find.setAccelerator(KeyStroke.getKeyStroke(Common.F, InputEvent.CTRL_DOWN_MASK));
		edit.add(find);

		findNext = new JMenuItem(Common.FIND_NEXT);
		findNext.addActionListener(this);
		findNext.setAccelerator(KeyStroke.getKeyStroke(Common.F, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		edit.add(findNext);

		replace = new JMenuItem(Common.REPLACE);
		replace.addActionListener(this);
		replace.setAccelerator(KeyStroke.getKeyStroke(Common.H, InputEvent.CTRL_DOWN_MASK));
		edit.add(replace);

		line = new JSeparator();
		edit.add(line);

		selectAll = new JMenuItem(Common.SELECT_ALL);
		selectAll.addActionListener(this);
		selectAll.setAccelerator(KeyStroke.getKeyStroke(Common.A, InputEvent.CTRL_DOWN_MASK));
		edit.add(selectAll);

		timeDate = new JMenuItem(Common.TIME_DATE);
		timeDate.addActionListener(this);
		timeDate.setAccelerator(KeyStroke.getKeyStroke(Common.T, InputEvent.CTRL_DOWN_MASK));
		edit.add(timeDate);
		
		menuBar.add(edit);
	}

	protected void initTextAreaPopupMenu() {
		textAreaPopupMenu = new JPopupMenu();
		
		popUndo = new JMenuItem(Common.UNDO);
		popUndo.addActionListener(this);
		textAreaPopupMenu.add(popUndo);

		line = new JSeparator();
		textAreaPopupMenu.add(line);

		popCut = new JMenuItem(Common.CUT);
		popCut.addActionListener(this);
		textAreaPopupMenu.add(popCut);
		
		popCopy = new JMenuItem(Common.COPY);
		popCopy.addActionListener(this);
		textAreaPopupMenu.add(popCopy);

		popPaste = new JMenuItem(Common.PASTE);
		popPaste.addActionListener(this);
		textAreaPopupMenu.add(popPaste);

		line = new JSeparator();
		textAreaPopupMenu.add(line);

		popSelectAll = new JMenuItem(Common.SELECT_ALL);
		popSelectAll.addActionListener(this);
		textAreaPopupMenu.add(popSelectAll);

		popTimeDate = new JMenuItem(Common.TIME_DATE);
		popTimeDate.addActionListener(this);
		textAreaPopupMenu.add(popTimeDate);
	}

	protected void menuFormat() {
		format = new JMenu(Common.FORMAT);

		wordWrap = new JMenuItem(Common.WORD_WRAP);
		wordWrap.addActionListener(this);
		wordWrap.setAccelerator(KeyStroke.getKeyStroke(Common.W, InputEvent.CTRL_DOWN_MASK));
		format.add(wordWrap);
		
		resetFont = new JMenuItem(Common.RESET_FONT);
		resetFont.addActionListener(this);
		format.add(resetFont);
		
		line = new JSeparator();
		format.add(line);

		font = new JMenuItem(Common.FONT);
		font.addActionListener(this);
		format.add(font);

		fontSize = new JMenuItem(Common.FONT_SIZE_TITLE);
		fontSize.addActionListener(this);
		format.add(fontSize);
		
		fontStyle = new JMenuItem(Common.FONT_STYLE);
		fontStyle.addActionListener(this);
		format.add(fontStyle);

		menuBar.add(format);
	}

	protected void menuView() {
		view = new JMenu(Common.VIEW);

		skin = new JMenuItem(Common.SKIN);
		skin.addActionListener(this);
		view.add(skin);

		menuBar.add(view);
	}

	protected void menuHelp() {
		help = new JMenu(Common.Help);

		viewHelp = new JMenu(Common.VIEW_HELP);
		help.add(viewHelp);
		
		homePage = new JMenuItem(Common.NOTEPAD_HOME_PAGE);
		homePage.addActionListener(this);
		viewHelp.add(homePage);

		source = new JMenuItem(Common.SOURCE);
		source.addActionListener(this);
		viewHelp.add(source);

		line = new JSeparator();
		help.add(line);

		about = new JMenuItem(Common.ABOUT_NOTEPAD);
		about.addActionListener(this);
		help.add(about);

		menuBar.add(help);
	}

	/*
	private static void initUndoManager(){
		undoManager = new UndoManager();
	}*/

	private static void setDisabledMenuAtCreating(boolean b){
		undo.setEnabled(b);
		popUndo.setEnabled(b);
		cut.setEnabled(b);
		popCut.setEnabled(b);
		copy.setEnabled(b);
		popCopy.setEnabled(b);
		find.setEnabled(b);	
		findNext.setEnabled(b);
	}

	protected static void setDisabledMenuAtSelecting(boolean b){
		cut.setEnabled(b);
		popCut.setEnabled(b);
		copy.setEnabled(b);
		popCopy.setEnabled(b);
	}

	protected JTextArea createTextArea() {
		return createTextArea(Common.UNTITLED, Common.EMPTY);
	}

	public static JTextArea createTextArea(String title, String string) {
		JTextArea result = new JTextArea(string);
		result.setLineWrap(lineWrap);
		//lineWrap = true;
		Font resultFont = new Font(FontManagerUI.FONT_TYPE, fontStyleNum, FontManagerUI.FONT_SIZE);
		result.setFont(resultFont);

		result.add(textAreaPopupMenu);

		// add Undoable edit listener
		UndoManager undoManager = new UndoManager();
		undoManagers.put(title, undoManager);
		result.getDocument().addUndoableEditListener(e -> undoManagers.get(title).addEdit(e.getEdit()));
		// add caret listener
		savedTexts.put(title, string);
		saveStates.put(title, true);
		result.addCaretListener(e -> {
				if (null != savedTexts.get(title) && null != result.getText()) {
					if (savedTexts.get(title).equals(result.getText())) {
						saveStates.replace(title, true);
					} else {
						saveStates.replace(title, false);
					}
				}
				result.setFocusable(true);
				setDisabledMenuAtCreating(true);
		});
		// add mouse motion listener
		result.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {
				isSelectedText();
			}
			
			public void mouseDragged(MouseEvent e) {
				isSelectedText();
			}
		});
		result.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					isSelectedText();
				}
			}
			
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					isSelectedText();
					textAreaPopupMenu.show(result, e.getX(), e.getY());
				}
			}
			
			public void mouseExited(MouseEvent e) {
			}
			
			public void mouseEntered(MouseEvent e) {
				
			}
			
			public void mouseClicked(MouseEvent e) {
			}
		});
		return result;
	}

	protected static void isSelectedText() {
		getSelectedTextArea().setFocusable(true);
		String selectText = getSelectedTextArea().getSelectedText();
		if(null != selectText){
			setDisabledMenuAtSelecting(true);
		}else{
			setDisabledMenuAtSelecting(false);
		}
	}

	public static void setCurrentTab(String title) {
		tabs.setSelectedIndex(tabs.indexOfTab(title));
	}

	public static String getSelectedTitle() {
		return tabs.getTitleAt(tabs.getSelectedIndex());
	}

	public static JTextArea getTextArea(String title) {
		return (JTextArea) ((JScrollPane) tabs.getComponentAt(tabs.indexOfTab(title))).getViewport().getView();
	}

	public static JTextArea getSelectedTextArea() {
		return (JTextArea) ((JScrollPane) tabs.getSelectedComponent()).getViewport().getView();
	}

	public static List<JTextArea> getTextAreas() {
		List<JTextArea> result = new ArrayList<>();
		for(int i = 0; i < tabs.getTabCount(); i++)
			result.add((JTextArea) ((JScrollPane) tabs.getComponentAt(i)).getViewport().getView());
		return result;
	}

	public void actionPerformed(ActionEvent e) {
		actionForFileItem(e);
		actionForEditItem(e);
		actionForFormatItem(e);
		actionForViewItem(e);
		actionForHelpItem(e);
	}

	protected void actionForFileItem(ActionEvent e) {
		if (e.getSource() == news) {
			FileMenuUtil.news(MainUI.this);
		} else if (e.getSource() == open) {
			FileMenuUtil file = new FileMenuUtil(Common.EMPTY);
			file.open(MainUI.this);
		} else if (e.getSource() == save) {
			FileMenuUtil.save(MainUI.this);
		} else if (e.getSource() == saveAs) {
			FileMenuUtil.saveAs(MainUI.this);
		} else if (e.getSource() == properties) {
			FileMenuUtil file = new FileMenuUtil(Common.EMPTY);
			file.readProperties(MainUI.this);
		} else if (e.getSource() == exit) {
			FileMenuUtil file = new FileMenuUtil(Common.EMPTY);
			file.exit(MainUI.this);
		}
	}

	protected void actionForEditItem(ActionEvent e) {
		if (e.getSource() == undo) {
			EditMenuUtil.undo();
		} else if (e.getSource() == popUndo) {
			EditMenuUtil.undo();
		} else if (e.getSource() == copy) {
			EditMenuUtil.copy();
		} else if (e.getSource() == popCopy) {
			EditMenuUtil.copy();
		} else if (e.getSource() == paste) {
			EditMenuUtil.paste();
		} else if (e.getSource() == popPaste) {
			EditMenuUtil.paste();
		} else if (e.getSource() == cut) {
			EditMenuUtil.cut();
		} else if (e.getSource() == popCut) {
			EditMenuUtil.cut();
		} else if (e.getSource() == find) {
			setMainUIXY();
			EditMenuUtil edit = new EditMenuUtil(Common.EMPTY);
			edit.find();
		} else if (e.getSource() == findNext) {
			EditMenuUtil edit = new EditMenuUtil(Common.EMPTY);
			edit.findNext();
		} else if (e.getSource() == replace) {
			setMainUIXY();
			EditMenuUtil edit = new EditMenuUtil(Common.EMPTY);
			edit.replace();
		} else if (e.getSource() == selectAll) {
			EditMenuUtil.selectAll();
		} else if (e.getSource() == popSelectAll) {
			EditMenuUtil.selectAll();
		} else if (e.getSource() == timeDate) {
			EditMenuUtil.timeDate();
		}else if (e.getSource() == popTimeDate) {
			EditMenuUtil.timeDate();
		}
	}

	protected void actionForFormatItem(ActionEvent e) {
		if (e.getSource() == wordWrap) {
			FormatMenuUtil.wordWrap();
		} else if(e.getSource() == resetFont){
			FormatMenuUtil format = new FormatMenuUtil(Common.EMPTY);
			format.resetFont(MainUI.this);
		}else if (e.getSource() == font) {
			setMainUIXY();
			FormatMenuUtil format = new FormatMenuUtil(Common.EMPTY);
			format.font(MainUI.this);
		} else if (e.getSource() == fontSize) {
			setMainUIXY();
			FormatMenuUtil format = new FormatMenuUtil(Common.EMPTY);
			format.fontSize(MainUI.this);
		}else if(e.getSource() == fontStyle){
			setMainUIXY();
			FormatMenuUtil format = new FormatMenuUtil(Common.EMPTY);
			format.fontStyle(MainUI.this);
		}
	}

	protected void actionForViewItem(ActionEvent e) {
		if (e.getSource() == skin) {
			setMainUIXY();
			ViewMenuUtil view = new ViewMenuUtil(Common.EMPTY);
			view.skin(MainUI.this);
		}
	}

	protected void actionForHelpItem(ActionEvent e) {
		if (e.getSource() == homePage) {
			//log.debug(Common.NOTEPAD_HOME_PAGE);
			NotepadUtil.accessURL(Common.NOTEPAD_PUBLISHED_PAGE);
		}else if(e.getSource() == source){
			//log.debug(Common.SOURCE_CODE);
			NotepadUtil.accessURL(Common.NOTEPAD_PUBLISHED_PAGE);
		}else if (e.getSource() == about) {
			setMainUIXY();
			HelpMenuUtil help = new HelpMenuUtil(Common.EMPTY);
			help.about(MainUI.this);
		}
	}

	public boolean allSaved() {
		return !saveStates.values().contains(false);
	}

	public boolean isSaved(String title) {
		return saveStates.get(title);
	}

	public static void setSaved(String title, boolean newVal) {
		saveStates.replace(title, newVal);
	}

	public static String doctorName(String name) {
		List<String> tabNames = new ArrayList<>();
		for(int i = 0; i < tabs.getTabCount(); i++)
			tabNames.add(tabs.getTitleAt(i));
		if(!tabNames.contains(name))
			return name;
		int count = 1;
		while(tabNames.contains(name + "(" + count + ")"))
			count++;
		return name + "(" + count + ")";
	}

	public static void removeTab(String title) {
		if(!MainUI.saveStates.get(title))
			if(FileMenuUtil.confirmSave(Client.MAIN_UI, title))
				return;
		tabs.remove(tabs.indexOfTab(title));
		MainUI.saveStates.remove(title);
		MainUI.savedTexts.remove(title);
		MainUI.filePaths.remove(title);
		MainUI.undoManagers.remove(title);
		if(tabs.getTabCount() == 0)
			addTab(Common.UNTITLED, Common.EMPTY);
	}
}
