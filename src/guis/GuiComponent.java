package guis;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import fontMeshCreator.GuiText;
import renderEngine.DisplayManager;
import renderEngine.Loader;

public class GuiComponent {
	protected int guiID;
	protected GuiPosition position;
	protected GuiComponent parent;
	protected GuiTexture texture;
	protected List<GuiComponent> children = new ArrayList<GuiComponent>();
	protected boolean isVisible = true;
	
	public GuiComponent(int guiID, Loader loader, GuiPosition guiPosition, GuiTexture texture){
		this.guiID = guiID;
		this.position = guiPosition;
		this.texture = texture;
	}
	
	public void render(List<GuiTexture> textures){
		if(!isVisible)
			return;
		
		if(texture != null)
			textures.add(texture);
		
		for(GuiComponent child:children){
			child.render(textures);
		}
	}
	
	public void renderText(List<GuiText> texts){
		if(!isVisible)
			return;
		for(GuiComponent child:children){
			child.renderText(texts);
		}
	}
	
	public void setVisible(boolean isVisible){
		this.isVisible = isVisible;
	}
	
	/**
	 * @param keyCode the key ID of the key that was pressed
	 * @param state 1 if key down, 0 if key up
	 * @return true if this component handled the event. False if the event is to be passed on to the next control in the chain.
	 */
	protected boolean onKeyEvent(boolean state, int keyCode){
		for(GuiComponent child:children){
			if(child.onKeyEvent(state, keyCode))
				return true;
		}
		return false;
	}
	
	/**
	 * @param button 0:LEFT 1:RIGHT 2:MIDDLE
	 * @param state 0 if down, 1 if up
	 * @return true if this component handled the event. False if the event is to be passed on to the next control in the chain.
	 */
	protected boolean onMouseEvent(boolean state, int button, int x, int y){
		if(!this.isPointInside(x, y)){
			return false;
		}

		for(GuiComponent child:children){
			if(child.onMouseEvent(state, button, x, y))
				return true;
		}
		return true;
	}
	
	public void addChild(GuiComponent component){
		this.children.add(component);
	}
	
	protected GuiComponent getChildByID(int ID){
		if(this.guiID == ID){
			return this;
		}
		else{
			for(GuiComponent child:children){
				GuiComponent guiComponent = child.getChildByID(ID);
				if(guiComponent != null){
					return guiComponent;
				}
			}
		}
		return null;
	}
	
	/**
	 * Takes mouse coordinates and determines if they are within the bounding area of this component.
	 */
	protected boolean isPointInside(int mouseX, int mouseY){
		float x = 2.0f*mouseX / DisplayManager.getScreenWidth() - 1.0f;
		float y = 2.0f*mouseY / DisplayManager.getScreenHeight() - 1.0f;
		
		if(x < position.center.x - position.scale.x || x > position.center.x + position.scale.x){
			return false;
		}
		else if(y < position.center.y - position.scale.y || y > position.center.y + position.scale.y)
			return false;
		return true;
	}
	
	/**
	 * 
	 * @param position the position in GL coordinates
	 * @return the position in screen coordinates of the top left corner of the text
	 */
	protected Vector2f calcTextPos(GuiPosition position, GuiText text, int alignMode, float xMargin, float yMargin){
		float parentTop =  1.0f - (position.center.y + position.scale.y + 1.0f) / 2.0f;
		if(alignMode == 0){	//left align
			float parentLeft = (position.center.x - position.scale.x + 1.0f) / 2.0f;
			float x = xMargin + parentLeft;
			float y = yMargin + parentTop;
			return new Vector2f(x,y);
		}else if(alignMode == 1){	//centered. margin is ignored.
			float parentCenter = (position.center.x + 1.0f) / 2.0f;
			float x = parentCenter - text.getRenderWidth() / 2.0f;
			float y = yMargin + parentTop;
			return new Vector2f(x,y);
		}else{	//right align
			float parentRight = (position.center.x + position.scale.x + 1.0f) / 2.0f;
			float x = parentRight - text.getRenderWidth() - xMargin;
			float y = yMargin + parentTop;
			return new Vector2f(x,y);
		}
	}
	
}
