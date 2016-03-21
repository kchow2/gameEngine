package guis;

import org.lwjgl.util.vector.Vector2f;

public class GuiTexture {
	private int textureId;
	private Vector2f position, scale;
	private boolean _isVisible = true;
	
	public GuiTexture(int textureId, Vector2f position, Vector2f scale) {
		this.textureId = textureId;
		this.position = position;
		this.scale = scale;
	}
	public int getTextureId() {
		return textureId;
	}
	public Vector2f getPosition() {
		return position;
	}
	public Vector2f getScale() {
		return scale;
	}
	
	public void show(){
		this._isVisible = true;
	}
	
	public void hide(){
		this._isVisible = false;
	}
	
	public boolean isVisible(){
		return this._isVisible;
	}
	
	
}
