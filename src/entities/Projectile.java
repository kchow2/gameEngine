package entities;

import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import physics.AABB;
import terrain.Terrain;
import world.World;

public class Projectile extends Entity{
	private int lifetime_ms;	//lifetime of the projectile in milliseconds. If the projectile hasn't hit anything within this time, it will despawn.
	private long startTime;
	private TexturedModel explosionModel;
	private Entity shooter;
	
	public Projectile(World world, Entity shooter, TexturedModel model, TexturedModel explosionModel, Vector3f position, float rotX, float rotY, float rotZ, float scale, float shotSpeed, float lifetime) {
		super(world, model, position, rotX, rotY, rotZ, 0.3f, 0.3f, 0.3f, scale);
		this.shooter = shooter;
		this.explosionModel = explosionModel;
		this.lifetime_ms = (int)(lifetime*1000);
		this.startTime = getCurrentTime();
		calculateStartingVelocity(position, rotX, rotY, rotZ, shotSpeed);
		isAlive = true;
		isGravityAffected = false;
		useMovementDamping = false;
		this.setCanCollide(true);
		//this.addComponent(new CollisionComponent(world.collisionManager, new AABB(0.5f,0.5f,0.5f)));
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
	protected boolean onTerrainCollide(Terrain terrain){
		this.setDead();
		this.spawnExplosion(this.getPosition());
		return true;
	}
	
	@Override
	public boolean onEntityCollide(Entity e){
		if(!e.equals(shooter)){
			this.setDead();
			this.spawnExplosion(this.getPosition());
			
			int damage = 15;
			e.applyDamage(damage);
			return true;
		}
		else{
			return false;
		}
	}
	
	private void spawnExplosion(Vector3f position){
		assert(explosionModel != null);
		Explosion e = new Explosion(world, explosionModel, position, rotX, rotY, rotZ, 1.0f, 1.0f, 1.0f, 1.0f);
		world.spawnExplosion(e);
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
