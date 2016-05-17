package entities;

import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import physics.AABB;
import physics.CollisionManager;
import renderEngine.DisplayManager;
import terrain.Terrain;

public class MobileEntity extends Entity {
	
	public static float GRAVITY = 20.0f;
	public static float BOUNCE_DAMPING = 0.6f;
	public static float BOUNCE_CUTOFF_THRESHOLD = 0.05f;
	
	protected Vector3f velocity = new Vector3f(0f,5.0f,0f);
	protected Vector3f acceleration = new Vector3f(0.0f, 0.0f, 0.0f);
	//protected Vector3f forces = new Vector3f(0f,0f,0f);

	public MobileEntity(TexturedModel model, Vector3f position, float rotX,
			float rotY, float rotZ, float scale, AABB aabb) {
		super(model, position, rotX, rotY, rotZ, scale);
		
		//this.addForce(GRAVITY, new Vector3f(0f,-1f,0f));
		this.acceleration.y = -GRAVITY;
		
		AABB aabb_ = new AABB(this, aabb.x2-aabb.x1, aabb.y2-aabb.y1, aabb.z2-aabb.z1, 0, 0, 0);
		CollisionManager.addBoundingBox(aabb_);
	}
	
	public Vector3f getVelocity(){
		return velocity;
	}
	
	public Vector3f getAcceleration(){
		return acceleration;
	}
	
	public void update(Terrain terrain){
		Vector3f pos = this.getPosition();
		float terrainHeight = terrain.getTerrainHeight(pos.x, pos.z);
		
		float dt = DisplayManager.getFrameTimeSeconds();
		
		this.position.x += this.velocity.x*dt + 0.5f*acceleration.x*dt*dt;
		this.position.y += this.velocity.y*dt + 0.5f*acceleration.y*dt*dt;
		this.position.z += this.velocity.z*dt + 0.5f*acceleration.z*dt*dt;
		this.velocity.x += this.acceleration.x*dt;
		this.velocity.y += this.acceleration.y*dt;
		this.velocity.z += this.acceleration.z*dt;
		
		if(this.position.y - terrainHeight < 0.001){
			this.position.y = terrainHeight;
			if(this.velocity.y < 0 && -this.velocity.y*dt > BOUNCE_CUTOFF_THRESHOLD)
				this.velocity.y = -this.velocity.y * BOUNCE_DAMPING;
			else
				this.velocity.y = 0;
		}
		
		//movement damping
		float xDamp = Math.min(Math.abs(velocity.x), 12.0f);
		xDamp = this.velocity.x < 0 ? -xDamp : xDamp;
		this.velocity.x -= xDamp*dt;
		float zDamp = Math.min(Math.abs(velocity.z), 12.0f);
		zDamp = this.velocity.z < 0 ? -zDamp : zDamp;
		this.velocity.z -= zDamp*dt;
	}
	
}
