/**
 * 
 */
package editor.client;

import editor.common.Common;
import editor.poulet.PouletUI;
import editor.ui.MainUI;

/**
 * @author Hongten
 * @created Nov 19, 2014
 */
public class Client {

	public static void main(String[] args) {
		start();
	}

	public static PouletUI start() {
		PouletUI ui = new PouletUI(Common.TITLE);
		ui.init();
		return ui;
	}

	public static MainUI startBasic() {
		MainUI ui = new MainUI(Common.TITLE);
		ui.init();
		return ui;
	}
}
