package renderEngine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import models.ModelData;
import models.OBJFileLoader;
import models.RawModel;
import models.TexturedModel;
import physics.AABB;
import textures.ModelTexture;


/* MasterModelLoader: the global model cache.
 * all models should be loaded through this interface.
 * 
 */
public class ModelCache {
	
	private Loader loader;
	private Map<String, LoadedModel> models = new HashMap<String, LoadedModel>();
	
	public ModelCache(Loader loader){
		this.loader = loader;
	}
	
	
	public TexturedModel loadModel(String modelName){
		
		//if model is already cached, return the cached one
		if(this.models.containsKey(modelName)){
			return this.models.get(modelName).texturedModel;
		}
		
		//otherwise load the model.
		Path path = Paths.get("res/"+modelName+".obj");
		if(!Files.exists(path)){
			System.err.println("ModelCache: model '"+modelName+"' not found.");
			return null;
		}
		
		ModelData modelData = OBJFileLoader.loadOBJ(modelName);
		if(modelData != null){
			RawModel rawModel = loader.loadToVAO(modelData.getVertices(), modelData.getTextureCoords(), modelData.getNormals(), modelData.getIndices());
			int modelTextureId;
			try{
				modelTextureId = loader.loadTexture(modelName);
			}
			catch(IOException e){
				System.err.println("Failed to load texture for model '"+ modelName +"'.");
				return null;
			}
			TexturedModel texturedModel = new TexturedModel(rawModel, new ModelTexture(modelTextureId));
			this.models.put(modelName, new LoadedModel(modelData, rawModel, texturedModel));
			return texturedModel;
		}
		else{
			System.err.println("ModelCache: error loading model '"+modelName+"'.");
			return null;
		}
	}
	
	public AABB getAABB(String modelName){
		if(this.models.containsKey(modelName)){
			return this.models.get(modelName).modelData.getAABB();
		}
		else{
			System.err.println("ModelCache.getAABB(): the model has not been loaded!");
			return null;
		}
	}
	

	public TexturedModel dbg_loadRawModelData(String modelName, ModelData modelData){
		RawModel rawModel = loader.loadToVAO(modelData.getVertices(), modelData.getTextureCoords(), modelData.getNormals(), modelData.getIndices());
		int modelTextureId;
		try{
			modelTextureId = loader.loadTexture(modelName);
		}
		catch(IOException e){
			System.err.println("Failed to load texture for model '"+ modelName +"'.");
			return null;
		}
		TexturedModel texturedModel = new TexturedModel(rawModel, new ModelTexture(modelTextureId));
		this.models.put(modelName, new LoadedModel(modelData, rawModel, texturedModel));
		return texturedModel;
		
	}
	
	public class LoadedModel{
		public ModelData modelData;
		public RawModel rawModel;
		public TexturedModel texturedModel;
		
		
		public LoadedModel(ModelData modelData, RawModel rawModel, TexturedModel texturedModel) {
			this.modelData = modelData;
			this.rawModel = rawModel;
			this.texturedModel = texturedModel;
		}
		
		
	}
	
	
}
