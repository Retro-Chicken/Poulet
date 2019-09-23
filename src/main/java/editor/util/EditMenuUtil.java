/**
 * 
 */
package editor.util;

import editor.common.Common;
import editor.ui.FindManagerUI;
import editor.ui.MainUI;
import editor.ui.ReplaceManagerUI;

import javax.swing.JOptionPane;

/**
 * @author Hongten
 * @created Nov 19, 2014
 */
public class EditMenuUtil extends MainUI {
	
	private static final long serialVersionUID = 1L;
	
	//static Logger log = Logger.getLogger(EditMenuUtil.class);

	private static FindManagerUI findManagerUI;
	private static ReplaceManagerUI replaceeManagerUI;
	
	public EditMenuUtil(String title) {
		super(title);
	}

	public static void undo() {
		//log.debug(Common.UNDO);
		String title = getSelectedTitle();
		if(undoManagers.get(title).canUndo()){
			undoManagers.get(title).undo();
		}
	}

	public static void copy() {
		//log.debug(Common.COPY);
		getSelectedTextArea().copy();
	}

	public static void paste() {
		//log.debug(Common.PASTE);
		getSelectedTextArea().paste();
	}

	public static void cut() {
		//log.debug(Common.CUT);
		getSelectedTextArea().cut();
	}

	/**
	 * Showing the <code>FindManagerUI</code> window.
	 */
	public void find() {
		//log.debug(Common.FIND);
		if (null == findManagerUI) {
			findManagerUI = new FindManagerUI(Common.FIND);
			findManagerUI.setEditMenuUtil(EditMenuUtil.this);
		} else {
			findManagerUI.setVisible(true);
			findManagerUI.setFocusable(true);
		}
	}

	/**
	 * The directory : isForward(true : Forward and false : Backward)<br>
	 * The Case Sensitive : isCaseSensitive(true : Case Sensitive and false : Not Case Sensitive)</br>
	 */
	public void findNext() {
		//log.debug(Common.FIND_NEXT);
		if (Common.EMPTY.equals(findWhat)) {
			JOptionPane.showMessageDialog(EditMenuUtil.this, Common.WHAT_DO_YOU_WANT_TO_FIND, Common.NOTEPAD, JOptionPane.INFORMATION_MESSAGE);
		} else if (findWhat.length() > getSelectedTextArea().getText().length()) {
			canNotFindKeyWord();
		} else {
			String content = getSelectedTextArea().getText();
			String temp = Common.EMPTY;
			int position = getSelectedTextArea().getSelectionEnd() - findWhat.length() + 1;
			if (FindManagerUI.isForward) {
				if(position > content.length() - findWhat.length()){
					canNotFindKeyWordOperation(content.length(), content.length());
				}
				for (; position <= content.length() - findWhat.length(); position++) {
					temp = content.substring(position, position + findWhat.length());
					if (FindManagerUI.isCaseSensitive) {
						if (temp.equals(findWhat)) {
							setTextAreaSelection(position, position + findWhat.length());
							break;
						} else if (position >= content.length() - findWhat.length()) {
							canNotFindKeyWordOperation(content.length(), content.length());
							break;
						}
					} else {
						if (temp.equalsIgnoreCase(findWhat)) {
							setTextAreaSelection(position, position + findWhat.length());
							break;
						} else if (position >= content.length() - findWhat.length()) {
							canNotFindKeyWordOperation(content.length(), content.length());
							break;
						}
					}
				}
			} else {// Backward
				if(null != getSelectedTextArea().getSelectedText() && !Common.EMPTY.equals(getSelectedTextArea().getSelectedText().trim())){
					position = getSelectedTextArea().getSelectionStart();
				}
				if(position < findWhat.length()){
					canNotFindKeyWordOperation(0, 0);
				}
				for (; position - findWhat.length() >= 0; position--) {
					temp = content.substring(position - findWhat.length(), position);
					if (FindManagerUI.isCaseSensitive) {//Case Sensitive
						if (temp.equals(findWhat)) {
							setTextAreaSelection(position - findWhat.length(), position);
							break;
						} else if (position - findWhat.length() == 0) {
							canNotFindKeyWordOperation(0, 0);
							break;
						}
					} else {
						if (temp.equalsIgnoreCase(findWhat)) {
							setTextAreaSelection(position - findWhat.length(), position);
							break;
						} else if (position - findWhat.length() == 0) {
							canNotFindKeyWordOperation(0, 0);
							break;
						}
					}
				}
			}
		}
	}


	private void canNotFindKeyWordOperation(int start, int end){
		setTextAreaSelection(start, end);
		canNotFindKeyWord();
	}
	
	private void canNotFindKeyWord() {
		JOptionPane.showMessageDialog(this, Common.CAN_NOT_FIND + findWhat, Common.NOTEPAD, JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void setTextAreaSelection(int start, int end){
		getSelectedTextArea().setSelectionStart(start);
		getSelectedTextArea().setSelectionEnd(end);
	}

	/**
	 * Showing the <code>ReplaceManagerUI</code> window.
	 */
	public void replace() {
		//log.debug(Common.REPLACE);
		if (null == replaceeManagerUI) {
			replaceeManagerUI = new ReplaceManagerUI(Common.REPLACE);
			replaceeManagerUI.setEditMenuUtil(EditMenuUtil.this);
		} else {
			replaceeManagerUI.setVisible(true);
			replaceeManagerUI.setFocusable(true);
		}
	}
	
	
	/**
	 * Default direction is Forward. The <code>replaceOperation</code> method can NOT be called when <br>
	 * <code>null == getSelectedTextArea().getSelectedText();</code> <br>Or <br><code>Common.EMPTY.equals(getSelectedTextArea().getSelectedText().trim());</code><br>
	 */
	public void replaceOperation(){
		FindManagerUI.isForward = true;
		findNext();
		if (null != getSelectedTextArea().getSelectedText() && !Common.EMPTY.equals(getSelectedTextArea().getSelectedText().trim())) {
			getSelectedTextArea().replaceRange(ReplaceManagerUI.replaceWord, getSelectedTextArea().getSelectionStart(), getSelectedTextArea().getSelectionEnd());
		}
	}

	/**
	 * When user want to call Replace_All method, the application will replace all with case sensitive.<br>
	 * A information window will display after replacing all words.<br>Finally, the application will set <br>
	 * <code>ReplaceManagerUI.replaceCount = 0;</code>
	 */
	public void replaceAllOperation() {
		String replaceWord = ReplaceManagerUI.replaceWord;
		String content = getSelectedTextArea().getText();
		String temp;
		for (int i = 0; i <= content.length() - findWhat.length(); i++) {
			temp = content.substring(i, i + findWhat.length());
			if (ReplaceManagerUI.isCaseSensitive) {
				if (temp.equals(findWhat)) {
					replaceRangeOperation(findWhat, replaceWord, i);
				}
			} else {
				if (temp.equalsIgnoreCase(findWhat)) {
					replaceRangeOperation(findWhat, replaceWord, i);
				}
			}
		}
		JOptionPane.showMessageDialog(this, ReplaceManagerUI.replaceCount + Common.MATCHES_REPLACED, Common.NOTEPAD, JOptionPane.INFORMATION_MESSAGE);
		ReplaceManagerUI.replaceCount = 0;
	}

	private void replaceRangeOperation(String findWhat, String replaceWord, int i) {
		ReplaceManagerUI.replaceCount++;
		getSelectedTextArea().setSelectionStart(i);
		getSelectedTextArea().setSelectionEnd(i + findWhat.length());
		getSelectedTextArea().replaceRange(replaceWord, getSelectedTextArea().getSelectionStart(), getSelectedTextArea().getSelectionEnd());
	}

	public static void selectAll() {
		//log.debug(Common.SELECT_ALL);
		getSelectedTextArea().selectAll();
	}

	public static void timeDate() {
		//log.debug(Common.TIME_DATE);
		getSelectedTextArea().replaceRange(NotepadUtil.getTimeDate(), getSelectedTextArea().getSelectionStart(), getSelectedTextArea().getSelectionEnd());
	}
	
	public void distoryFindManagerUI() {
		if (null != findManagerUI) {
			findManagerUI = null;
		}
	}
	
	public void distoryReplaceeManagerUI() {
		if (null != replaceeManagerUI) {
			replaceeManagerUI = null;
		}
	}

}
