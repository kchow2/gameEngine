package engineTester;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Camera.CameraMode;
import entities.Entity;
import entities.HoverCraftComponent;
import entities.Light;
import entities.Player;
import entities.WeaponComponent;
import fontMeshCreator.FontType;
import fontMeshCreator.GuiText;
import fontRendering.FontRenderer;
import fontRendering.TextMaster;
import guis.GuiMaster;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.DAEFileLoader;
import models.ModelData;
import models.TexturedModel;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import toolbox.KeyboardHelper;
import toolbox.MouseHelper;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;
import world.World;

public class MainGameLoop {
	public static void main(String[] args){	
		
		DisplayManager.createDisplay();
		Loader loader = new Loader();
		MasterRenderer renderer = new MasterRenderer(loader);
		World world = new World(loader, renderer);
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		
		FontType font = null;
		try{
			font = new FontType(loader.loadTexture("arial"), new File("res/arial.fnt"));
			
		} catch(IOException e){
			System.err.println("Failed to load res/arial.fnt");
		}
		
		List<Light> lights = new ArrayList<Light>();
		Light sun = new Light(new Vector3f(300,100,300), new Vector3f(0.8f,0.8f,0.8f), new Vector3f(1,0,0));
		lights.add(sun);
		lights.add(new Light(new Vector3f(70,20,50), new Vector3f(1,0,0), new Vector3f(1.0f,0.01f,0.002f)));
		lights.add(new Light(new Vector3f(120,20,90), new Vector3f(0,0,1), new Vector3f(1.0f,0.01f,0.002f)));
		Light playerLight = new Light(new Vector3f(0,0,0), new Vector3f(0.7f,0.7f,0.7f),  new Vector3f(0.5f,0.005f,0.009f));
		lights.add(playerLight);
		
		Entity testEntity = null;
		DAEFileLoader modelLoader = new DAEFileLoader();
		ModelData modelData = modelLoader.load("res/tank.dae");
		if(modelData != null){
			TexturedModel texturedModel = world.modelCache.dbg_loadRawModelData("tank", modelData);
			if(texturedModel != null){
				testEntity = world.createEntity("tank", new Vector3f(150,0,150), true);
			} else{
				System.err.println("texturedModel was null!");
			}
		} else{
			System.err.println("DAE FILE FAILED!");
		}
		
		Entity entityPlayer = testEntity;//world.createEntity("tank", new Vector3f(100,0,100), true);
		entityPlayer.addComponent(new HoverCraftComponent(entityPlayer));
		TexturedModel projectileModel = world.modelCache.loadModel("shell");
		TexturedModel explosionModel = world.modelCache.loadModel("sphere");
		try{
			entityPlayer.addComponent(new WeaponComponent(projectileModel,explosionModel,new ParticleTexture(loader.loadTexture("fire_particle"), 8, false), 0.65f, 100.0f, 1.25f));
		}catch(IOException e){System.out.println("failed to load particle texture");}
		
		world.populateEntities();
		
		//Camera
		Camera camera = new Camera(entityPlayer, world);
		camera.setCameraMode(CameraMode.THIRD_PERSON);
		camera.setThirdPersonDistance(20.0f);
		camera.setThirdPersonYaw(0.0f);
		camera.setThirdPersonPitch(15.0f);
		
		//Player 
		Player player = new Player(world, entityPlayer, camera);
		
		List<GuiTexture> guis = new ArrayList<GuiTexture>();
		//GuiTexture targetingReticle = new GuiTexture(loader.loadTexture("target"), new Vector2f(0f,0f), new Vector2f(0.1f,0.1f));
		//guis.add(targetingReticle);
		//targetingReticle.hide();
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		GuiMaster.init(loader);
		
		FontRenderer fontRenderer = new FontRenderer();
		TextMaster.init(loader, fontRenderer);
		
		//GuiText fpsCounterText = new GuiText(loader, "0", 2.0f, font, new Vector2f(0.01f, 0.0f), 0.5f, false);
		//TextMaster.addText(fpsCounterText);
		//fpsCounterText.setColour(1, 1, 0);
		
		
		//WorldEditorGui worldEditorGui = new WorldEditorGui(loader);
		//worldEditorGui.setVisible(true);
		
		MousePicker mousePicker = new MousePicker(camera, renderer.getProjectionMatrix(), world.getTerrain());

		WaterFrameBuffers fbos = new WaterFrameBuffers();
		List<WaterTile> waters = new ArrayList<WaterTile>();
		WaterShader waterShader = new  WaterShader();
		WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), fbos);
		WaterTile water = new WaterTile(100,100,-5);
		waters.add(water);
		
		ParticleTexture texture = null;
		ParticleSystem particleSystem = null;
		ParticleTexture texture2 = null;
		ParticleSystem particleSystem2 = null;
		try{
			texture = new ParticleTexture(loader.loadTexture("smoke1"), 2, false);
			particleSystem = new ParticleSystem(texture, 100.0f, 5.0f, 0.1f, 2.0f, 2.0f);
			texture2 = new ParticleTexture(loader.loadTexture("spark1"), 2, false);
			particleSystem2 = new ParticleSystem(texture2, 100.0f, 15.0f, 0.1f, 0.75f, 0.5f);
		}catch (IOException e){
			
		}
		int fps = 0, prevFps = 0;
		
		while(!Display.isCloseRequested()){
			
			//update mouse + keyboard
			MouseHelper.update();
			KeyboardHelper.update();
			
			//update player
			player.checkInputs();
			
			
			//update world + entities
			world.update();
			
			//mousepicker + light following mouse
			mousePicker.update();
			camera.move();
			
			//particles
			//if(Keyboard.isKeyDown(Keyboard.KEY_Y)){
			//	Particle p = new Particle(new Vector3f(player.getPosition()),new Vector3f(player.getVelocity()), 1.0f, 4.0f, 0, 1 );
			//	p.getVelocity().y += 10.0f;
			//}
			ParticleMaster.update(camera);
			if(testEntity != null){
				//Vector3f pos = entityPlayer.getHardpointWorldPos("HP_CANNON");
				//if(pos != null)
				//	particleSystem.generateParticles(pos);
				//Vector3f pos2 = entityPlayer.getHardpointWorldPos("HP_CANNON");
				//if(pos2 != null)
				//	particleSystem2.generateParticles(pos2);
			}
			
			Vector3f mousePoint = mousePicker.getCurrentTerrainPoint();
			if(mousePoint != null){
				Vector3f terrainNormal = world.getTerrain().getTerrainNormal(mousePoint.x, mousePoint.z);
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
			renderer.renderScene(world.getEntities(), world.getNormalMapEntities(), world.getTerrains(), lights, camera, new Vector4f(0,1,0,-water.getHeight()));
			camera.getPosition().y += distance;
			camera.invertPitch();
			fbos.bindRefractionFrameBuffer();
			renderer.renderScene(world.getEntities(), world.getNormalMapEntities(), world.getTerrains(), lights, camera, new Vector4f(0,-1,0,water.getHeight()));
			fbos.unbindCurrentFrameBuffer();
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			
			renderer.renderScene(world.getEntities(), world.getNormalMapEntities(), world.getTerrains(), lights, camera, new Vector4f(0,-1,0,999999));
			waterRenderer.render(waters,  camera, sun);
			
			//render extra debug info
			renderer.renderDebugInfo(camera, world.collisionManager.getCollisionObjects());
			
			/*Entity targetedEntity = player.getTargetedEntity();
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
			}*/
			
			//fps counter
			fps = DisplayManager.getFps();
			if(fps != prevFps){
				//TextMaster.removeText(fpsCounterText);
				//fpsCounterText = new GuiText(loader, String.valueOf(fps), 2.0f, font, new Vector2f(0.01f, 0.0f), 0.5f, false);
				//TextMaster.addText(fpsCounterText);
				//fpsCounterText.setColour(1, 1, 0);
				prevFps = fps;
			}
			
			ParticleMaster.renderParticles(camera);
			GuiMaster.render(guiRenderer);
			guiRenderer.render(guis);
			GuiMaster.renderTexts(fontRenderer);
			TextMaster.render();
			DisplayManager.updateDisplay();
		}
		
		TextMaster.cleanUp();
		world.cleanUp();
		renderer.cleanUp();
		fbos.cleanUp();
		waterShader.cleanUp();
		guiRenderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}
	
	private static void testINI(){
		System.out.println("Reading test.ini...");
		try{
			Ini ini = new Ini(new File("res/tank.ini"));
			for(Entry<String, Section> e : ini.entrySet()){
				String sectionName = e.getKey();
				Section section = e.getValue();
				System.out.println("sectionName="+sectionName);
				for(Entry<String, String> ent : section.entrySet()){
					System.out.println(ent.getKey() + " : " + ent.getValue());
				}
				
				
			}
		}
		catch(IOException e){
			System.out.println("Can't open the ini file!");
		}
	}
}
