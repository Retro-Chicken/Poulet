/**
 * 
 */
package editor.util;

import editor.common.Common;
import editor.ui.AboutUI;
import editor.ui.MainUI;

/**
 * @author Hongten
 * @created Nov 19, 2014
 */
public class HelpMenuUtil extends MainUI {

	private static final long serialVersionUID = 1L;

	//static Logger log = Logger.getLogger(HelpMenuUtil.class);

	private static AboutUI aboutUI;

	public HelpMenuUtil(String title) {
		super(title);
	}

	public void about(MainUI mainUI) {
		//log.debug(Common.ABOUT_NOTEPAD);
		if (null == aboutUI) {
			aboutUI = new AboutUI(Common.ABOUT_NOTEPAD);
			aboutUI.setHelpMenuUtil(HelpMenuUtil.this);
		} else {
			aboutUI.setVisible(true);
			aboutUI.setFocusable(true);
		}
	}

	public void destroyAboutUI() {
		if (null != aboutUI) {
			aboutUI = null;
		}
	}
}
