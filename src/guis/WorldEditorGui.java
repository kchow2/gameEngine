package guis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Player;
import fontMeshCreator.FontType;
import fontMeshCreator.GuiText;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import toolbox.KeyboardEventListener;
import toolbox.KeyboardHelper;
import toolbox.MouseEventListener;
import toolbox.MouseHelper;
import toolbox.MousePicker;
import worldeditor.WorldEditor;

public class WorldEditorGui implements MouseEventListener, KeyboardEventListener, GuiEventListener{
	private WorldEditor worldEditor;
	private MousePicker mousePicker;
	private boolean isVisible = false;
	private List<GuiComponent> components = new ArrayList<GuiComponent>();
	private FontType fontType;
	private static final float fontSize = 1.2f;
	
	//private List<GuiTexture> guiTextures = new ArrayList<GuiTexture>();
	//private List<GuiText> texts = new ArrayList<GuiText>();
	
	public WorldEditorGui(Loader loader, MasterRenderer masterRenderer, WorldEditor worldEditor, Camera camera, Player player){
		this.worldEditor = worldEditor;
		try{
			fontType = new FontType(loader.loadTexture("arial"), new File("res/arial.fnt"));
			
			GuiPosition position = GuiPosition.fromXYWH(0,0,60,250);
			GuiTexture panelTexture = new GuiTexture(loader.loadTexture("editor-panel"), position.center, position.scale);
			
			GuiPosition btnPosition = GuiPosition.fromXYWH(10,5,50,25);
			GuiTexture btnTexture = new GuiTexture(loader.loadTexture("editor-button"), btnPosition.center, btnPosition.scale);
			
			GuiPanel mainPanel = new GuiPanel(1, loader, position, panelTexture);
			this.components.add(mainPanel);
			mainPanel.addChild(new GuiButton(2, loader, btnPosition, btnTexture, fontType, fontSize, "Label", this));
		}
		catch(IOException e){
			System.out.println("Failed to load WorldEditor gui!");
		}
		MouseHelper.addListener(this);
		KeyboardHelper.addListener(this);
		
		mousePicker = new MousePicker(camera, masterRenderer.getProjectionMatrix(),worldEditor.getTerrain());
	}
	
	public void setVisible(boolean isVisible){
		this.isVisible = isVisible;
	}
	
	public void render(List<GuiTexture> textures){
		if(isVisible){
			for(GuiComponent component:components){
				component.render(textures);
			}
		}
	}
	
	public void renderTexts(List<GuiText> texts){
		if(!isVisible)
			return;
		for(GuiComponent component:components){
			component.renderText(texts);
		}
	}
	
	public void update(){
		if(Mouse.isButtonDown(1)){
			mousePicker.update();
			Vector3f mouseWorldPos = mousePicker.getCurrentTerrainPoint();
			if(mouseWorldPos != null)
				worldEditor.applyTerrainBrush(mouseWorldPos);
		}
	}
	
	//private void addButton(GuiPosition guiPos, int textureID, String label){
	//	this.guiTextures.add(new GuiTexture(textureID, guiPos.center, guiPos.scale));
	//	//addText(new GuiText(label));
	//}

	@Override
	public void onButtonClick(int guiID, int state) {		
		System.out.println("ID:"+guiID+" clicked. state="+state);
	}

	@Override
	public void onKeyEvent(boolean state, int keyCode) {
		if(state && keyCode == Keyboard.KEY_M){
			worldEditor.toggleWireframeMode();
		}
		
		for(GuiComponent component:components){
			if(component.onKeyEvent(state, keyCode))
				return;
		}
	}

	@Override
	public void onMouseEvent(boolean state, int button, int x, int y) {
		if(!isVisible)
			return;
		
		for(GuiComponent component:components){
			if(component.onMouseEvent(state, button, x, y)){
				return;
			}
		}
		
		if(state && button==0){
			mousePicker.update();
			Vector3f mouseWorldPos = mousePicker.getCurrentTerrainPoint();
			if(mouseWorldPos != null)
				worldEditor.placeObject(mouseWorldPos);
		}
	}
	
//	private GuiComponent getComponentByID(int id){
//		for(GuiComponent component:components){
//			GuiComponent c = component.getChildByID(id);
//			if(c!=null)
//				return c;
//		}
//		return null;
//	}
}
