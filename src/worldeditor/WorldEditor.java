package worldeditor;

import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Camera.CameraMode;
import entities.Player;
import renderEngine.DisplayManager;
import renderEngine.MasterRenderer;
import terrain.Terrain;
import world.World;
import worldeditor.TerrainBrush.BrushMode;
import worldeditor.TerrainBrush.BrushShape;

public class WorldEditor{
	private boolean isEditorOpen = false;
	
	private World world;
	private Camera camera;
	private Player player;
	
	private CameraMode oldCameraMode;
	
	private MasterRenderer masterRenderer;
	private TerrainBrush terrainBrush;
	
	public WorldEditor(World world, Camera camera, Player player, MasterRenderer masterRenderer){
		this.world = world;
		this.camera = camera;
		this.player = player;	
		//this.mousePicker = new MousePicker(camera, masterRenderer.getProjectionMatrix(), world.getTerrain());
		this.masterRenderer = masterRenderer;
		this.terrainBrush = new TerrainBrush(10, BrushMode.RAISE, BrushShape.CIRCLE, 5, 10.0f);
	}
	
	public boolean isEditorOpen(){
		return this.isEditorOpen;
	}
	
	
	public void beginEditing(){
		if(!isEditorOpen){
			oldCameraMode = camera.getCameraMode();
			camera.setCameraMode(CameraMode.OVERHEAD);		//we probably want to be in overhead view while editing
			player.disableControls();
		}
		isEditorOpen = true;
	}
	
	public void update(){
		//mousePicker.update();
		//if(Mouse.isButtonDown(1)){
		//	Vector3f mouseWorldCoords = mousePicker.getCurrentTerrainPoint();
		//	terrainBrush.applyBrush(world.getTerrain(), mouseWorldCoords.x, mouseWorldCoords.z, DisplayManager.getFrameTimeSeconds());
		//}
	}
	
	public void renderOverlay(){
		//TODO: implement GUI system for editor controls
	}
	
	public Terrain getTerrain(){
		return world.getTerrain();
	}
	
	public void endEditing(){
		if(isEditorOpen){
			camera.setCameraMode(oldCameraMode);	//restore old camera mode
			player.enableControls();
			masterRenderer.getTerrainRenderer().setWireframeMode(false);
		}
		isEditorOpen = false;
	}
	
	public void placeObject(Vector3f position){
		Vector3f placePosition = new Vector3f(position);	//mousePicker.getCurrentTerrainPoint()
		//world.createEntity2("tank", placePosition, 0, 0, 0, 1.0f);
		world.createEntity("tank", placePosition, true);
	}
	
	//public void placeObject(int screenX, int screenY){
	//	Vector3f placePosition = new Vector3f(mousePicker.getCurrentTerrainPoint());
	//	//world.createEntity2("tank", placePosition, 0, 0, 0, 1.0f);
	//	world.createEntity("tank", placePosition, true);
	//}
	
	public void toggleWireframeMode(){
		masterRenderer.getTerrainRenderer().toggleWireframeMode();
	}
	
	public void applyTerrainBrush(Vector3f worldCoords){
		terrainBrush.applyBrush(world.getTerrain(), worldCoords.x, worldCoords.z, DisplayManager.getFrameTimeSeconds());
	}
	
}
