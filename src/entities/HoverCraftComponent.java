package entities;

import renderEngine.DisplayManager;
import terrain.Terrain;
import toolbox.Maths;

public class HoverCraftComponent implements IGameComponent{

	private static final float MAX_SPEED = 20.0f;
	private static final float MAX_STRAFE_SPEED = 15.0f;
	private static final float MAX_TURN_RATE = 150.0f;
	private static final float MAX_PITCH = 15.0f;
	private static final float MAX_ROLL = 10.0f;
	private static final float HOVER_HEIGHT = 0.75f;
	
	private Entity entity;
	
	private float engineForwardPower = 0.0f;
	private float engineStrafePower = 0.0f;
	private float engineTurnPower = 0.0f;
	private float engineJumpPower = 0.0f;
	private float enginePitchPower = 0.0f;
	
	public HoverCraftComponent(Entity e) {
		this.entity = e;
	}
	
	@Override
	public void onCreate(Entity e){
		
	}
	
	@Override
	public void update(Entity e, Terrain terrain){
		
		Maths.capAngleTo180(this.entity.rotY);
		//turning
		//System.out.println(engineTurnPower);
		this.entity.rotY += engineTurnPower;
		this.entity.rotX += enginePitchPower;
		
		float fx = (float) Math.sin(this.entity.getRotY()*Math.PI/180.0f) * engineForwardPower + (float) Math.sin((this.entity.getRotY()+90.0f)*Math.PI/180.0f) * engineStrafePower;
		float fz = (float) Math.cos(this.entity.getRotY()*Math.PI/180.0f) * engineForwardPower + (float) Math.cos((this.entity.getRotY()+90.0f)*Math.PI/180.0f) * engineStrafePower;
		float fy = engineJumpPower;

		//damp engine power when it is close to the max speed
		float speed = this.entity.getVelocity().length();
		float enginePowerFactor = Math.max((MAX_SPEED - speed) / MAX_SPEED, 0f);
		fx *= enginePowerFactor;
		fz *= enginePowerFactor;
		
		this.entity.addForce(fx, fy, fz);
		
		//hover physics
		float dt = DisplayManager.getFrameTimeSeconds();
		float h = this.entity.getPosition().y - terrain.getTerrainHeight(this.entity.getPosition().x, this.entity.getPosition().z) - HOVER_HEIGHT;
		float f = Math.max(0.0f, -55.0f*h + e.world.getGravity()) - 2.5f*entity.getVelocity().y;
		this.entity.addForce(0, f, 0);
		
		float prevPitch = this.entity.rotX;
		float prevRoll = this.entity.rotZ;
		float newPitch = prevPitch + 1.4f*dt*(engineForwardPower*MAX_PITCH/MAX_SPEED - prevPitch);
		float newRoll = prevRoll + 1.4f*dt*(-engineStrafePower*MAX_ROLL/MAX_STRAFE_SPEED - prevRoll);
		this.entity.rotX = newPitch;
		this.entity.rotZ = newRoll;
		
		//sample the terrain normals around the craft and apply a rotational force that tries to align the craft to be perpendicular to the average terrain normal.
		//this.entity.
		float size = 1.0f;
		float x0 = this.entity.getPosition().x;
		float z0 = this.entity.getPosition().z;
		float h0 = terrain.getTerrainHeight(this.entity.getPosition().x, this.entity.getPosition().z);
		float h1 = terrain.getTerrainHeight(x0 + (float)(size*Math.sin(Math.toRadians(this.entity.rotY))), z0 + (float)(size*Math.cos(Math.toRadians(this.entity.rotY))));
	}
	
	@Override
	public void processInput(EntityMovement movement){
		engineForwardPower = movement.forward * MAX_SPEED;
		engineStrafePower = movement.strafe * MAX_STRAFE_SPEED;
		engineTurnPower = movement.turn;
		enginePitchPower = movement.pitchUp;
		engineJumpPower = movement.jump * (40.0f+entity.world.getGravity());
	}

	@Override
	public void onDestroy(Entity e){
		
	}
	
}
