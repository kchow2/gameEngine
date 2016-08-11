package entities;

import renderEngine.DisplayManager;
import terrain.Terrain;
import toolbox.Maths;

public class HoverCraftComponent implements IGameComponent{

	private static final float MAX_SPEED = 20.0f;
	private static final float MAX_STRAFE_SPEED = 15.0f;
	private static final float MAX_TURN_RATE = 150.0f;
	private static final float MAX_PITCH = 10.0f;
	private static final float MAX_ROLL = 5.0f;
	private static final float HOVER_HEIGHT = 10.0f;
	
	private Entity entity;
	
	private float engineForwardPower = 0.0f;
	private float engineStrafePower = 0.0f;
	private float engineTurnPower = 0.0f;
	private float engineJumpPower = 0.0f;
	private float enginePitchPower = 0.0f;
	
	private float accelHeightCorrection = 0.0f;
	private float velocHeightCorrection = 0.0f;
	private float hoverHeight = 0.0f;
	
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
		this.entity.addForce(fx, fy, fz);
		
		
		float dt = DisplayManager.getFrameTimeSeconds();
		float h = this.entity.getPosition().y - terrain.getTerrainHeight(this.entity.getPosition().x, this.entity.getPosition().z) - hoverHeight;
		//make the craft hover at HOVER_HEIGHT above the terrain
		hoverHeight = Math.min(hoverHeight, h);
		hoverHeight = Math.min(hoverHeight, HOVER_HEIGHT);
		if(hoverHeight < HOVER_HEIGHT){
			hoverHeight += 0.5f * dt;
		}
		
		
		//float DAMP_FACTOR = 5f;
		//float antiGrav = Math.max(-10*h, 0.0f);//h < 0 ? 25.0f : 15.0f;
		//float springForce = Math.max(-(h+DAMP_FACTOR*this.entity.getVelocity().y), 0.0f);
		//System.out.println(h);
		
		//velocHeightCorrection = h < 0 ? 1.0f - 0.5f*h : 0.0f;
		//accelHeightCorrection = antiGrav + springForce;
		//this.entity.getVelocity().y += dt*accelHeightCorrection;
		//this.entity.getPosition().y += velocHeightCorrection*dt;
		
		//tilt the craft forward/backwards and left/right according to the current forward velocity
		if(Math.abs(this.entity.getVelocity().x) > 0.01f || Math.abs(this.entity.getVelocity().z) > 0.01f){
		
			float lookDirection = this.entity.getRotY();

			float vx = this.entity.getVelocity().x;	
			float vz = this.entity.getVelocity().z;
			float speed = (float)Math.sqrt(vx*vx+vz*vz);
			vx/=speed;
			vz/=speed;
			float velocityDirection = (float) ( Math.atan2(vx,vz)*180.0f/Math.PI);

			float dot1 = (float) Math.cos((lookDirection-velocityDirection)*Math.PI/180.0f);
			float dot2 = (float) Math.sin((lookDirection-velocityDirection)*Math.PI/180.0f);

			float pitchFactor = Math.min(speed/MAX_SPEED, 1.0f);
			float pitch = (float)(dot1*pitchFactor*MAX_PITCH);
			float roll = (float)(dot2*pitchFactor*MAX_ROLL);
			
			this.entity.rotX = pitch;
			this.entity.rotZ = roll;
		}
		
		//sample the terrain normals around the craft and apply a rotational force that tries to align the craft to be perpendicular to the average terrain normal.
		//this.entity.
		
		
	}
	
	@Override
	public void processInput(EntityMovement movement){
		engineForwardPower = movement.forward * MAX_SPEED;
		engineStrafePower = movement.strafe * MAX_STRAFE_SPEED;
		engineTurnPower = movement.turn;
		enginePitchPower = movement.pitchUp;
		engineJumpPower = movement.jump * 40.0f;
	}

	@Override
	public void onDestroy(Entity e){
		
	}
	
}
