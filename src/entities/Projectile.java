package entities;

import org.lwjgl.Sys;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import models.TexturedModel;
import terrain.Terrain;
import toolbox.Maths;

public class Projectile extends Entity{
	private int lifetime_ms;	//lifetime of the projectile in milliseconds. If the projectile hasn't hit anything within this time, it will despawn.
	private long startTime;
	
	
	public Projectile(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, float shotSpeed, float lifetime) {
		super(model, position, rotX, rotY, rotZ, scale);
		this.lifetime_ms = (int)(lifetime*1000);
		this.startTime = getCurrentTime();
		calculateStartingVelocity(position, rotX, rotY, rotZ, shotSpeed);
		isAlive = true;
		isGravityAffected = false;
		useMovementDamping = false;
	}
	
	@Override
	public void update(Terrain terrain){
		if(isAlive){
			super.update(terrain);
			
			if(getCurrentTime() - startTime > lifetime_ms){
				this.setDead();
			}
		}
	}
	
	@Override
	protected void onTerrainCollide(Terrain terrain){
		this.setDead();
		this.spawnExplosion(this.getPosition());
	}
	
	private void spawnExplosion(Vector3f position){
		//Explosion e = new Explosion(ModelCache., position, rotX, rotX, rotX, rotX);
		System.out.println("Spawning explosion at "+position);
	}
	
	private long getCurrentTime(){
		return Sys.getTime()*1000 / Sys.getTimerResolution();
	}
	
	private void calculateStartingVelocity(Vector3f position, float rotX, float rotY, float rotZ, float shotSpeed){
		Vector3f direction = this.getLookVec();
		
		velocity.x = shotSpeed*direction.x;
		velocity.y = shotSpeed*direction.y;
		velocity.z = shotSpeed*direction.z;
	}

}
