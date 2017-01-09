package guis;

import org.lwjgl.util.vector.Vector2f;

import renderEngine.DisplayManager;

/*
 * Helper class for positioning GUI elements.
 * Stores the position and size as a position and scale in openGL coordinates
 */
public class GuiPosition{
	
	public Vector2f center;
	public Vector2f scale;
	
	public GuiPosition(Vector2f center, Vector2f scale){
		this.center = center;
		this.scale = scale;
	}
	
	//converts x,y,width,height to opengl coordinates
	public static GuiPosition fromXYWH(int x, int y, int width, int height){
		float xScl = 2.0f / DisplayManager.getScreenWidth();
		float yScl = 2.0f / DisplayManager.getScreenHeight();
		float xSize = width * xScl;
		float ySize = height * yScl;
		float xCenter = x * xScl - 1.0f + xSize;
		float yCenter = -(y * xScl - 1.0f + ySize);
		
		return new GuiPosition(new Vector2f(xCenter,yCenter), new Vector2f(xSize, ySize));
	}
}
