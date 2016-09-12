package world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import entities.Entity;
import entities.EntityMovementManager;
import entities.EntityRenderingManager;
import entities.ProjectileManager;
import models.ModelData;
import models.OBJFileLoader;
import models.RawModel;
import models.TexturedModel;
import physics.CollisionManager;
import renderEngine.Loader;
import renderEngine.ModelCache;
import terrain.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;

public class World {
	//Terrain terrain;
	Loader loader;
	Terrain terrain;
	List<Terrain> terrains = new ArrayList<Terrain>();
	public ModelCache modelCache;
	EntityRenderingManager entityManager = new EntityRenderingManager();
	EntityMovementManager mobileEntityManager = new EntityMovementManager();
	ProjectileManager projectileManager = new ProjectileManager(entityManager,mobileEntityManager );
	CollisionManager collisionManager = new CollisionManager();
	List<Entity> normalMapEntities = new ArrayList<Entity>();
	
	public World(Loader loader){
		this.loader = loader;
		this.terrain = loadTerrain();
		terrains.add(terrain);
		this.modelCache = new ModelCache(loader);
	}
	
	public Terrain getTerrain(){
		return terrain;
	}
	
	private Terrain loadTerrain(){
		
		try{
			TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grass01"));
			TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
			TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
			TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
			TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
			TerrainTexturePack texturePack = new TerrainTexturePack(
					backgroundTexture, rTexture, gTexture, bTexture);
			
			return new Terrain(0, 0, loader, texturePack, blendMap, "heightMap");
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public void cleanUp(){
		entityManager.cleanUp();
		mobileEntityManager.cleanUp();
		projectileManager.cleanUp();
	}
	
	public void update(){
		mobileEntityManager.updateEntities(terrain);
		projectileManager.update(terrain);
		collisionManager.checkCollisions();
	}
	
	public List<Entity> getEntities(){
		return entityManager.getEntities();
	}
	
	public List<Entity> getNormalMapEntities(){
		return normalMapEntities;
	}
	
	public List<Terrain> getTerrains(){
		return terrains;
	}
	
	public Entity createEntity(String entityName, Vector3f position, boolean needsUpdates){
		TexturedModel model = modelCache.loadModel(entityName);
		Entity entity = new Entity(model, position, 0, 0, 0, 1.0f );
		entityManager.addEntity(entity);
		if(needsUpdates){
			mobileEntityManager.addEntity(entity);
			collisionManager.addCollisionDetection(entity, modelCache.getAABB(entityName));
		}
		return entity;
	}
	
	public void hideEntity(Entity e){
		entityManager.hideEntity(e);
	}
	
	public void showEntity(Entity e){
		entityManager.showEntity(e);
	}
	
	public void populateEntities(){
		
		this.populatePlants(modelCache, terrain);
		this.populateCubes(modelCache, terrain);
		System.out.println("Done populating world.");
		System.out.println("Entity count: "+entityManager.getEntities().size());
		System.out.println("Collision object count: "+collisionManager.getObjectCount());
		System.out.println("Tank AABB="+modelCache.getAABB("tank"));
		System.out.println("Box AABB="+modelCache.getAABB("cube"));
	}
	
	
	//populates the world with a bunch of random plants like trees, ferns, etc
	public void populatePlants(ModelCache modelCache, Terrain terrain){
		final int NUM_FERNS = 100;
		final int NUM_TREES = 50;
		final int NUM_GRASS = 100;
		
		final float XMIN = 0;
		final float ZMIN = 0;
		final float MAPSIZE = 800;
		
		Random rand = new Random();
		
		//FERNS
		TexturedModel modelFern = modelCache.loadModel("fern");
		ModelTexture fernAtlas = modelFern.getTexture();
		fernAtlas.setTransparent(true);
		fernAtlas.setUseFakeLighting(true);
		fernAtlas.setNumberOfRows(2);
		
		for(int i = 0; i < NUM_FERNS; i++){
			Entity e = this.createEntity("fern", getRandomPosition(XMIN,ZMIN,MAPSIZE,terrain), false );
			e.setTextureOffset(rand.nextInt()%4);
		}
		
		//TREES
		for(int i = 0; i < NUM_TREES; i++){
			this.createEntity("tree", getRandomPosition(XMIN,ZMIN,MAPSIZE,terrain), false );
		}
		
		//GRASS
		for(int i = 0; i < NUM_GRASS; i++){
			this.createEntity("grassModel", getRandomPosition(XMIN,ZMIN,MAPSIZE,terrain), false );
		}
	}
	
	//populates the world with collidable cubes
	public void populateCubes(ModelCache modelCache, Terrain terrain){
		final int NUM_BOXES = 1;
		final float XMIN = 0;
		final float ZMIN = 0;
		final float MAPSIZE = 800;
		
		for(int i = 0; i < NUM_BOXES; i++){
			this.createEntity("cube", new Vector3f(100,terrain.getTerrainHeight(105, 100),105) , true);	//this.getRandomPosition(XMIN, ZMIN, MAPSIZE, terrain)
		}
	}
	
	private Vector3f getRandomPosition(float xMin, float zMin, float mapSize, Terrain terrain){
		Random r = new Random();
		float x = (float)(r.nextDouble()*mapSize+xMin);
		float z = (float)(r.nextDouble()*mapSize+zMin);
		return new Vector3f(x, terrain.getTerrainHeight(x, z), z);
	}
	
}
