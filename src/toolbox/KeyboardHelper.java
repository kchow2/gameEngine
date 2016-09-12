package toolbox;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

public class KeyboardHelper {

	private static List<KeyboardEventListener> listeners = new ArrayList<KeyboardEventListener>();
	
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
		}
	}
	
	public static void addListener(KeyboardEventListener listener){
		listeners.add(listener);
	}
	
	public static void removeListener(KeyboardEventListener listener){
		listeners.remove(listener);
	}
	
}
