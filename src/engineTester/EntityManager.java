package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import renderEngine.Loader;
import renderEngine.MasterRenderer;
import textures.ModelTexture;
import models.ModelData;
import models.OBJFileLoader;
import models.RawModel;
import models.TexturedModel;
import entities.Entity;

public class EntityManager {
	
	private static List<Entity> entities = new ArrayList<Entity>();
	
	public EntityManager(){
		
	}
	
	public void renderAllEntities(MasterRenderer renderer){
		for(Entity e:entities){
			renderer.processEntity(e);
		}
	}
	
	//populates the world with a bunch of random shit like trees, ferns, etc
	public void populateWorld(Loader loader){
		final int NUM_FERNS = 50;
		final int NUM_TREES = 25;
		final int NUM_GRASS = 50;
		
		final float XMIN = 0;
		final float ZMIN = 0;
		final float MAPSIZE = 800;
		
		//FERNS
		ModelData modelFernData = OBJFileLoader.loadOBJ("fern");
		RawModel modelFern = loader.loadToVAO(modelFernData.getVertices(), 
				modelFernData.getTextureCoords(), modelFernData.getNormals(),
				modelFernData.getIndices());
		ModelTexture fernTexture = new ModelTexture(loader.loadTexture("fern"));
		fernTexture.setTransparent(true);
		fernTexture.setUseFakeLighting(true);
		TexturedModel modelFernTextured = new TexturedModel(modelFern, fernTexture);
		for(int i = 0; i < NUM_FERNS; i++){
			Entity e = new Entity(modelFernTextured, new Vector3f(), 0, 0, 0, 1.0f);
			entities.add(e);
		}
		
		//TREES
		ModelData modelTreeData = OBJFileLoader.loadOBJ("tree");
		RawModel modelTree = loader.loadToVAO(modelTreeData.getVertices(), 
				modelTreeData.getTextureCoords(), modelTreeData.getNormals(),
				modelTreeData.getIndices());
		ModelTexture treeTexture = new ModelTexture(loader.loadTexture("tree"));
		TexturedModel modelTreeTextured = new TexturedModel(modelTree, treeTexture);
		for(int i = 0; i < NUM_TREES; i++){
			Entity e = new Entity(modelTreeTextured, new Vector3f(), 0, 0, 0, 1.0f);
			entities.add(e);
		}
		
		//GRASS
		ModelData modelGrassData = OBJFileLoader.loadOBJ("grassModel");
		RawModel modelGrass = loader.loadToVAO(modelGrassData.getVertices(), 
				modelGrassData.getTextureCoords(), modelGrassData.getNormals(),
				modelGrassData.getIndices());
		ModelTexture grassTexture = new ModelTexture(loader.loadTexture("grassTexture"));
		grassTexture.setTransparent(true);
		grassTexture.setUseFakeLighting(true);
		TexturedModel modelGrassTextured = new TexturedModel(modelGrass, grassTexture);
		for(int i = 0; i < NUM_TREES; i++){
			Entity e = new Entity(modelGrassTextured, new Vector3f(), 0, 0, 0, 1.0f);
			entities.add(e);
		}
		
		//spread them out over the map
		for(Entity e:entities){
			randomizeEntityPosition(e, XMIN, ZMIN, MAPSIZE);
		}
	}
	
	private void randomizeEntityPosition(Entity entity, float xMin, float zMin, float mapSize){
		Random r = new Random();
		entity.getPosition().x = (float)(r.nextDouble()*mapSize+xMin);
		entity.getPosition().z = (float)(r.nextDouble()*mapSize+zMin);
		entity.setRotY((float)r.nextDouble()*360.0f);
	}
	
	public void cleanUp(){
		entities.clear();
	}
	
}