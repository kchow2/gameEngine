package world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.EntityFactory;
import entities.EntityMovementManager;
import entities.EntityRenderingManager;
import entities.Explosion;
import entities.Player;
import entities.ProjectileManager;
import guis.GuiMaster;
import models.Hardpoint;
import models.TexturedModel;
import physics.AABB;
import physics.CollisionManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.ModelCache;
import terrain.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import worldeditor.WorldEditor;

public class World {
	private final float GRAVITY = 20.0f;
	
	Loader loader;
	Terrain terrain;
	List<Terrain> terrains = new ArrayList<Terrain>();
	public ModelCache modelCache;
	public EntityFactory entityFactory;
	EntityRenderingManager entityManager = new EntityRenderingManager();
	EntityMovementManager mobileEntityManager = new EntityMovementManager();
	ProjectileManager projectileManager = new ProjectileManager(entityManager,mobileEntityManager );
	public CollisionManager collisionManager = new CollisionManager();
	List<Entity> normalMapEntities = new ArrayList<Entity>();
	
	List<Explosion> explosionsToAdd = new ArrayList<Explosion>(); 
	
	private MasterRenderer masterRenderer;
	WorldEditor worldEditor;
	private boolean isWorldEditorOpen = false;
	
	public World(Loader loader, MasterRenderer masterRenderer){
		this.loader = loader;
		this.terrain = loadTerrain();
		terrains.add(terrain);
		this.modelCache = new ModelCache(loader);
		this.entityFactory = new EntityFactory(this, loader, modelCache);
		this.masterRenderer = masterRenderer;
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
	
	public float getGravity(){
		return GRAVITY;
	}
	
	public void cleanUp(){
		entityManager.cleanUp();
		mobileEntityManager.cleanUp();
		projectileManager.cleanUp();
	}
	
	public void update(){
		explosionsToAdd.clear();
		
		entityManager.update();
		mobileEntityManager.updateEntities(terrain);
		projectileManager.update(terrain);
		collisionManager.checkCollisions();
		
		//terrain.testDynamicTerrainUpdate(DisplayManager.getFrameTimeSeconds());
		
		for(Explosion e:explosionsToAdd){
			addExplosion(e);
		}
		
		if(this.worldEditor != null && this.worldEditor.isEditorOpen()){
			this.worldEditor.update();
		}
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
		AABB aabb = new AABB(modelCache.getAABB(entityName));
		//float modelSize = Math.max(aabb.x2-aabb.x1, aabb.z2-aabb.z1);
		Entity entity = new Entity(this, model, position, 0, 0, 0, 1.0f, 1.0f, 1.0f, 1.0f);
		entityManager.addEntity(entity);
		if(needsUpdates){
			mobileEntityManager.addEntity(entity);
			collisionManager.addCollisionDetection(entity, aabb);
		}
		
		for( Hardpoint hardpoint:modelCache.getModelData(entityName).getHardpoints()){
			entity.addHardpoint(hardpoint);
		}
		return entity;
	}
	
	public Entity createEntity2(String className, Vector3f position, float rotX, float rotY, float rotZ, float scale){
		Entity entity = entityFactory.createEntityFromIni(className, position, rotX, rotY, rotZ, scale);
		entityManager.addEntity(entity);
		if(entity.needsUpdates()){
			mobileEntityManager.addEntity(entity);
		}
		if(entity.canCollide()){
			AABB aabb = new AABB(modelCache.getAABB(className));
			collisionManager.addCollisionDetection(entity, aabb);
		}
		return entity;
	}
	
	public void spawnExplosion(Explosion e){
		explosionsToAdd.add(e);
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
	
	private void addExplosion(Explosion e){
		entityManager.addEntity(e);
		mobileEntityManager.addEntity(e);
	}
	
	
	public void toggleWorldEditor(Camera camera, Player player){
		if(!this.isWorldEditorOpen){
			if(this.worldEditor == null){
				this.worldEditor = new WorldEditor(this, camera, player, masterRenderer);
			}
			this.openWorldEditor(camera, player);
		}
		else{
			this.closeWorldEditor();
		}
	}
	
	private void openWorldEditor(Camera camera, Player player){
		GuiMaster.showWorldEditorGui(this.worldEditor, this.masterRenderer, this.loader, camera, player);
		this.worldEditor.beginEditing();
		this.isWorldEditorOpen = true;
	}
	
	private void closeWorldEditor(){
		GuiMaster.hideWorldEditorGui();
		this.worldEditor.endEditing();
		this.isWorldEditorOpen = false;
	}
	
}
