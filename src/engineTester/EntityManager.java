package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import renderEngine.Loader;
import renderEngine.MasterRenderer;
import terrain.Terrain;
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
	
	public List<Entity> getEntities(){
		return entities;
	}
	
	public void addEntity(Entity e){
		entities.add(e);
	}
	
	public void renderAllEntities(MasterRenderer renderer){
		for(Entity e:entities){
			renderer.processEntity(e);
		}
	}
	
	//populates the world with a bunch of random shit like trees, ferns, etc
	public void populateWorld(Loader loader, Terrain terrain){
		final int NUM_FERNS = 100;
		final int NUM_TREES = 50;
		final int NUM_GRASS = 100;
		
		final float XMIN = 0;
		final float ZMIN = 0;
		final float MAPSIZE = 800;
		
		//FERNS
		ModelData modelFernData = OBJFileLoader.loadOBJ("fern");
		RawModel modelFern = loader.loadToVAO(modelFernData.getVertices(), 
				modelFernData.getTextureCoords(), modelFernData.getNormals(),
				modelFernData.getIndices());
		ModelTexture fernAtlas = new ModelTexture(loader.loadTexture("fernAtlas"));
		fernAtlas.setTransparent(true);
		fernAtlas.setUseFakeLighting(true);
		fernAtlas.setNumberOfRows(2);
		TexturedModel modelFernTextured = new TexturedModel(modelFern, fernAtlas);
		Random rand = new Random();
		for(int i = 0; i < NUM_FERNS; i++){
			Entity e = new Entity(modelFernTextured, new Vector3f(), 0, 0, 0, 1.0f);
			e.setTextureOffset(rand.nextInt()%4);
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
			Entity e = new Entity(modelTreeTextured, new Vector3f(), 0, 0, 0, 4.0f);
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
			randomizeEntityPosition(e, XMIN, ZMIN, MAPSIZE, terrain);
		}
	}
	
	private void randomizeEntityPosition(Entity entity, float xMin, float zMin, float mapSize, Terrain terrain){
		Random r = new Random();
		float x = (float)(r.nextDouble()*mapSize+xMin);
		float z = (float)(r.nextDouble()*mapSize+zMin);
		entity.getPosition().x = x;
		entity.getPosition().y = terrain.getTerrainHeight(x,z);
		entity.getPosition().z = z;
		entity.setRotY((float)r.nextDouble()*360.0f);
	}
	
	public void cleanUp(){
		entities.clear();
	}
	
}
