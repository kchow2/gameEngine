package guis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import entities.Camera;
import entities.Player;
import fontMeshCreator.FontType;
import fontMeshCreator.GuiText;
import fontRendering.FontRenderer;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import worldeditor.WorldEditor;

public class GuiMaster {

	private static WorldEditorGui worldEditorGui;
	
	private static List<GuiTexture> guiTexturesToRender = new ArrayList<GuiTexture>();
	private static List<GuiText> guiTextsToRender = new ArrayList<GuiText>();
	
	public static void init(Loader loader){

	}
	
	public static WorldEditorGui getWorldEditorGui(){
		return worldEditorGui;
	}
	
	public static void showWorldEditorGui(WorldEditor worldEditor, MasterRenderer masterRenderer, Loader loader, Camera camera, Player player){
		if(worldEditorGui == null)
			worldEditorGui = new WorldEditorGui(loader, masterRenderer, worldEditor, camera, player);
		worldEditorGui.setVisible(true);
	}
	
	public static void hideWorldEditorGui(){
		if(worldEditorGui != null)
			worldEditorGui.setVisible(false);
	}
	
	public static void render(GuiRenderer renderer){	
		guiTexturesToRender.clear();
		if(worldEditorGui != null){
			worldEditorGui.update();
			worldEditorGui.render(guiTexturesToRender);
		}
		renderer.render(guiTexturesToRender);
	}
	
	public static void renderTexts(FontRenderer fontRenderer){
		guiTextsToRender.clear();
		if(worldEditorGui != null){
			worldEditorGui.renderTexts(guiTextsToRender);
		}
		if(guiTextsToRender.size() > 0){
			FontType fontType = guiTextsToRender.get(0).getFont();
			fontRenderer.renderTextBatch(fontType, guiTextsToRender);
		}
	}
}
