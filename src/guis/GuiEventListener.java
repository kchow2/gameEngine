package guis;

public interface GuiEventListener {

	/**
	 * Allows GuiButtons, etc to send events back to the parent GUI.
	 */
	public void onButtonClick(int guiID, int state);
}
