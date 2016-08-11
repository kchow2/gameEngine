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
import renderEngine.ModelCache;
import terrain.Terrain;
import textures.ModelTexture;

//class responsible for movement for all mobile entities in the game
public class EntityMovementManager {
private static List<Entity> entities = new ArrayList<Entity>();
	
	public EntityMovementManager(){
		
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
	
	public void updateEntities(Terrain terrain){
		boolean bounce = false;
		if(Keyboard.isKeyDown(Keyboard.KEY_K)){
			bounce = true;
		}
		for(Entity e:entities){
			
			if(bounce){
				e.getVelocity().y = 10.0f;
			}
			e.update(terrain);
		}
	}
	
	public void cleanUp(){
		entities.clear();
	}
}
