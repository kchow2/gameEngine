package entities;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.Loader;
import renderEngine.ModelCache;
import world.World;

public class EntityFactory {

	private World world;
	private Loader loader;
	private ModelCache modelCache;
	
	public EntityFactory(World world, Loader loader, ModelCache modelCache){
		this.world = world;
		this.loader = loader;
		this.modelCache = modelCache;
	}
	
	public Entity createEntityFromIni(String className, Vector3f position, float rotX, float rotY, float rotZ, float scale){
		
		Entity e = new Entity(world, modelCache.loadModel(className), position, rotX,rotY,rotZ, 1.0f, 1.0f, 1.0f, scale);
		e.setClassName(className);
		return e;
	}
}
