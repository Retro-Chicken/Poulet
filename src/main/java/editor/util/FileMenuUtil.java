/**
 * 
 */
package editor.util;

import editor.common.Common;
import editor.ui.MainUI;

import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Hongten
 * @created Nov 19, 2014
 */
public class FileMenuUtil extends MainUI {

	private static final long serialVersionUID = 1L;

	//static Logger log = Logger.getLogger(FileMenuUtil.class);

	public FileMenuUtil(String title) {
		super(title);
	}

	/**
	 * Create a new Notepad. <br>
	 * 1. If the content of the Notepad is empty, then, create a new Notepad is
	 * itself.<br>
	 * 2. If the content of the Notepad is NOT empty, then, we want to create a
	 * new Notepad:<br>
	 * 2.1. If the Notepad is saved, then, create a new Notepad and let the
	 * parent <code>setVisible(false)</code><br>
	 * 2.2. If the Notepad is NOT saved<br>
	 * 2.2.1. If the user want to save the content, "YES", <code>save()</code>,
	 * go to step 2.1<br>
	 * 2.2.2. If the user do NOT want to save the content, "NO", clean the
	 * textArea, go to step 1<br>
	 * 2.2.3. If the user select the "Cancel" option, nothing to do and return
	 * to textArea.<br>
	 * 
	 * @param mainUI
	 */
	public static void news(MainUI mainUI) {
		//log.debug(Common.NEW);
		/*
		if (!Common.EMPTY.equals(filePath)) {
			if (savedText.equals(getSelectedTextArea().getText())) {
				createMainUI(mainUI);
			} else {
				confirmSave(mainUI);
			}
		} else {
			if (Common.EMPTY.equals(getSelectedTextArea().getText())) {
				createMainUI(mainUI);
			} else {
				confirmSave(mainUI);
			}
		}*/
		String name = doctorName(Common.UNTITLED);
		addTab(name, Common.EMPTY);
		setCurrentTab(name);
	}

	/**
	 * @param mainUI
	 */
	private static boolean confirmSave(MainUI mainUI) {
		return confirmSave(mainUI, getSelectedTitle());
	}

	/**
	 * @param mainUI
	 * @param title
	 * @return Returns true if cancelled, false otherwise.
	 */
	public static boolean confirmSave(MainUI mainUI, String title) {
		int option = JOptionPane.showConfirmDialog(mainUI, Common.DO_YOU_WANT_TO_SAVE_CHANGES, Common.NOTEPAD, JOptionPane.YES_NO_CANCEL_OPTION);
		if (option == JOptionPane.YES_OPTION) {
			save(mainUI, title);
			return false;
			//createMainUI(mainUI);
		} else if (option == JOptionPane.NO_OPTION) {
			return false;
			//createMainUI(mainUI);
		} else if (option == JOptionPane.CANCEL_OPTION) {
			getSelectedTextArea().setFocusable(true);
			return true;
		}
		return true;
	}

	/**
	 * Open a text file:<br>
	 * 1. If the textArea is empty, then, click the "Open" menu to open a text
	 * file.<br>
	 * 2. If the textArea is NOT empty, then, we want to open a text file:<br>
	 * 2.1. If the content of textArea was saved, then we click the "Open" menu
	 * to open a text file.<br>
	 * 2.2. If the content of textArea was NOT saved. There is a dialog display.<br>
	 * 2.2.1. Selecting "Yes" to save content, and open a text file.<br>
	 * 2.2.2. Selecting "No", then do NOT save the content, and open a text
	 * file.<br>
	 * 2.2.3. Selecting "Cancel", nothing to do and return to textArea.<br>
	 * 
	 * @param mainUI
	 * @see FileMenuUtil#openOperation(MainUI)
	 */
	public void open(MainUI mainUI) {
		//log.debug(Common.OPEN);
		/*
		if (!Common.EMPTY.equals(filePath)) {
			if (savedText.equals(getSelectedTextArea().getText())) {
				openOperation(mainUI);
			} else {
				confirmOpen(mainUI);
			}
		} else {
			if (Common.EMPTY.equals(getSelectedTextArea().getText())) {
				openOperation(mainUI);
			} else {
				confirmOpen(mainUI);
			}
		}*/
		openOperation(mainUI);
	}

	private void confirmOpen(MainUI mainUI) {
		int option = JOptionPane.showConfirmDialog(FileMenuUtil.this, Common.DO_YOU_WANT_TO_SAVE_CHANGES, Common.CONFIRM_EXIT, JOptionPane.YES_NO_CANCEL_OPTION);
		if (option == JOptionPane.YES_OPTION) {
			save(mainUI);
			openOperation(mainUI);
		} else if (option == JOptionPane.NO_OPTION) {
			openOperation(mainUI);
		} else if (option == JOptionPane.CANCEL_OPTION) {
			getSelectedTextArea().setFocusable(true);
		}
	}

	/**
	 * The operation of the open<br>
	 * When the user want to open a DEFAULT_FILTER_EXT file, this method will be called.<br>
	 * 
	 * @param mainUI
	 * @see FileMenuUtil#open(MainUI)
	 */
	private static void openOperation(MainUI mainUI) {
		String path;
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter;
		filter = new FileNameExtensionFilter(Common.DEFAULT_FILTER, Common.DEFAULT_FILTER_EXT);
		chooser.setFileFilter(filter);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(Common.OPEN);
		int ret = chooser.showOpenDialog(null);
		if (ret == JFileChooser.APPROVE_OPTION) {
			path = chooser.getSelectedFile().getAbsolutePath();

			if(filePaths.values().contains(path)) {
				String exisitingName = getSelectedTitle();
				for(String key : filePaths.keySet()) {
					if (filePaths.get(key).equals(path)) {
						exisitingName = key;
						break;
					}
				}
				setCurrentTab(exisitingName);
				return;
			}

			String name = doctorName(chooser.getSelectedFile().getName());
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), Common.GB2312));
				StringBuffer buffer = new StringBuffer();
				String line;
				while ((line = reader.readLine()) != null) {
					buffer.append(line).append(Common.NEW_LINE);
				}
				reader.close();
				addTab(name, String.valueOf(buffer), path);
				if(filePaths.get(getSelectedTitle()).equals(Common.EMPTY) && saveStates.get(getSelectedTitle()))
					removeTab(getSelectedTitle());
				setCurrentTab(name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Saving a DEFAULT_FILTER_EXT file.<br>
	 * 1. If the user want to create a new DEFAULT_FILTER_EXT file, and type the content(empty
	 * is allowed) to save. In this case, a dialog will display.<br>
	 * 2. If the user want to save a existing file. then call
	 * <code>save()</code> method to save content.<br>
	 * 3. A existing file with some changes, then the user want to save it. The
	 * operation as same as step 2.<br>
	 * 
	 * @param mainUI
	 */
	public static void save(MainUI mainUI, String title) {
        //log.debug(Common.SAVE);
        String filePath = filePaths.get(title);
        String text = getTextArea(title).getText();
        try {
            if (null != filePath && !Common.EMPTY.equals(filePath)) {
                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filePath));
                out.write(text);
                out.close();
                mainUI.setSaved(title, true);
                savedTexts.replace(title, text);
            } else {
                FileDialog fileDialog = new FileDialog(mainUI, Common.SAVE, FileDialog.SAVE);
                fileDialog.setVisible(true);
                if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
                    String fileName = fileDialog.getFile();
                    if (!Common.DEFAULT_FILTER_EXT.equalsIgnoreCase(NotepadUtil.getPostfix(fileName))) {
                        fileName = fileName + Common.POINT + Common.DEFAULT_FILTER_EXT;
                    }
                    String path = fileDialog.getDirectory() + fileName;
                    OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(path));
                    out.write(text);
                    out.close();
                    mainUI.setTitle(fileName + Common.NOTEPAD_NOTEPAD);
                    filePaths.replace(title, path);
                    mainUI.setSaved(title, true);
                    savedTexts.replace(title, text);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //log.debug(e);
        }
    }

	public static void save(MainUI mainUI) {
        save(mainUI, getSelectedTitle());
	}

	public static void saveAs(MainUI mainUI) {
		//log.debug(Common.SAVE_AS);
        String title = getSelectedTitle();
		String path = filePaths.get(title);
		filePaths.replace(title, Common.EMPTY);
		save(mainUI);
		if (Common.EMPTY.equals(filePaths.get(title))) {
            filePaths.replace(title, path);
		}
	}

	public void readProperties(MainUI mainUI) {
		//log.debug(Common.PROPERTIES);
        String title = getSelectedTitle();
		if (!Common.EMPTY.equals(filePaths.get(title)) && mainUI.isSaved(title)) {
			File file = new File(filePaths.get(title));
			JOptionPane.showMessageDialog(FileMenuUtil.this, NotepadUtil.fileProperties(file), Common.NOTEPAD, JOptionPane.INFORMATION_MESSAGE);
		} else {
			confirmSave(mainUI);
		}
	}

	public void exit(MainUI mainUI) {
		//log.debug(Common.EXIT);
        if(allSaved())
            NotepadUtil.exit();
        else
            confirmExit(mainUI);
        /*
		if (!Common.EMPTY.equals(filePath)) {
			if (savedText.equals(getSelectedTextArea().getText())) {
				NotepadUtil.exit();
			} else {
				confirmExit(mainUI);
			}
		} else {
			if (Common.EMPTY.equals(getSelectedTextArea().getText())) {
				NotepadUtil.exit();
			} else {
				confirmExit(mainUI);
			}
		}*/
	}

	private void confirmExit(MainUI mainUI) {
		int option = JOptionPane.showConfirmDialog(FileMenuUtil.this, Common.SAVE_ALL_CHANGES, Common.CONFIRM_EXIT, JOptionPane.YES_NO_CANCEL_OPTION);
		if (option == JOptionPane.YES_OPTION) {
		    for(int i = 0; i < tabs.getTabCount(); i++)
			    save(mainUI, tabs.getTitleAt(i));
			NotepadUtil.exit();
		} else if (option == JOptionPane.NO_OPTION) {
			NotepadUtil.exit();
		} else if (option == JOptionPane.CANCEL_OPTION) {
			getSelectedTextArea().setFocusable(true);
		}
	}

	/*
	private static void createMainUI(MainUI mainUI) {
		mainUI.setTitle(Common.UNTITLED + Common.NOTEPAD_NOTEPAD);
		getSelectedTextArea().setText(Common.EMPTY);
		filePath = Common.EMPTY;
		savedText = Common.EMPTY;
		mainUI.setSaved(false);
	}*/

}
