/**
 * 
 */
package editor.util;

import editor.common.Common;
import editor.ui.FontManagerUI;
import editor.ui.FontSizeManagerUI;
import editor.ui.FontStyleManagerUI;
import editor.ui.MainUI;

import javax.swing.*;
import java.awt.Font;

/**
 * @author Hongten
 * @created Nov 19, 2014
 */
public class FormatMenuUtil {
	private final MainUI parent;

	public FormatMenuUtil(MainUI parent) {
		this.parent = parent;
	}

	public void wordWrap() {
		parent.lineWrap = !parent.lineWrap;
		for(JTextArea area : parent.getTextAreas())
			area.setLineWrap(parent.lineWrap);
	}
	
	public void resetFont() {
		parent.fontNum = Common.FONT_NUM;
		FontManagerUI.FONT_TYPE = Common.FONT_LUCIDA_CONSOLE;
		parent.fontSizeNum = Common.FONT_SIZE_NUM;
		FontManagerUI.FONT_SIZE = Common.FONT_SIZE;
		FontManagerUI.FONT_STYLE = Common.FONT_STYLE_DEFAULT;
		parent.fontStyleNum = Common.FONT_STYLE_NUM;
		for(JTextArea area : parent.getTextAreas())
			area.setFont(new Font(FontManagerUI.FONT_TYPE, parent.fontStyleNum, FontManagerUI.FONT_SIZE));
		parent.setJUI();
	}
}
