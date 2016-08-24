package entities;

import java.util.ArrayList;
import java.util.List;

import models.TexturedModel;
import physics.AABB;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import renderEngine.DisplayManager;
import renderEngine.ModelCache;
import terrain.Terrain;
import toolbox.Maths;
import toolbox.MouseHelper;

public class Player{
	
	//private static final float MAX_VELOCITY = 30.0f;
	//private static final float SPRINT_ACCELERATION = 100.0f;
	//private static final float FORWARD_ACCELERATION = 75.0f;
	//private static final float STRAFE_ACCELERATION = 50.0f;
	//private static final float TURN_SPEED = 160;
	//private static final float JUMP_POWER = 30;
	
	//private float currentForwardAcceleration = 0;
	//private float currentStrafeAcceleration = 0;
	//private float currentVerticalAcceleration = 0;
	//private float currentTurnSpeed = 0;
	
	//private long lastShotTime_ms = 0;
	//private int shotDelay_ms = 500;
	//private TexturedModel projectileModel;
	
	//private boolean isInAir = false;
	private EntityMovement movement = new EntityMovement();
	
	private Entity entity;
	private Entity targetedEntity = null;

	public Player(Entity entity) {
		this.entity = entity;
	}
	
	public Entity getEntity(){
		return this.entity;
	}
	
	public void setEntity(Entity entity){
		this.entity = entity;
	}
	
	
	public void checkInputs(){
		
		movement.clearInputs();
		
		//FORWARD
		if(Keyboard.isKeyDown(Keyboard.KEY_W)){
			movement.forward++;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)){
			movement.forward--;
		}
		
		//STRAFE
		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
			movement.strafe--;
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_A)){
			movement.strafe++;
		}
		
		//TURN
		if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
			movement.turn++;
		} else if(Keyboard.isKeyDown(Keyboard.KEY_E)){
			movement.turn--;
		}
		
		//JUMP
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
			movement.jump = 1.0f;
		}
		
		//fire weapons
		if(Mouse.isButtonDown(0)){ //L MOUSE BUTTON
			movement.fire = true;
		}
		
		//handle keyboard key events (non-polling)
		//These events are for processing non-movement commands, so multiple commands don't get issued if the key is held down
		while(Keyboard.next()){	
			if(Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_T){	//target object
				//this.targetedEntity = getEntityPlayerIsLookingAt(mobileEntities);
				if(this.targetedEntity != null){
					//float distance = getDistanceBetweenEntities(this.targetedEntity, this);
					//System.out.println("target distance: "+distance);
				}
			}
			if(Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_G){	//target object
				MouseHelper.grabMouse();
			}
			if(Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE){	//target object
				MouseHelper.releaseMouse();
			}
		}
		
		if(MouseHelper.isGrabbed()){
			int mouseDx = MouseHelper.getDX();
			int mouseDy = MouseHelper.getDY();
			//MouseHelper.resetPos();
			
			float mouseTurnSensitivity = .2f;
			float mousePitchSensitivity = .2f;
			
			movement.turn = (float)-mouseDx * mouseTurnSensitivity;
			movement.pitchUp = mouseDy * mousePitchSensitivity;
		}
		
		this.entity.processMovementInput(movement);
	}
	
	/*private Entity getEntityPlayerIsLookingAt(EntityMovementManager mobileEntities){
		float RANGE = 100f;
		float CONE_ANGLE = 25f;	//everything inside this cone can be detected, in degrees
		float RANGE2 = 15f;
		float CONE_ANGLE2 = 90f;	//if the target is very close to the player, loosen up on the detection angle
		float closestEntityDistance = RANGE;
		Entity closestEntity = null;
		
		List<Entity> candidates = new ArrayList<Entity>();
		for(Entity entity : mobileEntities.getEntities()){
			Vector3f toEntityVector = new Vector3f();
			Vector3f.sub(entity.getPosition(), this.getPosition(), toEntityVector);
			float distance = toEntityVector.length();
			toEntityVector.normalise(toEntityVector);
			
			if(distance < RANGE){
				Matrix4f trnMat = Maths.createTransformationMatrix(new Vector3f(0,0,0), this.rotX, this.rotY, this.rotZ, 1.0f);
				Vector4f dir = new Vector4f(0,0,1,0);
				Vector4f playerLookDirection4 = new Vector4f();
				Matrix4f.transform(trnMat, dir, playerLookDirection4);
				Vector3f playerLookDirection = new Vector3f(playerLookDirection4.x, playerLookDirection4.y, playerLookDirection4.z);
	
				float dotProduct = Vector3f.dot(playerLookDirection, toEntityVector);
				double angle = Math.toDegrees(Math.acos(dotProduct));
				
				if(angle < CONE_ANGLE || distance < RANGE2 && angle < CONE_ANGLE2){
					candidates.add(entity);
					if(distance < closestEntityDistance){
						closestEntity = entity;
						closestEntityDistance = distance;
					}
				}
			}
		}
		//System.out.println("candidates: "+candidates.size());
		return closestEntity;
		
	}*/
	
	public Entity getTargetedEntity(){
		return this.targetedEntity;
	}
	
	private static long getCurrentTime(){
		return Sys.getTime()*1000 / Sys.getTimerResolution();
	}

}
