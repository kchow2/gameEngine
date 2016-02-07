package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

public class Camera {
	private Vector3f position = new Vector3f(0,0,0);
	private float pitch, yaw, roll;
	
	public Camera(){
		
	}
	
	public void move(){
		if(Keyboard.isKeyDown(Keyboard.KEY_W)){
			position.z-=0.1f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
			position.x+=0.1f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A)){
			position.x-=0.1f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)){
			position.z+=0.1f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
			yaw-=0.2f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_E)){
			yaw+=0.2f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_R)){	//RESET POS + ROT
			position.x=0.0f;
			position.y=0.0f;
			position.z=0.0f;
			yaw=0.0f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
			position.y+=0.1f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
			position.y-=0.1f;
		}
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public void setRoll(float roll) {
		this.roll = roll;
	}
	
	
}
