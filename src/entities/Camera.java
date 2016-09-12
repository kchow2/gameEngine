package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import toolbox.KeyboardEventListener;
import toolbox.KeyboardHelper;
import toolbox.MouseHelper;
import world.World;

public class Camera implements KeyboardEventListener{
	
	public enum CameraMode{THIRD_PERSON, FIRST_PERSON, OVERHEAD};
	
	CameraMode cameraMode = CameraMode.THIRD_PERSON;
	
	private float distanceFromPlayer = 50.0f;
	private float angleAroundPlayer = 0.0f;
	
	
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
		if(cameraMode == CameraMode.FIRST_PERSON){
			this.position.x = player.getPosition().x;
			this.position.y = player.getPosition().y;
			this.position.z = player.getPosition().z;
			this.pitch = player.rotX;
			this.yaw = player.rotY;
		}
		
		if(cameraMode == CameraMode.THIRD_PERSON){
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
