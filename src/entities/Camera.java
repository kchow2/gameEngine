package entities;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {
	
	private float distanceFromPlayer = 50.0f;
	private float angleAroundPlayer = 0.0f;
	
	
	private Vector3f position = new Vector3f(0,0,0);
	private float pitch, yaw;
	
	private Player player;
	
	public Camera(Player player){
		this.player = player;
	}
	
	public void move(){
		calculateZoom();
		calculatePitch();
		calculateAngleAroundPlayer();
		
		float horizontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horizontalDistance, verticalDistance);
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
		if(Mouse.isButtonDown(1)){	//R_BUTTON
			float pitchChange = Mouse.getDY() * 0.1f;
			pitch -= pitchChange;
		}
	}
	
	private void calculateAngleAroundPlayer(){
		if(Mouse.isButtonDown(1)){	//R_BUTTON
			float angleChange = Mouse.getDX() * 0.3f;
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
	
	
}
