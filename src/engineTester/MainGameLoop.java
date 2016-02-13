package engineTester;

import java.util.ArrayList;
import java.util.List;

import models.ModelData;
import models.OBJFileLoader;
import models.RawModel;
import models.TexturedModel;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import entities.Camera;
import guis.GuiRenderer;
import guis.GuiTexture;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.EntityRenderer;
import shaders.StaticShader;
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
		//Light light = 
		//Light light2 = new Light(new Vector3f(200,1000,200), new Vector3f(0.0f,1,1));
		List<Light> lights = new ArrayList<Light>();
		lights.add(new Light(new Vector3f(100,1000,100), new Vector3f(0.4f,0.4f,0.4f), new Vector3f(1,0,0)));
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
		ModelData modelData = OBJFileLoader.loadOBJ("player");
		RawModel playerModelRaw = loader.loadToVAO(modelData.getVertices(), modelData.getTextureCoords(), modelData.getNormals(), modelData.getIndices());
		TexturedModel playerModel = new TexturedModel(playerModelRaw, new ModelTexture(loader.loadTexture("player")));
		Player player = new Player(playerModel, new Vector3f(100,0,100),0,135.0f,0,1.0f);
		Camera camera = new Camera(player);
		camera.setDistanceFromPlayer(20.0f);
		camera.setAngleAroundPlayer(0.0f);
		camera.setPitch(15.0f);
		
		EntityManager entityManager = new EntityManager();
		entityManager.populateWorld(loader, terrain);
		entityManager.addEntity(player);
		
		List<GuiTexture> guis = new ArrayList<GuiTexture>();
		GuiTexture gui = new GuiTexture(loader.loadTexture("gui"), new Vector2f(0.5f,0.5f), new Vector2f(0.25f,0.25f));
		guis.add(gui);
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		
		MousePicker mousePicker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);

		WaterFrameBuffers fbos = new WaterFrameBuffers();
		List<WaterTile> waters = new ArrayList<WaterTile>();
		WaterShader waterShader = new  WaterShader();
		WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), fbos);
		WaterTile water = new WaterTile(100,100,-5);
		waters.add(water);
		
		while(!Display.isCloseRequested()){
			
			camera.move();
			player.move(terrain);
			
			//mousepicker + light following mouse
			mousePicker.update();
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
			renderer.renderScene(entityManager.getEntities(), terrains, lights, camera, new Vector4f(0,1,0,-water.getHeight()));
			camera.getPosition().y += distance;
			camera.invertPitch();
			fbos.bindRefractionFrameBuffer();
			renderer.renderScene(entityManager.getEntities(), terrains, lights, camera, new Vector4f(0,-1,0,water.getHeight()));
			fbos.unbindCurrentFrameBuffer();
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			
			renderer.renderScene(entityManager.getEntities(), terrains, lights, camera, new Vector4f(0,-1,0,999999));
			waterRenderer.render(waters,  camera);
			guiRenderer.render(guis);
			DisplayManager.updateDisplay();
		}
		
		renderer.cleanUp();
		fbos.cleanUp();
		waterShader.cleanUp();
		guiRenderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}
}
