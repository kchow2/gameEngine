package entities;

import java.util.ArrayList;
import java.util.List;

import models.TexturedModel;
import physics.AABB;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import renderEngine.DisplayManager;
import terrain.Terrain;
import toolbox.Maths;

public class Player extends MobileEntity {
	
	private static final float MAX_VELOCITY = 30.0f;
	private static final float SPRINT_ACCELERATION = 100.0f;
	private static final float FORWARD_ACCELERATION = 75.0f;
	private static final float STRAFE_ACCELERATION = 50.0f;
	private static final float TURN_SPEED = 160;
	private static final float JUMP_POWER = 30;
	
	private float currentForwardAcceleration = 0;
	private float currentStrafeAcceleration = 0;
	private float currentVerticalAcceleration = 0;
	private float currentTurnSpeed = 0;
	
	//private boolean isInAir = false;
	
	private MobileEntity targetedEntity = null;

	public Player(TexturedModel model, Vector3f position, float rotX,
			float rotY, float rotZ, float scale, AABB aabb) {
		super(model, position, rotX, rotY, rotZ, scale, aabb);
	}
	
	public void move(Terrain terrain, MobileEntityManager mobileEntities){
		checkInputs(mobileEntities);
		super.increaseRotation(0, currentTurnSpeed*DisplayManager.getFrameTimeSeconds(), 0);
		
		float dt = DisplayManager.getFrameTimeSeconds();
		float accelerationDamping = 1.0f - this.velocity.lengthSquared() / (MAX_VELOCITY*MAX_VELOCITY+1);
		

		currentForwardAcceleration *= accelerationDamping;
		currentStrafeAcceleration *= accelerationDamping;
		//currentVerticalAcceleration *= accelerationDamping;
		
		float forwardVelocity = currentForwardAcceleration * dt;
		this.velocity.x += (float) (Math.sin(Math.toRadians(this.getRotY()))*forwardVelocity);
		this.velocity.z += (float) (Math.cos(Math.toRadians(this.getRotY()))*forwardVelocity);
		
		float strafeVelocity = currentStrafeAcceleration * dt;
		this.velocity.x += (float) (Math.sin(Math.toRadians(this.getRotY()+90.0f))*strafeVelocity);
		this.velocity.z += (float) (Math.cos(Math.toRadians(this.getRotY()+90.0f))*strafeVelocity);
		
		float verticalVelocity = currentVerticalAcceleration * dt;
		this.velocity.y += verticalVelocity;
	}
	
	private void checkInputs(MobileEntityManager mobileEntities){
		//FORWARDS
		if(Keyboard.isKeyDown(Keyboard.KEY_W)){
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
				this.currentForwardAcceleration = SPRINT_ACCELERATION;
			}
			else{
				this.currentForwardAcceleration = FORWARD_ACCELERATION;
			}
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_S)){
			this.currentForwardAcceleration = -FORWARD_ACCELERATION;
		}
		else{
			this.currentForwardAcceleration = 0;
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
			currentStrafeAcceleration = -STRAFE_ACCELERATION;
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_A)){
			currentStrafeAcceleration = STRAFE_ACCELERATION;
		}
		else{
			currentStrafeAcceleration = 0;
		}
		
		//JUMP
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
			currentVerticalAcceleration = JUMP_POWER;
			this.isJumping = true;
			//if(!isInAir){
			//	
			//	isInAir = true;
			//}
		}
		else{
			currentVerticalAcceleration = 0.0f;
			isJumping = false;
		}
		
		//OTHER MOVEMENT
		if(Keyboard.isKeyDown(Keyboard.KEY_R)){	//RESET POS + ROT
			position.x=0.0f;
			position.y=0.0f;
			position.z=0.0f;
			rotY=0.0f;
		}
		
		//handle keyboard key events (non-polling)
		//These events are for processing non-movement commands, so multiple commands don't get issued if the key is held down
		while(Keyboard.next()){	
			if(Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_T){	//target object
				this.targetedEntity = getEntityPlayerIsLookingAt(mobileEntities);
				if(this.targetedEntity != null){
					float distance = getDistanceBetweenEntities(this.targetedEntity, this);
					System.out.println("target distance: "+distance);
				}
			}
		}
		
	}
	
	private float getDistanceBetweenEntities(Entity e1, Entity e2){
		Vector3f toEntityVector = new Vector3f();
		Vector3f.sub(e1.getPosition(), e2.getPosition(), toEntityVector);
		return toEntityVector.length();
	}
	
	private MobileEntity getEntityPlayerIsLookingAt(MobileEntityManager mobileEntities){
		float RANGE = 100f;
		float CONE_ANGLE = 25f;	//everything inside this cone can be detected, in degrees
		float RANGE2 = 15f;
		float CONE_ANGLE2 = 90f;	//if the target is very close to the player, loosen up on the detection angle
		float closestEntityDistance = RANGE;
		MobileEntity closestEntity = null;
		
		List<MobileEntity> candidates = new ArrayList<MobileEntity>();
		for(MobileEntity entity : mobileEntities.getEntities()){
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
		
	}
	
	public MobileEntity getTargetedEntity(){
		return this.targetedEntity;
	}

}
