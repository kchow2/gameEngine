package toolbox;

import org.lwjgl.input.Mouse;

import renderEngine.DisplayManager;

public class MouseHelper {
	private static int dx, dy;
	
	private boolean isMouseGrabbed = false;
	
	public static void grabMouse(){
		if(!Mouse.isGrabbed()){
			Mouse.setGrabbed(true);
			dx = 0;
			dy = 0;
			Mouse.setCursorPosition(dx, dy);
		}
	}
	
	public static boolean isGrabbed(){
		return Mouse.isGrabbed();
	}
	
	public static void releaseMouse(){
		dx = DisplayManager.getScreenWidth() / 2;
		dy = DisplayManager.getScreenHeight() / 2;
		Mouse.setCursorPosition(dx, dy);
		Mouse.setGrabbed(false);
	}
	
	public static void update(){
		dx = Mouse.getDX();
		dy = Mouse.getDY();
	}
	
	public static int getDX(){
		return dx;
	}
	
	public static int getDY(){
		return dy;
	}
	
	public static void resetPos(){
		dx = DisplayManager.getScreenWidth() / 2;
		dy = DisplayManager.getScreenHeight() / 2;
	}
}
