package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import models.ModelData;
import models.OBJFileLoader;
import models.RawModel;
import models.TexturedModel;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.Loader;
import terrain.Terrain;
import textures.ModelTexture;

//class responsible for physics for all mobile entities in the game
public class MobileEntityManager {
private static List<MobileEntity> entities = new ArrayList<MobileEntity>();
	
	public MobileEntityManager(){
		
	}
	
	public List<MobileEntity> getEntities(){
		return entities;
	}
	
	public void addEntity(MobileEntity e){
		entities.add(e);
	}
	
	public void updateEntities(Terrain terrain){
		boolean bounce = false;
		if(Keyboard.isKeyDown(Keyboard.KEY_K)){
			bounce = true;
		}
		for(MobileEntity e:entities){
			
			if(bounce){
				e.getVelocity().y = 10.0f;
			}
			e.update(terrain);
		}
	}
	
	//populates the world with a bunch of random shit like trees, ferns, etc
	public void populateWorld(EntityManager entityManager, Loader loader, Terrain terrain){
		final int NUM_BOXES = 500;
		final float XMIN = 0;
		final float ZMIN = 0;
		final float MAPSIZE = 800;
		
		//FERNS
		ModelData modelCubeData = OBJFileLoader.loadOBJ("cube");
		RawModel modelCube = loader.loadToVAO(modelCubeData.getVertices(), 
				modelCubeData.getTextureCoords(), modelCubeData.getNormals(),
				modelCubeData.getIndices());
		ModelTexture cubeTex = new ModelTexture(loader.loadTexture("cube"));
		TexturedModel modelCubeTextured = new TexturedModel(modelCube, cubeTex);
		for(int i = 0; i < NUM_BOXES; i++){
			MobileEntity e = new MobileEntity(modelCubeTextured, new Vector3f(), 0, 0, 0, 1.0f, modelCubeData.getAABB());
			entities.add(e);
			entityManager.addEntity(e);	//the entity manager is still responsible for rendering all entities. This class only does movement
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
