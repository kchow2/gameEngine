package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import toolbox.KeyboardEventListener;
import toolbox.KeyboardHelper;
import toolbox.MouseHelper;
import world.World;

public class Player implements KeyboardEventListener{
	private EntityMovement movement = new EntityMovement();
	
	private World world;
	private Camera camera;
	private Entity entity;
	private Entity targetedEntity = null;
	private boolean controlsEnabled = true;

	public Player(World world, Entity entity, Camera camera) {
		this.world = world;
		this.entity = entity;
		this.camera = camera;
		KeyboardHelper.addListener(this);
	}
	
	public Entity getEntity(){
		return this.entity;
	}
	
	public void setEntity(Entity entity){
		this.entity = entity;
	}
	
	public void disableControls(){
		this.controlsEnabled = false;
	}
	
	public void enableControls(){
		this.controlsEnabled = true;
	}
	
	
	public void checkInputs(){
		
		movement.clearInputs();

		if(!controlsEnabled){
			return;
		}
		
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

	@Override
	public void onKeyEvent(boolean state, int keyCode) {
		if(state && keyCode == Keyboard.KEY_G)
			MouseHelper.grabMouse();
		else if(state && keyCode == Keyboard.KEY_ESCAPE)
			MouseHelper.releaseMouse();
		else if(state && keyCode == Keyboard.KEY_W && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
			world.toggleWorldEditor(camera, this);
	}
}
