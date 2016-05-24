package engineTester;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import models.ModelData;
import models.OBJFileLoader;
import models.RawModel;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import particles.Particle;
import particles.ParticleMaster;
import physics.AABB;
import physics.CollisionManager;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.EntityManager;
import entities.Light;
import entities.MobileEntity;
import entities.MobileEntityManager;
import entities.Player;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRendering.TextMaster;
import guis.GuiRenderer;
import guis.GuiTexture;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import terrain.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class MainGameLoop {
	public static void main(String[] args){
		DisplayManager.createDisplay();
		
		Loader loader = new Loader();
		MasterRenderer renderer = new MasterRenderer(loader);
		TextMaster.init(loader);
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		
		//FontType font = new FontType(loader.loadTexture("arial"), new File("res/arial.fnt"));
		//GUIText text = new GUIText("Hello World!", 10.0f, font, new Vector2f(0.1f,0.2f), 0.5f, false);
		//text.setColour(1, 0, 1);
		
		//Light light = 
		//Light light2 = new Light(new Vector3f(200,1000,200), new Vector3f(0.0f,1,1));
		List<Light> lights = new ArrayList<Light>();
		Light sun = new Light(new Vector3f(300,100,300), new Vector3f(0.8f,0.8f,0.8f), new Vector3f(1,0,0));
		lights.add(sun);
		lights.add(new Light(new Vector3f(70,20,50), new Vector3f(1,0,0), new Vector3f(1.0f,0.01f,0.002f)));
		lights.add(new Light(new Vector3f(120,20,90), new Vector3f(0,0,1), new Vector3f(1.0f,0.01f,0.002f)));
		Light playerLight = new Light(new Vector3f(0,0,0), new Vector3f(0.7f,0.7f,0.7f),  new Vector3f(0.5f,0.005f,0.009f));
		lights.add(playerLight);
		
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grass01"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		
		TerrainTexturePack texturePack = new TerrainTexturePack(
				backgroundTexture, rTexture, gTexture, bTexture);

		Terrain terrain = new Terrain(0, 0, loader, texturePack, blendMap, "heightMap");
		List<Terrain> terrains = new ArrayList<Terrain>();
		terrains.add(terrain);
		//Terrain terrain2 = new Terrain(1,-1,loader, new ModelTexture(loader.loadTexture("stallTexture")));
		
		//Player
		ModelData modelData = OBJFileLoader.loadOBJ("cube");
		RawModel playerModelRaw = loader.loadToVAO(modelData.getVertices(), modelData.getTextureCoords(), modelData.getNormals(), modelData.getIndices());
		TexturedModel playerModel = new TexturedModel(playerModelRaw, new ModelTexture(loader.loadTexture("player")));
		Player player = new Player(playerModel, new Vector3f(100,0,100),0,135.0f,0,1.0f, modelData.getAABB());
		//CollisionManager.addBoundingBox(modelData.getAABB());
		Camera camera = new Camera(player);
		camera.setDistanceFromPlayer(20.0f);
		camera.setAngleAroundPlayer(0.0f);
		camera.setPitch(15.0f);
		
		EntityManager entityManager = new EntityManager();
		entityManager.populateWorld(loader, terrain);
		entityManager.addEntity(player);
		
		List<Entity> normalMapEntities = new ArrayList<Entity>();
		TexturedModel barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader), new ModelTexture(loader.loadTexture("barrel")));
		barrelModel.getTexture().setNormalMap(loader.loadTexture("barrelNormal"));
		barrelModel.getTexture().setShineDamper(10.0f);
		barrelModel.getTexture().setReflectivity(0.5f);
		normalMapEntities.add(new Entity(barrelModel, new Vector3f(75,10,75), 0,0,0,1f));
		
		MobileEntityManager mobileEntityManager = new MobileEntityManager();
		mobileEntityManager.populateWorld(entityManager, loader, terrain);	
		mobileEntityManager.addEntity(player);
		
		List<GuiTexture> guis = new ArrayList<GuiTexture>();
		GuiTexture targetingReticle = new GuiTexture(loader.loadTexture("target"), new Vector2f(0f,0f), new Vector2f(0.1f,0.1f));
		guis.add(targetingReticle);
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		
		MousePicker mousePicker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);

		WaterFrameBuffers fbos = new WaterFrameBuffers();
		List<WaterTile> waters = new ArrayList<WaterTile>();
		WaterShader waterShader = new  WaterShader();
		WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), fbos);
		WaterTile water = new WaterTile(100,100,-5);
		waters.add(water);
		
		while(!Display.isCloseRequested()){
			//update entities
			mobileEntityManager.updateEntities(terrain);
			
			//update player+camera
			player.move(terrain, mobileEntityManager);
			camera.move();
			
			//check for collisions
			CollisionManager.checkCollisions();
			
			//mousepicker + light following mouse
			mousePicker.update();
			
			//particles
			if(Keyboard.isKeyDown(Keyboard.KEY_Y)){
				Particle p = new Particle(new Vector3f(player.getPosition()),new Vector3f(player.getVelocity()), 1.0f, 4.0f, 0, 1 );
				p.getVelocity().y += 10.0f;
			}
			ParticleMaster.update();
			
			Vector3f mousePoint = mousePicker.getCurrentTerrainPoint();
			if(mousePoint != null){
				Vector3f terrainNormal = terrain.getTerrainNormal(mousePoint.x, mousePoint.z);
				if(terrainNormal != null){
					Vector3f lightPos = new Vector3f(mousePoint.x+3*terrainNormal.x, mousePoint.y+3*terrainNormal.y, mousePoint.z+3*terrainNormal.z);
					playerLight.setPosition(lightPos);
				}
			}
			
			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
			fbos.bindReflectionFrameBuffer();
			float distance = 2*(camera.getPosition().y - water.getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			renderer.renderScene(entityManager.getEntities(), normalMapEntities, terrains, lights, camera, new Vector4f(0,1,0,-water.getHeight()));
			camera.getPosition().y += distance;
			camera.invertPitch();
			fbos.bindRefractionFrameBuffer();
			renderer.renderScene(entityManager.getEntities(), normalMapEntities, terrains, lights, camera, new Vector4f(0,-1,0,water.getHeight()));
			fbos.unbindCurrentFrameBuffer();
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			
			renderer.renderScene(entityManager.getEntities(), normalMapEntities, terrains, lights, camera, new Vector4f(0,-1,0,999999));
			waterRenderer.render(waters,  camera, sun);
			
			MobileEntity targetedEntity = player.getTargetedEntity();
			if(targetedEntity != null){
				Vector2f targetingReticleScreenCoords = renderer.worldToScreenCoords(camera, targetedEntity.getPosition());
				if(targetingReticleScreenCoords != null){	//targeting reticle is on screen
					targetingReticle.getPosition().x = targetingReticleScreenCoords.x;
					targetingReticle.getPosition().y = targetingReticleScreenCoords.y;
					targetingReticle.show();
				}
				else{		//targeting reticle is off screen. render an arrow in the direction to turn instead
					targetingReticle.hide();
				}
			}
			else{
				targetingReticle.hide();
			}
			ParticleMaster.renderParticles(camera);
			guiRenderer.render(guis);
			TextMaster.render();
			DisplayManager.updateDisplay();
		}
		
		TextMaster.cleanUp();
		renderer.cleanUp();
		fbos.cleanUp();
		waterShader.cleanUp();
		guiRenderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}
}
