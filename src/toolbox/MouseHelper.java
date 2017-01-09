package toolbox;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import renderEngine.DisplayManager;

public class MouseHelper {
	
	private static List<MouseEventListener> listeners = new ArrayList<MouseEventListener>();
	private static List<MouseEventListener> listenersToAdd = new ArrayList<MouseEventListener>();
	private static List<MouseEventListener> listenersToRemove = new ArrayList<MouseEventListener>();
	
	private static int dx, dy;
	
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
		
		while(Mouse.next()){
			int button = Mouse.getEventButton();
			boolean state = Mouse.getEventButtonState();
			int x = Mouse.getEventX();
			int y = Mouse.getEventY();
			
			for(MouseEventListener listener:listeners){
				listener.onMouseEvent(state, button, x, y);
			}
			
			//cant add/remove listeners directly, since we may be iterating through the listeners at the time
			for(MouseEventListener listener:listenersToAdd){
				listeners.add(listener);
			}
			for(MouseEventListener listener:listenersToRemove){
				listeners.remove(listener);
			}
			listenersToAdd.clear();
			listenersToRemove.clear();
		}
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
	
	public static void addListener(MouseEventListener listener){
		listenersToAdd.add(listener);
	}
	
	public static void removeListener(MouseEventListener listener){
		listenersToRemove.add(listener);
	}
}
