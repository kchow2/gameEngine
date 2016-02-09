package engineTester;

import java.util.ArrayList;
import java.util.List;

import models.ModelData;
import models.OBJFileLoader;
import models.RawModel;
import models.TexturedModel;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

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

public class MainGameLoop {
	public static void main(String[] args){
		DisplayManager.createDisplay();
		
		Loader loader = new Loader();
		//Light light = new Light(new Vector3f(100,1000,100), new Vector3f(1,1,1));
		//Light light2 = new Light(new Vector3f(200,1000,200), new Vector3f(0.0f,1,1));
		List<Light> lights = new ArrayList<Light>();
		//lights.add(light);
		//lights.add(light2);
		
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grass01"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		
		TerrainTexturePack texturePack = new TerrainTexturePack(
				backgroundTexture, rTexture, gTexture, bTexture);

		Terrain terrain = new Terrain(0, 0, loader, texturePack, blendMap, "heightMap");
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
		
		List<GuiTexture> guis = new ArrayList<GuiTexture>();
		GuiTexture gui = new GuiTexture(loader.loadTexture("gui"), new Vector2f(0.5f,0.5f), new Vector2f(0.25f,0.25f));
		guis.add(gui);
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		
		MasterRenderer renderer = new MasterRenderer();
		while(!Display.isCloseRequested()){
			
			camera.move();
			player.move(terrain);
			renderer.processTerrain(terrain);
			renderer.processEntity(player);
			entityManager.renderAllEntities(renderer);
			renderer.render(lights, camera);
			guiRenderer.render(guis);
			DisplayManager.updateDisplay();
		}
		renderer.cleanUp();
		guiRenderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}
}
