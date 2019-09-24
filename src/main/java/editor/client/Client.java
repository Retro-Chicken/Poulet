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
	public static MainUI MAIN_UI;

	public static void main(String[] args) {
		start();
	}

	public static PouletUI start() {
		PouletUI ui = new PouletUI(Common.NOTEPAD);
		MAIN_UI = ui;
		ui.init();
		return ui;
	}

	public static MainUI startBasic() {
		MainUI ui = new MainUI(Common.NOTEPAD);
		MAIN_UI = ui;
		ui.init();
		return ui;
	}
}
