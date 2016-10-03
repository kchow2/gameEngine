package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.DisplayManager;
import toolbox.KeyboardEventListener;
import toolbox.KeyboardHelper;
import toolbox.MouseHelper;
import world.World;

public class Camera implements KeyboardEventListener{
	
	public enum CameraMode{THIRD_PERSON, FIRST_PERSON, OVERHEAD};
	
	CameraMode cameraMode = CameraMode.THIRD_PERSON;
	
	private static final float OVERHEAD_CAMERA_MOVE_SPEED = 45.0f;
	private static final float DEFAULT_OVERHEAD_CAMERA_HEIGHT = 65.0f;	//the default height of the camera above the terrain
	
	private float distanceFromPlayer = 50.0f;
	private float angleAroundPlayer = 0.0f;
	
	//overhead camera settings
	private float cameraHeight = DEFAULT_OVERHEAD_CAMERA_HEIGHT;
	
	
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
			if(KeyboardHelper.isKeyDown(Keyboard.KEY_LEFT)){
				this.position.x -= OVERHEAD_CAMERA_MOVE_SPEED*DisplayManager.getFrameTimeSeconds();
			}
			if(KeyboardHelper.isKeyDown(Keyboard.KEY_RIGHT)){
				this.position.x += OVERHEAD_CAMERA_MOVE_SPEED*DisplayManager.getFrameTimeSeconds();
			}
			if(KeyboardHelper.isKeyDown(Keyboard.KEY_UP)){
				this.position.z -= OVERHEAD_CAMERA_MOVE_SPEED*DisplayManager.getFrameTimeSeconds();
			}
			if(KeyboardHelper.isKeyDown(Keyboard.KEY_DOWN)){
				this.position.z += OVERHEAD_CAMERA_MOVE_SPEED*DisplayManager.getFrameTimeSeconds();
			}
			
			this.yaw = 0;
			this.pitch = 70;
			this.position.y = world.getTerrain().getTerrainHeight(position.x, position.z) + cameraHeight;
			
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
			calculateZoom();
			calculatePitch();
			calculateAngleAroundPlayer();
			
			float horizontalDistance = calculateHorizontalDistance();
			float verticalDistance = calculateVerticalDistance();
			calculateCameraPosition(horizontalDistance, verticalDistance);
		}
	}
	
	public void setCameraMode(CameraMode mode){
		this.cameraMode = mode;
		if(mode == CameraMode.FIRST_PERSON){
			world.hideEntity(player);
		}
		else{
			world.showEntity(player);
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
	
	private float calculateHorizontalDistance(){
		return (float)(distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
	}
	private float calculateVerticalDistance(){
		return (float)(distanceFromPlayer * Math.sin(Math.toRadians(pitch)));
	}
	private void calculateCameraPosition(float horizontalDistance, float verticalDistance){
		
		float theta = player.getRotY() + angleAroundPlayer;
		float offsetX = (float)(horizontalDistance * Math.sin(Math.toRadians(theta)));
		float offsetZ = (float)(horizontalDistance * Math.cos(Math.toRadians(theta)));
		position.x = player.getPosition().x - offsetX;
		position.z = player.getPosition().z - offsetZ;
		position.y = player.getPosition().y + 1 + verticalDistance;
		
		yaw = 180.0f - theta;
	}
	
	private void calculateZoom(){
		float zoomLevel = Mouse.getDWheel() * 0.1f;
		distanceFromPlayer -= zoomLevel;
	}
	
	private void calculatePitch(){
		if(Mouse.isButtonDown(2)){	//R_BUTTON
			float pitchChange = MouseHelper.getDY() * 0.1f;
			pitch -= pitchChange;
		}
	}
	
	private void calculateAngleAroundPlayer(){
		if(Mouse.isButtonDown(2)){	//R_BUTTON
			float angleChange = MouseHelper.getDX() * 0.3f;
			angleAroundPlayer -= angleChange;
		}
	}

	public float getDistanceFromPlayer() {
		return distanceFromPlayer;
	}

	public void setDistanceFromPlayer(float distanceFromPlayer) {
		this.distanceFromPlayer = distanceFromPlayer;
	}

	public float getAngleAroundPlayer() {
		return angleAroundPlayer;
	}

	public void setAngleAroundPlayer(float angleAroundPlayer) {
		this.angleAroundPlayer = angleAroundPlayer;
	}
	
	public void invertPitch(){
		pitch = -pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	@Override
	public void onKeyEvent(boolean state, int keyCode) {
		if(state && keyCode == Keyboard.KEY_V)
			cycleCameraMode();
		
	}
	
}
