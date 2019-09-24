/**
 * 
 */
package editor.util;

import editor.client.Client;
import editor.common.Common;
import editor.ui.MainUI;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Hongten
 * @created Nov 19, 2014
 */
public class FileMenuUtil {
	private final MainUI parent;

	public FileMenuUtil(MainUI parent) {
		this.parent = parent;
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
	 */
	public void newFile() {
		String name = parent.doctorName(Common.UNTITLED);
		parent.addTab(name, Common.EMPTY);
		parent.setCurrentTab(name);
	}

	private boolean confirmSave() {
		return confirmSave(parent.getSelectedTitle());
	}

	/**
	 * @param title
	 * @return Returns true if cancelled, false otherwise.
	 */
	public boolean confirmSave(String title) {
		int option = JOptionPane.showConfirmDialog(parent, Common.DO_YOU_WANT_TO_SAVE_CHANGES, Common.NOTEPAD, JOptionPane.YES_NO_CANCEL_OPTION);
		if (option == JOptionPane.YES_OPTION) {
			save(title);
			return false;
		} else if (option == JOptionPane.NO_OPTION) {
			return false;
		} else if (option == JOptionPane.CANCEL_OPTION) {
			parent.getSelectedTextArea().setFocusable(true);
			return true;
		}
		return true;
	}

	/**
	 * The operation of the open<br>
	 * When the user want to open a DEFAULT_FILTER_EXT file, this method will be called.<br>
	 */
	public void open() {
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

			if(parent.filePaths.values().contains(path)) {
				String exisitingName = parent.getSelectedTitle();
				for(String key : parent.filePaths.keySet()) {
					if (parent.filePaths.get(key).equals(path)) {
						exisitingName = key;
						break;
					}
				}
				parent.setCurrentTab(exisitingName);
				return;
			}

			String name = parent.doctorName(chooser.getSelectedFile().getName());
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), Common.GB2312));
				StringBuffer buffer = new StringBuffer();
				String line;
				while ((line = reader.readLine()) != null) {
					buffer.append(line).append(Common.NEW_LINE);
				}
				reader.close();
				parent.addTab(name, String.valueOf(buffer), path);
				if(parent.filePaths.get(parent.getSelectedTitle()).equals(Common.EMPTY) && parent.saveStates.get(parent.getSelectedTitle()))
					parent.removeTab(parent.getSelectedTitle());
				parent.setCurrentTab(name);
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
	 * @param title
	 */
	public void save(String title) {
        String filePath = parent.filePaths.get(title);
        String text = parent.getTextArea(title).getText();
        try {
            if (null != filePath && !Common.EMPTY.equals(filePath)) {
                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filePath));
                out.write(text);
                out.close();
                parent.setSaved(title, true);
				parent.savedTexts.replace(title, text);
            } else {
                FileDialog fileDialog = new FileDialog(parent, Common.SAVE, FileDialog.SAVE);
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
                    parent.setTitle(fileName + Common.NOTEPAD_NOTEPAD);
					parent.filePaths.replace(title, path);
                    parent.setSaved(title, true);
					parent.savedTexts.replace(title, text);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public void save() {
        save(parent.getSelectedTitle());
	}

	public void saveAs() {
        String title = parent.getSelectedTitle();
		String path = parent.filePaths.get(title);
		parent.filePaths.replace(title, Common.EMPTY);
		save();
		if (Common.EMPTY.equals(parent.filePaths.get(title))) {
			parent.filePaths.replace(title, path);
		}
	}

	public void readProperties() {
        String title = parent.getSelectedTitle();
		if (!Common.EMPTY.equals(parent.filePaths.get(title)) && parent.isSaved(title)) {
			File file = new File(parent.filePaths.get(title));
			JOptionPane.showMessageDialog(Client.MAIN_UI, NotepadUtil.fileProperties(file), Common.NOTEPAD, JOptionPane.INFORMATION_MESSAGE);
		} else {
			confirmSave();
		}
	}

	public void exit() {
        if(parent.allSaved())
            NotepadUtil.exit();
        else
            confirmExit();
	}

	private void confirmExit() {
		int option = JOptionPane.showConfirmDialog(Client.MAIN_UI, Common.SAVE_ALL_CHANGES, Common.CONFIRM_EXIT, JOptionPane.YES_NO_CANCEL_OPTION);
		if (option == JOptionPane.YES_OPTION) {
		    for(int i = 0; i < parent.tabs.getTabCount(); i++)
			    save(parent.tabs.getTitleAt(i));
			NotepadUtil.exit();
		} else if (option == JOptionPane.NO_OPTION) {
			NotepadUtil.exit();
		} else if (option == JOptionPane.CANCEL_OPTION) {
			parent.getSelectedTextArea().setFocusable(true);
		}
	}

}
