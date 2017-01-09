package guis;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import fontMeshCreator.FontType;
import fontMeshCreator.GuiText;
import renderEngine.DisplayManager;
import renderEngine.Loader;

public class GuiButton extends GuiComponent{

	private GuiEventListener listener;
	private GuiText buttonLabel;
	
	private boolean isPressed = false;
	
	public GuiButton(int guiID, Loader loader, GuiPosition guiPosition, GuiTexture texture, FontType fontType, float fontSize, String label, GuiEventListener listener) {
		super(guiID, loader, guiPosition, texture);
		this.buttonLabel = new GuiText(loader, label, fontSize, fontType, new Vector2f(), 1.0f, false);
		float margin = 0.0f;
		Vector2f textPos = calcTextPos(this.position, buttonLabel, 1, margin, margin);
		buttonLabel.setPosition(textPos);
		this.listener = listener;
	}
	
	@Override
	public boolean onMouseEvent(boolean state, int button, int x, int y){
		//Vector2f v = buttonLabel.getPosition();
		//Vector2f v2 = calcTextPos(this.position);
		//v.x = (float) x / DisplayManager.getScreenWidth();
		//v.y = 1.0f - (float)y / DisplayManager.getScreenHeight();
		//System.out.println("xy:"+v.x + " "+v.y);
		if(isPointInside(x,y)){
			if(isPressed && !state && listener != null){	//generates an event on the falling edge of the mouse click
				listener.onButtonClick(this.guiID, state?1:0);
			}
			isPressed = state;
			return true;
		}
		
		return false;
	}
	
	@Override
	public void renderText(List<GuiText> texts){
		texts.add(buttonLabel);
	}

}
