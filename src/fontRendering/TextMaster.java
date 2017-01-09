package fontRendering;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fontMeshCreator.FontType;
import fontMeshCreator.GuiText;
import fontMeshCreator.TextMeshData;
import renderEngine.Loader;

public class TextMaster {
	public static Loader loader;
	public static Map<FontType, List<GuiText>> texts = new HashMap<FontType, List<GuiText>>();
	private static FontRenderer renderer;
	
	public static void init(Loader theLoader, FontRenderer fontRenderer){
		renderer = fontRenderer;
		loader = theLoader;
	}
	
	
	
	public static void render(){
		renderer.render(texts);
	}
	
	public static void addText(GuiText text){
		FontType font = text.getFont();
		//TextMeshData data = font.loadText(text);
		//int vao = loader.loadToVAO(data.getVertexPositions(), data.getTextureCoords());
		//text.setMeshInfo(vao, data.getVertexCount());
		List<GuiText> textBatch = texts.get(font);
		if(textBatch == null){
			textBatch = new ArrayList<GuiText>();
			texts.put(font,  textBatch);
		}
		textBatch.add(text);
	}
	
	public static void removeText(GuiText text){
		List<GuiText> textBatch = texts.get(text.getFont());
		if(textBatch == null)
			return;
		textBatch.remove(text);
		if(textBatch.isEmpty()){
			texts.remove(textBatch);
		}
	}
	
	public static void cleanUp(){
		renderer.cleanUp();
	}
}
