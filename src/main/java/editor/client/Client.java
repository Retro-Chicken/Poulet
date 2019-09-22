/**
 * 
 */
package editor.client;

import editor.common.Common;
import editor.ui.MainUI;

/**
 * @author Hongten
 * @created Nov 19, 2014
 */
public class Client {

	public static void main(String[] args) {
		start();
	}

	public static MainUI start() {
		MainUI ui = new MainUI(Common.TITLE);
		ui.init();
		return ui;
	}
}
