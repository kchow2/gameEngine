package entities;

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

//this class is responsible for rendering all entities in the game
public class EntityRenderingManager {
	
	private static List<Entity> entities = new ArrayList<Entity>();
	private static List<Entity> deadEntities = new ArrayList<Entity>();
	private static List<Entity>	hiddenEntities = new ArrayList<Entity>();
	
	public EntityRenderingManager(){
		
	}
	
	public List<Entity> getEntities(){
		return entities;
	}
	
	public void addEntity(Entity e){
		entities.add(e);
	}
	
	public void removeEntity(Entity e){
		entities.remove(e);
	}
	
	public void hideEntity(Entity e){
		entities.remove(e);
		if(!hiddenEntities.contains(e)){
			hiddenEntities.add(e);
		}
	}
	
	public void showEntity(Entity e){
		if(!entities.contains(e)){
			entities.add(e);
		}
		hiddenEntities.remove(e);
	}
	
	public void update(){
		deadEntities.clear();
		for(Entity e:entities){
			if(!e.isAlive()){
				deadEntities.add(e);
			}
		}
		entities.removeAll(deadEntities);
		hiddenEntities.removeAll(deadEntities);
	}
	
	public void cleanUp(){
		entities.clear();
	}
	
}
