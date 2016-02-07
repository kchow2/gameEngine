package engineTester;

import models.OBJFileLoader;
import models.RawModel;
import models.TexturedModel;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.EntityRenderer;
import shaders.StaticShader;
import terrain.Terrain;
import textures.ModelTexture;

public class MainGameLoop {
	public static void main(String[] args){
		DisplayManager.createDisplay();
		
		Loader loader = new Loader();
		Light light = new Light(new Vector3f(10,20,10), new Vector3f(1,1,1));
		
		Terrain terrain = new Terrain(0,0,loader, new ModelTexture(loader.loadTexture("grass01")));
		//Terrain terrain2 = new Terrain(1,-1,loader, new ModelTexture(loader.loadTexture("stallTexture")));
		
		Camera camera = new Camera();
		camera.getPosition().x = 1;
		camera.getPosition().y = 1;
		camera.getPosition().z = 1;
		camera.setYaw(150.0f);
		
		EntityManager entityManager = new EntityManager();
		entityManager.populateWorld(loader);
		
		//int frameCount = 0;
		//long prevTime = System.currentTimeMillis();
		MasterRenderer renderer = new MasterRenderer();
		while(!Display.isCloseRequested()){
			//entity.increasePosition(0,0,-0.01f);
			//entity.increaseRotation(0,0.02f,0);
			/*frameCount++;
			long currentTime = System.currentTimeMillis();
			if(currentTime - prevTime >= 1000){
				System.out.println("FPS: "+frameCount);
				frameCount = 0;
				prevTime = currentTime;
			}*/
			
			
			camera.move();
			renderer.processTerrain(terrain);
			entityManager.renderAllEntities(renderer);
			renderer.render(light, camera);
			DisplayManager.updateDisplay();
		}
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}
}
