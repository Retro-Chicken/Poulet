/**
 * 
 */
package editor.util;

import editor.common.Common;
import editor.ui.FindManagerUI;
import editor.ui.MainUI;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * @author Hongten
 * @created Nov 19, 2014
 */
public class EditMenuUtil {
	private final MainUI parent;
	
	public EditMenuUtil(MainUI parent) {
		this.parent = parent;
	}

	public void undo() {
		String title = parent.getSelectedTitle();
		if(parent.undoManagers.get(title).canUndo()){
			parent.undoManagers.get(title).undo();
		}
	}

	public void copy() {
		parent.getSelectedTextArea().copy();
	}

	public void paste() {
		parent.getSelectedTextArea().paste();
	}

	public void cut() {
		parent.getSelectedTextArea().cut();
	}

	/**
	 * The directory : isForward(true : Forward and false : Backward)<br>
	 * The Case Sensitive : isCaseSensitive(true : Case Sensitive and false : Not Case Sensitive)</br>
	 */
	public void findNext(Component notificationParent) {
		String findWhat = parent.findWhat;
        Pattern findPattern = Pattern.compile(parent.findWhat, FindManagerUI.isCaseSensitive ? Pattern.LITERAL : Pattern.CASE_INSENSITIVE);
        JTextArea textArea = parent.getSelectedTextArea();
        String content = textArea.getText();
		if(findWhat.isEmpty()) {
			JOptionPane.showMessageDialog(parent, Common.WHAT_DO_YOU_WANT_TO_FIND, Common.NOTEPAD, JOptionPane.INFORMATION_MESSAGE);
		} else if(!findPattern.matcher(content).find()) {
			canNotFindKeyWord(notificationParent);
		} else {
			int start = FindManagerUI.isForward ? 0 : content.length() - findWhat.length();
			if(textArea.getSelectedText() != null && findPattern.matcher(textArea.getSelectedText()).matches())
                start = textArea.getSelectionStart() + (FindManagerUI.isForward ? 1 : -1);
			start -= FindManagerUI.isForward ? 1 : -1;
            int end;
			do {
                start += FindManagerUI.isForward ? 1 : -1;
                if(start < 0) start += content.length();
                start %= content.length();
                end = start + findWhat.length();
                if(end > content.length()) {
                    if(FindManagerUI.isForward) {
                        start = 0;
                        end = findWhat.length();
                    } else {
                        end = content.length();
                        start = end - findWhat.length();
                    }
                }
            } while(!findPattern.matcher(content.substring(start, end)).matches());
            textArea.setSelectionStart(start);
            textArea.setSelectionEnd(end);
		}
	}

    public void findNext() {
        findNext(parent);
    }
	
	private void canNotFindKeyWord(Component notificationParent) {
		JOptionPane.showMessageDialog(notificationParent, Common.CAN_NOT_FIND + parent.findWhat, Common.NOTEPAD, JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	/**
	 * Default direction is Forward. The <code>replace</code> method can NOT be called when <br>
	 * <code>null == getSelectedTextArea().getSelectedText();</code>
	 */
	public void replace(String replace, Component notificationParent){
	    JTextArea textArea = parent.getSelectedTextArea();
        Pattern findPattern = Pattern.compile(parent.findWhat, FindManagerUI.isCaseSensitive ? Pattern.LITERAL : Pattern.CASE_INSENSITIVE);
        if(!findPattern.matcher(textArea.getText()).find())
            return;
        if(!(textArea.getSelectedText() != null && findPattern.matcher(textArea.getSelectedText()).matches()))
            findNext(notificationParent);
		if (textArea.getSelectedText() != null)
			textArea.replaceRange(replace, parent.getSelectedTextArea().getSelectionStart(), parent.getSelectedTextArea().getSelectionEnd());
	}

	public void replace(String replace) {
	    replace(replace, parent);
    }

	/**
	 * When user want to call Replace_All method, the application will replace all with case sensitive.<br>
	 * A information window will display after replacing all words.<br>Finally, the application will set <br>
	 * <code>ReplaceManagerUI.replaceCount = 0;</code>
	 */
	public void replaceAll(String replace, Component notificationParent) {
	    int replaceCount = 0;
        JTextArea textArea = parent.getSelectedTextArea();
		String result = textArea.getText();
        Pattern findPattern = Pattern.compile(parent.findWhat, FindManagerUI.isCaseSensitive ? Pattern.LITERAL : Pattern.CASE_INSENSITIVE);
		if(!findPattern.matcher(result).find())
		    return;
		while(findPattern.matcher(result).find()) {
            result = result.replaceFirst(findPattern.pattern(), replace);
            replaceCount++;
        }
		textArea.setText(result);
		JOptionPane.showMessageDialog(notificationParent, replaceCount + Common.MATCHES_REPLACED, Common.NOTEPAD, JOptionPane.INFORMATION_MESSAGE);
	}

	public void replaceAll(String replace) {
	    replaceAll(replace, parent);
    }

	public void selectAll() {
		parent.getSelectedTextArea().selectAll();
	}

	public void timeDate() {
		parent.getSelectedTextArea().replaceRange(NotepadUtil.getTimeDate(), parent.getSelectedTextArea().getSelectionStart(), parent.getSelectedTextArea().getSelectionEnd());
	}

}
