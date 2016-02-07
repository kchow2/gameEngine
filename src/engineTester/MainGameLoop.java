package engineTester;

import models.RawModel;
import models.TexturedModel;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.ObjLoader;
import renderEngine.Renderer;
import shaders.StaticShader;
import textures.ModelTexture;

public class MainGameLoop {
	public static void main(String[] args){
		DisplayManager.createDisplay();
		
		Loader loader = new Loader();
		StaticShader shader = new StaticShader();
		Renderer renderer = new Renderer(shader);
		
		/*float vertices[] = {
			-0.5f, 0.5f, 0,
			-0.5f, -0.5f, 0,
			0.5f, -0.5f, 0,
			0.5f, 0.5f, 0f
		};
		int indices[] = {
			0,1,3,
			3,1,2
		};
		float textureCoords[] = {
			0,0,
			0,1,
			1,1,
			1,0
		};*/
		//RawModel model = loader.loadToVAO(vertices, textureCoords, indices);
		RawModel model = ObjLoader.loadObjModel("stall", loader) ;
		ModelTexture texture = new ModelTexture(loader.loadTexture("stallTexture"));
		texture.setReflectivity(0.2f);
		texture.setShineDamper(10.0f);
		TexturedModel texturedModel = new TexturedModel(model, texture);
		Entity entity = new Entity(texturedModel, new Vector3f(0,0,-25),0,0,0,1);
		Light light = new Light(new Vector3f(0,2,-10), new Vector3f(1,1,1));
		
		Camera camera = new Camera();
		
		//int frameCount = 0;
		//long prevTime = System.currentTimeMillis();
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
			renderer.prepare();
			shader.start();
			shader.loadLight(light);
			shader.loadViewMatrix(camera);
			renderer.render(entity, shader);
			shader.stop();
			DisplayManager.updateDisplay();
		}
		shader.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}
}
