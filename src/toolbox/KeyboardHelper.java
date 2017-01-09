package toolbox;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

public class KeyboardHelper {

	private static List<KeyboardEventListener> listeners = new ArrayList<KeyboardEventListener>();
	private static List<KeyboardEventListener> listenersToAdd = new ArrayList<KeyboardEventListener>();
	private static List<KeyboardEventListener> listenersToRemove = new ArrayList<KeyboardEventListener>();
	
	public static boolean isKeyDown(int keyCode){
		return Keyboard.isKeyDown(keyCode);
	}
	
	public static void update(){
		while(Keyboard.next()){	
			boolean keyState = Keyboard.getEventKeyState();
			int keyCode = Keyboard.getEventKey();
			for(KeyboardEventListener listener:listeners){
				listener.onKeyEvent(keyState, keyCode);
			}
			
			//cant add/remove listeners directly, since we may be iterating through the listeners at the time
			for(KeyboardEventListener listener:listenersToAdd){
				listeners.add(listener);
			}
			for(KeyboardEventListener listener:listenersToRemove){
				listeners.remove(listener);
			}
			listenersToAdd.clear();
			listenersToRemove.clear();
		}
	}
	
	public static void addListener(KeyboardEventListener listener){
		listenersToAdd.add(listener);	
	}
	
	public static void removeListener(KeyboardEventListener listener){
		listenersToRemove.add(listener);
	}
	
}
