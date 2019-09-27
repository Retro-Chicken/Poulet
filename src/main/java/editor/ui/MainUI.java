/**
 * 
 */
package editor.ui;

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

	protected JMenuBar menuBar;
	protected JSeparator line;
	// Menus
	protected JMenu file, edit, format, view, help, viewHelp;
	// PopupMenu
	protected JPopupMenu textAreaPopupMenu;
	// File Items
	protected JMenuItem news, open, save, saveAs, properties, exit;
	protected FileMenuUtil fileUtil;
	// Edit Items
	protected JMenuItem undo, copy, paste, cut, find, findNext, replace, selectAll, timeDate;
	protected FindManagerUI findManagerUI;
	protected ReplaceManagerUI replaceManagerUI;
	protected EditMenuUtil editUtil;
	// PopupMenu
	protected JMenuItem popUndo, popCopy, popPaste, popCut, popSelectAll, popTimeDate;
	// Format Items
	protected JMenuItem wordWrap, tabFormat, resetFont, font, fontSize, fontStyle;
	protected TabsUI tabsUI;
	protected FontManagerUI fontManagerUI;
	protected FontSizeManagerUI fontSizeManagerUI;
	protected FontStyleManagerUI fontStyleManagerUI;
	protected FormatMenuUtil formatUtil;
	// View Items
	protected JMenuItem skin, toggleLineNumber;
	protected SkinManagerUI skinManagerUI;
	// Help Items
	protected JMenuItem about, homePage, source;
	protected AboutUI aboutUI;
	// textArea
	public JTabbedPane tabs;

	// File specific data indexed by tab name
	public Map<String, String> filePaths = new HashMap<>();
	public Map<String, Boolean> saveStates = new HashMap<>();
	public Map<String, String> savedTexts = new HashMap<>();
	public Map<String, UndoManager> undoManagers = new HashMap<>();
	protected Map<String, TextLineNumber> lineNumberComponents = new HashMap<>();

	public boolean lineWrap = Common.DEFAULT_WORD_WRAP;
	public boolean lineNumbers = Common.DEFAULT_LINE_NUMBERS;

	// Default position is (0, 0)
	public int pointX = 0;
	public int pointY = 0;

	// Font info
	public int fontNum = Common.FONT_NUM;
	public int fontSizeNum = Common.FONT_SIZE_NUM;
	public int fontStyleNum = Common.FONT_STYLE_NUM;
	public String findWhat = Common.EMPTY;

	// Tab info
	public boolean useTabs = false;
	public int tabSpaces = 4;

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
		initTextAreaPopupMenu();
		tabs = new JTabbedPane();
		addTab(Common.UNTITLED, Common.EMPTY);
		initMenu();
		this.add(tabs);

		this.setResizable(true);
		this.setBounds(new Rectangle(150, 100, 800, 550));
		this.setVisible(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				fileUtil.exit();
			}
		});
		
		setMainUIXY();
	}

	public void addTab(String title, String text) {
		addTab(title, text, Common.EMPTY);
	}

	public void addTab(String title, String text, String fileName) {
		filePaths.put(title, fileName);
		JTextArea contents = createTextArea(title, text);
		JScrollPane scrollPane = new JScrollPane(contents);
		TextLineNumber tln = new TextLineNumber(contents);
		if(lineNumbers)
			scrollPane.setRowHeaderView(tln);
		lineNumberComponents.put(title, tln);
		tabs.add(title, scrollPane);
		tabs.setTabComponentAt(tabs.indexOfTab(title), new ButtonTabComponent(tabs, this));
	}

	protected void initMenu() {
		menuBar = new JMenuBar();
		menuFile();
		menuEdit();
		menuFormat();
		menuView();
		menuHelp();
		setJMenuBar(menuBar);
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

		fileUtil = new FileMenuUtil(this);

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

		editUtil = new EditMenuUtil(this);
		findManagerUI = new FindManagerUI("Find", editUtil, this);
		replaceManagerUI = new ReplaceManagerUI(Common.REPLACE, editUtil,this);
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

		tabFormat = new JMenuItem("Tabbing...");
		tabFormat.addActionListener(this);
		format.add(tabFormat);
		
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

		formatUtil = new FormatMenuUtil(this);

		tabsUI = new TabsUI("Tabbing", this);
		fontManagerUI = new FontManagerUI(Common.FONT, this);
		fontSizeManagerUI = new FontSizeManagerUI(Common.FONT_SIZE_TITLE, this);
		fontStyleManagerUI = new FontStyleManagerUI(Common.FONT_STYLE, this);
	}

	protected void menuView() {
		view = new JMenu(Common.VIEW);

		skin = new JMenuItem(Common.SKIN);
		skin.addActionListener(this);
		view.add(skin);

		toggleLineNumber = new JMenuItem(Common.LINE_NUMBERS);
		toggleLineNumber.addActionListener(this);
		view.add(toggleLineNumber);

		aboutUI = new AboutUI(Common.ABOUT_NOTEPAD, this);

		menuBar.add(view);

		skinManagerUI = new SkinManagerUI(Common.SKIN, this);
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

	private void setDisabledMenuAtCreating(boolean b){
		undo.setEnabled(b);
		popUndo.setEnabled(b);
		cut.setEnabled(b);
		popCut.setEnabled(b);
		copy.setEnabled(b);
		popCopy.setEnabled(b);
		find.setEnabled(b);	
		findNext.setEnabled(b);
	}

	protected void setDisabledMenuAtSelecting(boolean b){
		cut.setEnabled(b);
		popCut.setEnabled(b);
		copy.setEnabled(b);
		popCopy.setEnabled(b);
	}

	protected JTextArea createTextArea() {
		return createTextArea(Common.UNTITLED, Common.EMPTY);
	}

	public JTextArea createTextArea(String title, String string) {
		JTextArea result = new JTextArea(string);
		result.setLineWrap(lineWrap);
		result.setTabSize(tabSpaces);
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

	protected void isSelectedText() {
		getSelectedTextArea().setFocusable(true);
		String selectText = getSelectedTextArea().getSelectedText();
		if(null != selectText){
			setDisabledMenuAtSelecting(true);
		}else{
			setDisabledMenuAtSelecting(false);
		}
	}

	public void setCurrentTab(String title) {
		tabs.setSelectedIndex(tabs.indexOfTab(title));
	}

	public String getSelectedTitle() {
		return tabs.getTitleAt(tabs.getSelectedIndex());
	}

	public JTextArea getTextArea(String title) {
		return (JTextArea) ((JScrollPane) tabs.getComponentAt(tabs.indexOfTab(title))).getViewport().getView();
	}

	public JTextArea getSelectedTextArea() {
		return (JTextArea) ((JScrollPane) tabs.getSelectedComponent()).getViewport().getView();
	}

	public List<JTextArea> getTextAreas() {
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
			fileUtil.newFile();
		} else if (e.getSource() == open) {
			fileUtil.open();
		} else if (e.getSource() == save) {
			fileUtil.save();
		} else if (e.getSource() == saveAs) {
			fileUtil.saveAs();
		} else if (e.getSource() == properties) {
			fileUtil.readProperties();
		} else if (e.getSource() == exit) {
			fileUtil.exit();
		}
	}

	protected void actionForEditItem(ActionEvent e) {
		if (e.getSource() == undo) {
			editUtil.undo();
		} else if (e.getSource() == popUndo) {
			editUtil.undo();
		} else if (e.getSource() == copy) {
			editUtil.copy();
		} else if (e.getSource() == popCopy) {
			editUtil.copy();
		} else if (e.getSource() == paste) {
			editUtil.paste();
		} else if (e.getSource() == popPaste) {
			editUtil.paste();
		} else if (e.getSource() == cut) {
			editUtil.cut();
		} else if (e.getSource() == popCut) {
			editUtil.cut();
		} else if (e.getSource() == find) {
			findManagerUI.display();
		} else if (e.getSource() == findNext) {
			editUtil.findNext();
		} else if (e.getSource() == replace) {
			replaceManagerUI.display();
		} else if (e.getSource() == selectAll) {
			editUtil.selectAll();
		} else if (e.getSource() == popSelectAll) {
			editUtil.selectAll();
		} else if (e.getSource() == timeDate) {
			editUtil.timeDate();
		}else if (e.getSource() == popTimeDate) {
			editUtil.timeDate();
		}
	}

	protected void actionForFormatItem(ActionEvent e) {
		if (e.getSource() == wordWrap) {
			formatUtil.wordWrap();
		} else if(e.getSource() == tabFormat) {
			tabsUI.display();
		} else if(e.getSource() == resetFont){
			formatUtil.resetFont();
		}else if (e.getSource() == font) {
			fontManagerUI.display();
		} else if (e.getSource() == fontSize) {
			fontSizeManagerUI.display();
		}else if(e.getSource() == fontStyle){
			fontStyleManagerUI.display();
		}
	}

	protected void actionForViewItem(ActionEvent e) {
		if (e.getSource() == skin) {
			skinManagerUI.display();
		} else if(e.getSource() == toggleLineNumber) {
			lineNumbers = !lineNumbers;
			for(String title : lineNumberComponents.keySet()) {
				((JScrollPane) tabs.getComponentAt(tabs.indexOfTab(title))).setRowHeaderView(
						lineNumbers ? lineNumberComponents.get(title) : null
				);
			}
		}
	}

	protected void actionForHelpItem(ActionEvent e) {
		if (e.getSource() == homePage) {
			NotepadUtil.accessURL(Common.NOTEPAD_PUBLISHED_PAGE);
		}else if(e.getSource() == source){
			NotepadUtil.accessURL(Common.NOTEPAD_PUBLISHED_PAGE);
		}else if (e.getSource() == about) {
			aboutUI.display();
		}
	}

	public boolean allSaved() {
		return !saveStates.values().contains(false);
	}

	public boolean isSaved(String title) {
		return saveStates.get(title);
	}

	public void setSaved(String title, boolean newVal) {
		saveStates.replace(title, newVal);
	}

	public String doctorName(String name) {
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

	public void removeTab(String title) {
		if(!saveStates.get(title))
			if(fileUtil.confirmSave(title))
				return;
		tabs.remove(tabs.indexOfTab(title));
		saveStates.remove(title);
		savedTexts.remove(title);
		filePaths.remove(title);
		undoManagers.remove(title);
		lineNumberComponents.remove(title);
		if(tabs.getTabCount() == 0)
			addTab(Common.UNTITLED, Common.EMPTY);
	}
}
