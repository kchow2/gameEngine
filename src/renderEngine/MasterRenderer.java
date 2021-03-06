package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.TexturedModel;
import normalMappingRenderer.NormalMappingRenderer;
import physics.CollisionManager;
import shaders.StaticShader;
import shaders.TerrainShader;
import shaders.WireframeShader;
import skybox.SkyboxRenderer;
import terrain.Terrain;
import toolbox.Maths;

public class MasterRenderer {
	
	private static final float FOV = 70;
	private static final float NEAR_PLANE = 1.0f;		//setting this too low will cause the depth buffer to have insufficient resolution, causing z-fighting, and flickering! See https://www.sjbaker.org/steve/omniv/love_your_z_buffer.html
	private static final float FAR_PLANE = 10000f;
	
	public static final Vector3f SKY_COLOUR = new Vector3f(0.7f, 0.7f, 0.7f);
	
	private StaticShader shader = new StaticShader();
	private EntityRenderer renderer;
	private NormalMappingRenderer normalMappingRenderer;
	private Matrix4f projectionMatrix;
	private Map<TexturedModel, List<Entity>> entities = new HashMap<TexturedModel, List<Entity>>();
	private Map<TexturedModel, List<Entity>> normalMapEntities = new HashMap<TexturedModel, List<Entity>>();
	private List<Terrain> terrains = new ArrayList<Terrain>();
	private TerrainRenderer terrainRenderer;
	private TerrainShader terrainShader = new TerrainShader();
	private WireframeShader wireframeShader = new WireframeShader();
	private SkyboxRenderer skyboxRenderer;
	private DebugBoundingBoxRenderer debugBoundingBoxRenderer;
	
	public MasterRenderer(Loader loader){
		enableCulling();
		createProjectionMatrix();
		renderer = new EntityRenderer(shader, projectionMatrix);
		normalMappingRenderer = new NormalMappingRenderer(projectionMatrix);
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
		debugBoundingBoxRenderer = new DebugBoundingBoxRenderer(loader, wireframeShader, projectionMatrix);
	}
	
	public static void enableCulling(){
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}
	
	public static void disableCulling(){
		GL11.glDisable(GL11.GL_CULL_FACE);
	}
	
	public void renderScene(List<Entity> entities, List<Entity> normalMapEntities, List<Terrain> terrains, List<Light> lights, Camera camera, Vector4f clipPlane){
		for(Terrain terrain:terrains){
			processTerrain(terrain);
		}
		for(Entity entity:entities){
			processEntity(entity);
		}
		for(Entity entity:normalMapEntities){
			processNormalMapEntity(entity);
		}
		render(lights,camera, clipPlane);
	}
	
	public void prepare(){
		GL11.glClearColor(SKY_COLOUR.x, SKY_COLOUR.y, SKY_COLOUR.z, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_DEPTH_BUFFER_BIT);
	}
	
	public void render(List<Light> lights, Camera camera, Vector4f clipPlane){
		prepare();
		//entities
		shader.start();
		shader.loadClippingPlane(clipPlane);
		shader.loadLights(lights);
		shader.loadSkyColour(SKY_COLOUR);
		shader.loadViewMatrix(camera);
		renderer.render(entities);
		shader.stop();
		
		//wireframeShader.start();
		//wireframeShader.loadSkyColour(SKY_COLOUR);
		//wireframeShader.loadViewMatrix(camera);
		//debugBoundingBoxRenderer.renderAABBs(entities, normalMapEntities);
		//wireframeShader.stop();
		
		normalMappingRenderer.render(normalMapEntities, clipPlane, lights, camera);
		
		
		//Terrain
		//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		terrainShader.start();
		terrainShader.loadClippingPlane(clipPlane);
		terrainShader.loadLights(lights);
		terrainShader.loadSkyColour(SKY_COLOUR);
		terrainShader.loadViewMatrix(camera);
		terrainRenderer.render(terrains);
		terrainShader.stop();
		
		skyboxRenderer.render(camera, SKY_COLOUR);
		
		entities.clear();
		terrains.clear();
		normalMapEntities.clear();
		//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL );
		
		
	}
	
	public void renderDebugInfo(Camera camera, List<CollisionManager.CollisionEntry> collisionObjects){
		wireframeShader.start();
		wireframeShader.loadSkyColour(SKY_COLOUR);
		wireframeShader.loadViewMatrix(camera);
		debugBoundingBoxRenderer.renderAABBs(collisionObjects);
		wireframeShader.stop();
	}
	
	public void processTerrain(Terrain terrain){
		terrains.add(terrain);
	}
	
	public void processEntity(Entity entity){
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = entities.get(entityModel);
		if(batch != null){
			batch.add(entity);
		}
		else{
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}
	
	public void processNormalMapEntity(Entity entity){
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = normalMapEntities.get(entityModel);
		if(batch != null){
			batch.add(entity);
		}
		else{
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			normalMapEntities.put(entityModel, newBatch);
		}
	}
	
	public void cleanUp(){
		shader.cleanUp();
		terrainShader.cleanUp();
		normalMappingRenderer.cleanUp();
	}
	
	public Matrix4f getProjectionMatrix(){
		return projectionMatrix;
	}

	public void createProjectionMatrix(){
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV/2f))) * aspectRatio);
        float x_scale = y_scale / aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;
        
        projectionMatrix = new Matrix4f();
        projectionMatrix.setIdentity();
        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        projectionMatrix.m33 = 0;
	}
	
	public Vector2f worldToScreenCoords(Camera camera, Vector3f worldPos){
		Vector4f worldPos4 = new Vector4f(worldPos.x, worldPos.y, worldPos.z, 1.0f);
		Matrix4f viewMat = Maths.createViewMatrix(camera);
		Matrix4f projViewMat = new Matrix4f();
		Matrix4f.mul(this.projectionMatrix, viewMat, projViewMat);
		Vector4f result = new Vector4f();
		Matrix4f.transform(projViewMat, worldPos4, result);
		
		boolean isInFrontOfCamera = result.z > 0;
		result.scale(1.0f / result.w);	//divide by w to normalize the vector
		if(result.x >= -1 && result.x <= 1f && result.y >= -1f && result.y <= 1f && isInFrontOfCamera){
			
			return new Vector2f(result.x, result.y);
		}
		else{
			return null;
		}
	}
	
	public TerrainRenderer getTerrainRenderer(){
		return this.terrainRenderer;
	}
	
}
