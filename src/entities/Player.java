package entities;

import models.TexturedModel;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.DisplayManager;

public class Player extends Entity {
	
	private static final float TERRAIN_HEIGHT = 0;
	private static final float SPRINT_SPEED = 50;
	private static final float RUN_SPEED = 20;
	private static final float STRAFE_SPEED = 15;
	private static final float TURN_SPEED = 160;
	private static final float GRAVITY = 50;
	private static final float JUMP_POWER = 30;
	
	private float currentForwardSpeed = 0;
	private float currentStrafeSpeed = 0;
	private float currentVerticalSpeed = 0;
	private float currentTurnSpeed = 0;
	
	private boolean isInAir = false;

	public Player(TexturedModel model, Vector3f position, float rotX,
			float rotY, float rotZ, float scale) {
		super(model, position, rotX, rotY, rotZ, scale);
		// TODO Auto-generated constructor stub
	}
	
	public void move(){
		checkInputs();
		super.increaseRotation(0, currentTurnSpeed*DisplayManager.getFrameTimeSeconds(), 0);
		
		float forwardDistance = currentForwardSpeed * DisplayManager.getFrameTimeSeconds();
		float dx = (float) (Math.sin(Math.toRadians(this.getRotY()))*forwardDistance);
		float dz = (float) (Math.cos(Math.toRadians(this.getRotY()))*forwardDistance);;
		
		float strafeDistance = currentStrafeSpeed * DisplayManager.getFrameTimeSeconds();
		dx += (float) (Math.sin(Math.toRadians(this.getRotY()+90.0f))*strafeDistance);
		dz += (float) (Math.cos(Math.toRadians(this.getRotY()+90.0f))*strafeDistance);
		
		float verticalDistance = currentVerticalSpeed * DisplayManager.getFrameTimeSeconds();
		float dy = verticalDistance;
		super.increasePosition(dx, dy, dz);
		
		if(super.getPosition().y < TERRAIN_HEIGHT){
			currentVerticalSpeed = 0;
			isInAir = false;
			this.getPosition().y = TERRAIN_HEIGHT;
		}
		
		currentVerticalSpeed -= GRAVITY * DisplayManager.getFrameTimeSeconds();
	}
	
	private void checkInputs(){
		//FORWARDS
		if(Keyboard.isKeyDown(Keyboard.KEY_W)){
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
				this.currentForwardSpeed = SPRINT_SPEED;
			}
			else{
				this.currentForwardSpeed = RUN_SPEED;
			}
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_S)){
			this.currentForwardSpeed = -RUN_SPEED;
		}
		else{
			this.currentForwardSpeed = 0;
		}
		
		//ROTATE
		if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
			this.currentTurnSpeed = TURN_SPEED;
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_E)){
			this.currentTurnSpeed = -TURN_SPEED;
		}
		else{
			this.currentTurnSpeed = 0;
		}
		
		//STRAFE
		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
			currentStrafeSpeed = RUN_SPEED;
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_A)){
			currentStrafeSpeed = -RUN_SPEED;
		}
		else{
			currentStrafeSpeed = 0;
		}
		
		//JUMP
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
			if(!isInAir){
				currentVerticalSpeed = JUMP_POWER;
				isInAir = true;
			}
		}
		
		//OTHER MOVEMENT
		if(Keyboard.isKeyDown(Keyboard.KEY_R)){	//RESET POS + ROT
			position.x=0.0f;
			position.y=0.0f;
			position.z=0.0f;
			rotY=0.0f;
		}
		
	}

}
