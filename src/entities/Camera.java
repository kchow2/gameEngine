package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.DisplayManager;
import terrain.Terrain;
import toolbox.KeyboardEventListener;
import toolbox.KeyboardHelper;
import toolbox.MouseHelper;
import world.World;

public class Camera implements KeyboardEventListener{
	
	public enum CameraMode{THIRD_PERSON, FIRST_PERSON, OVERHEAD};
	
	CameraMode cameraMode = CameraMode.THIRD_PERSON;
	
	private static final float OVERHEAD_CAMERA_MOVE_SPEED = 45.0f;
	private static final float DEFAULT_OVERHEAD_CAMERA_DISTANCE = 65.0f;	//the default height of the camera above the terrain

	//third person camera settings
	private float thirdPersonDistance = 50.0f;
	private float thirdPersonYaw = 0.0f;
	private float thirdPersonPitch = 30.0f;
	
	//overhead camera settings
	private float overheadCameraHeight = DEFAULT_OVERHEAD_CAMERA_DISTANCE;
	private Vector3f overheadCameraTarget = new Vector3f();
	
	//these fields are calculated depending on camera mode and player pos
	private Vector3f position = new Vector3f(0,0,0);
	private float pitch, yaw;
	
	private Entity player;
	private World world;
	
	public Camera(Entity player, World world){
		this.player = player;
		this.world = world;
		KeyboardHelper.addListener(this);
	}
	
	public void move(){
		if(cameraMode == CameraMode.OVERHEAD){
			updateOverheadCameraPos(world.getTerrain());
			calculateZoom();

			calculateCameraPosition(overheadCameraTarget, overheadCameraHeight, 0, 70, 0);
		}
		else if(cameraMode == CameraMode.FIRST_PERSON){
			Vector3f viewPos = player.getHardpointWorldPos("HP_VIEW");
			if(viewPos == null)
				viewPos = player.getPosition();
			
			this.position.x = viewPos.x;
			this.position.y = viewPos.y;
			this.position.z = viewPos.z;
			this.pitch = player.rotX;
			this.yaw = 180.0f - player.rotY;
		}
		else if(cameraMode == CameraMode.THIRD_PERSON){
			updateThirdPersonCameraPos(world.getTerrain());
			calculateZoom();
			
			calculateCameraPosition(player.getPosition(), thirdPersonDistance, player.getRotY() + thirdPersonYaw, thirdPersonPitch, 1.75f);
		}
	}
	
	public CameraMode getCameraMode(){
		return this.cameraMode;
	}
	
	public void setCameraMode(CameraMode mode){
		this.cameraMode = mode;
		//if the view mode is first person, we need to prevent the player model from being rendered.
		if(mode == CameraMode.FIRST_PERSON)
			world.hideEntity(player);
		else
			world.showEntity(player);
		
		if(mode == CameraMode.OVERHEAD){
			this.overheadCameraTarget.x = player.getPosition().x;
			this.overheadCameraTarget.y = player.getPosition().y;
			this.overheadCameraTarget.z = player.getPosition().z;
		}
	}
	
	public void cycleCameraMode(){
		if(cameraMode == CameraMode.THIRD_PERSON)
			setCameraMode(CameraMode.FIRST_PERSON);
		else if(cameraMode == CameraMode.FIRST_PERSON)
			setCameraMode(CameraMode.OVERHEAD);
		else if(cameraMode == CameraMode.OVERHEAD)
			setCameraMode(CameraMode.THIRD_PERSON);
	}

	public Vector3f getPosition() {
		return position;
	}
	
	private void calculateCameraPosition(Vector3f cameraTarget, float distance, float theta, float pitch, float vOffset){
		float horizontalDistance = (float)(distance * Math.cos(Math.toRadians(pitch)));
		float verticalDistance = (float)(distance * Math.sin(Math.toRadians(pitch)));
		
		float offsetX = (float)(horizontalDistance * Math.sin(Math.toRadians(theta)));
		float offsetZ = (float)(horizontalDistance * Math.cos(Math.toRadians(theta)));
		position.x = cameraTarget.x - offsetX;
		position.z = cameraTarget.z - offsetZ;
		position.y = cameraTarget.y + vOffset + verticalDistance;
		
		this.pitch = pitch;
		this.yaw = 180.0f - theta;
	}
	
	private void calculateZoom(){
		float zoomLevel = Mouse.getDWheel() * 0.1f;
		if(cameraMode == CameraMode.THIRD_PERSON)
			thirdPersonDistance -= zoomLevel;
		else if(cameraMode == CameraMode.OVERHEAD)
			overheadCameraHeight -= zoomLevel;
	}

	public void setThirdPersonYaw(float angle){
		this.thirdPersonYaw = angle;
	}
	public void setThirdPersonPitch(float angle){
		this.thirdPersonPitch = angle;
	}
	public void setThirdPersonDistance(float d){
		this.thirdPersonDistance = d;
	}
	
	//used for rendering reflections
	public void invertPitch(){
		this.pitch = -pitch;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}
	
	private void updateThirdPersonCameraPos(Terrain terrain){
		if(Mouse.isButtonDown(2)){	//MIDDLE MOUSE BUTTON
			float pitchChange = MouseHelper.getDY() * 0.1f;
			thirdPersonPitch -= pitchChange;
		}
		if(Mouse.isButtonDown(2)){	//MIDDLE MOUSE BUTTON
			float angleChange = MouseHelper.getDX() * 0.3f;
			thirdPersonYaw -= angleChange;
		}
	}
	
	private void updateOverheadCameraPos(Terrain terrain){
		if(KeyboardHelper.isKeyDown(Keyboard.KEY_LEFT)){
			this.overheadCameraTarget.x += OVERHEAD_CAMERA_MOVE_SPEED*DisplayManager.getFrameTimeSeconds();
		}
		if(KeyboardHelper.isKeyDown(Keyboard.KEY_RIGHT)){
			this.overheadCameraTarget.x -= OVERHEAD_CAMERA_MOVE_SPEED*DisplayManager.getFrameTimeSeconds();
		}
		if(KeyboardHelper.isKeyDown(Keyboard.KEY_UP)){
			this.overheadCameraTarget.z += OVERHEAD_CAMERA_MOVE_SPEED*DisplayManager.getFrameTimeSeconds();
		}
		if(KeyboardHelper.isKeyDown(Keyboard.KEY_DOWN)){
			this.overheadCameraTarget.z -= OVERHEAD_CAMERA_MOVE_SPEED*DisplayManager.getFrameTimeSeconds();
		}
		this.overheadCameraTarget.y = terrain.getTerrainHeight(this.overheadCameraTarget.x, this.overheadCameraTarget.z);
	}

	@Override
	public void onKeyEvent(boolean state, int keyCode) {
		if(state && keyCode == Keyboard.KEY_V)
			cycleCameraMode();
	}
	
}
