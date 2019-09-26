package editor.common;

/**
 * @author Hongten
 * @created Nov 19, 2014
 */
public class Common {
	// Default configurations
	public static final int DEFAULT_SKIN_NUM = 4;
	public static final boolean DEFAULT_WORD_WRAP = false;
	public static final boolean DEFAULT_LINE_NUMBERS = true;


	public static final String HYPHEN = "-";
	public static final String EMPTY = "";
	public static final String NEW_LINE = "\r\n";
	public static final String BLANK = " ";
	public static final String QUESTION_MARK = "?";
	public static final String POINT = ".";
	public static final String COLON = ":";
	public static final String STAR = "*";
	public static final String DEFAULT_FILTER_EXT = "poulet";
	public static final String DEFAULT_FILTER = STAR + POINT + DEFAULT_FILTER_EXT;

	public static final String UNTITLED = "Untitled";
	public static final String NOTEPAD = "Poulet";
	public static final String NOTEPAD_NOTEPAD = BLANK + HYPHEN + BLANK + NOTEPAD;
	public static final String SYSTEM_EXIT = "System Exit";
	public static final String SYSTEM_OPEN = "System Open";

	public static final String FILE = "File";
	public static final String EDIT = "Edit";
	public static final String FORMAT = "Format";
	public static final String VIEW = "View";
	public static final String Help = "Help";

	// File Items
	public static final String NEW = "New";
	public static final String OPEN = "Open...";
	public static final String SAVE = "Save";
	public static final String SAVE_AS = "Save as...";
	public static final String PROPERTIES = "Properties";
	public static final String EXIT = "Exit";

	// Edit Items
	public static final String UNDO = "Undo";
	public static final String COPY = "Copy";
	public static final String PASTE = "Paste";
	public static final String CUT = "Cut";
	public static final String DELETE = "Delete";
	public static final String FIND = "Find...";
	public static final String FIND_NEXT = "Find Next";
	public static final String REPLACE = "Replace";
	public static final String GO_TO = "Go To...";
	public static final String SELECT_ALL = "Select All";
	public static final String TIME_DATE = "Time/Date";

	// Format Items
	public static final String WORD_WRAP = "Word Wrap";
	public static final String RESET_FONT = "Reset Font";
	public static final String FONT = "Font";
	public static final String FONT_STYLE = "Font Style";
	public static final String FONT_SIZE_TITLE = "Font Size";

	// View
	public static final String SKIN = "Change Skin";
	public static final String LINE_NUMBERS = "Line Numbers";

	// Help Items
	public static final String VIEW_HELP = "View Help";
	public static final String ABOUT_NOTEPAD = "About Poulet";

	// KeyStroke
	public static final char A = 'A';
	public static final char N = 'N';
	public static final char O = 'O';
	public static final char L = 'L';
	public static final char Z = 'Z';
	public static final char C = 'C';
	public static final char D = 'D';
	public static final char W = 'W';
	public static final char H = 'H';
	public static final char F = 'F';
	public static final char V = 'V';
	public static final char X = 'X';
	public static final char G = 'G';
	public static final char S = 'S';
	public static final char P = 'P';
	public static final char T = 'T';
	public static final char R = 'R';
	public static final char SPACE = ' ';

	public static final String RC_LOGO = "pouletlogo.png";

	// About UI
	public static final String AUTHOR = "Author";
	public static final String AUTHOR_NAME = "Retro Chicken";
	public static final String ITEM = "Item";
	public static final String DESCRIPTION = "Description";
	public static final String APPLICATION = "Application";
	public static final String NAME = "Name";
	public static final String APPLICATION_NAME = APPLICATION + BLANK + NAME;
	public static final String NOTEPAD_APP = NOTEPAD;
	public static final String APPLICATION_DESCRIPTION = APPLICATION + BLANK + DESCRIPTION;
	public static final String APPLICATION_DESCRIPTION_DETAIL = "A CiC Language";
	public static final String VERSION = "Version";
	public static final String VERSION_VALUE = "1.1";
	public static final String HOME_PAGE = "Home Page";
	public static final String HOME_PAGE_URL = "http://www.github.com/retro-chicken";
	public static final String NOTEPAD_PUBLISHED_PAGE = HOME_PAGE_URL + "/poulet";

	public static final int TABLE_ROW_HEIGHT = 20;

	// Dialog messages and titles
	public static final String CONFIRM_EXIT = "Confirm Exit";
	public static final String ACCESS_URL_REQUEST = "Access URL Request";
	public static final String ACCESS_URL = "Access URL : ";

	public static final String FONT_LUCIDA_CONSOLE = "Lucida Console";
	public static final String FONT_TYPE = "Monospaced";
	public static final int FONT_SIZE = 14;
	public static final int FONT_NUM = 148;
	public static final int FONT_SIZE_NUM = 4;
	public static final int FONT_STYLE_NUM = 0;
	public static final String FONT_STYLE_DEFAULT = "Regular";
	public static final String DATE_FORMAT = "HH:mm MM/dd/yyyy";
	public static final String THIS_IS_A_SAMPLE = "This is a Sample";
	public static final String SAMPLE = "Sample";

	public static final String CURRENT_SKIN = "Current Skin" + BLANK + COLON + BLANK;
	public static final String DESCRIPTION_WITH_COLON = DESCRIPTION + BLANK + COLON + BLANK;
	public static final String CURRENT_FONT = "Current Font" + BLANK + COLON + BLANK;
	public static final String CURRENT_FONT_SIZE = "Current Font Size" + BLANK + COLON + BLANK;
	public static final String CURRENT_FONT_STYLE = "Current Font Style" + BLANK + COLON + BLANK;

	public static final String DO_YOU_WANT_TO_SAVE_CHANGES = "Do you want to save changes?";
	public static final String SAVE_ALL_CHANGES = "Do you want to save all changes?";
	public static final String WHAT_DO_YOU_WANT_TO_FIND = "Please type what you want to find.";
	public static final String CAN_NOT_FIND = "Cannot find ";
	public static final String MATCHES_REPLACED = " matches replaced!";

	public static final String FIND_WHAT = "Find What :";
	public static final String REPLACE_TO = "Replace To :";
	public static final String REPLACE_ALL = "Replace All";
	public static final String CASE_SENSITIVE = "Case Sensitive";
	public static final String FORWARD = "Forward";
	public static final String BACKWARD = "Backward";
	public static final String CANCEL = "Cancel";
	public static final String GB2312 = "GB2312";
	
	public static final String NOTEPAD_HOME_PAGE = "Home Page";
	public static final String SOURCE = "Source";
}
