package toolbox;

public interface MouseEventListener {
	/**
	 * Is called whenever there is a mouse event.
	 * @param state: 0 if down, 1 if up.
	 * @param button: 0 for left mouse button, 1 for right mouse button, 2 for middle mouse click.
	 */
	public void onMouseEvent(boolean state, int button, int x, int y);
}
