/**
 * 
 */
package editor.util;

import editor.common.Common;
import editor.ui.MainUI;
import editor.ui.SkinManagerUI;

/**
 * @author Hongten
 * @created Nov 19, 2014
 */
public class ViewMenuUtil extends MainUI {

	private static final long serialVersionUID = 1L;

	//static Logger log = Logger.getLogger(ViewMenuUtil.class);

	private static SkinManagerUI skinManagerUI;

	public ViewMenuUtil(String title) {
		super(title);
	}

	public void skin(MainUI mainUI) {
		//log.debug(Common.SKIN);
		if (null == skinManagerUI) {
			skinManagerUI = new SkinManagerUI(Common.SKIN);
			skinManagerUI.setViewMenuUtil(ViewMenuUtil.this);
		} else {
			skinManagerUI.setVisible(true);
			skinManagerUI.setFocusable(true);
		}
	}

	public void distorySkinManagerUI() {
		if (null != skinManagerUI) {
			skinManagerUI = null;
		}
	}

}
