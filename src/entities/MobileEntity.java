package entities;

import models.TexturedModel;

import org.lwjgl.util.vector.Vector3f;

import renderEngine.DisplayManager;
import terrain.Terrain;

public class MobileEntity extends Entity {
	
	public static float GRAVITY = 50f;
	public static float BOUNCE_DAMPING = 0.6f;
	public static float BOUNCE_CUTOFF_THRESHOLD = 0.05f;
	
	protected Vector3f velocity = new Vector3f(0f,5.0f,0f);
	protected Vector3f forces = new Vector3f(0f,0f,0f);

	public MobileEntity(TexturedModel model, Vector3f position, float rotX,
			float rotY, float rotZ, float scale) {
		super(model, position, rotX, rotY, rotZ, scale);
		
		this.addForce(GRAVITY, new Vector3f(0f,-1f,0f));
	}
	
	public Vector3f getVelocity(){
		return velocity;
	}
	
	public Vector3f getForces(){
		return forces;
	}
	
	public void addForce(float amt, Vector3f direction){
		Vector3f normal = new Vector3f(); 
		direction.normalise(normal);
		forces.x += amt * normal.x;
		forces.y += amt * normal.y;
		forces.z += amt * normal.z;
	}
	
	public void update(Terrain terrain){
		Vector3f pos = this.getPosition();
		float terrainHeight = terrain.getTerrainHeight(pos.x, pos.z);
		
		float dt = DisplayManager.getFrameTimeSeconds();
		this.position.x += dt * (this.velocity.x - forces.x*dt/2);
		this.position.y += dt * (this.velocity.y - forces.y*dt/2);
		this.position.z += dt * (this.velocity.z - forces.z*dt/2);
		this.velocity.x = this.velocity.x + forces.x * dt;
		this.velocity.y = this.velocity.y + forces.y * dt;
		this.velocity.z = this.velocity.z + forces.z * dt;
		
		if(this.position.y - terrainHeight < 0.001){
			this.position.y = terrainHeight;
			if(this.velocity.y < 0 && -this.velocity.y*dt > BOUNCE_CUTOFF_THRESHOLD)
				this.velocity.y = -this.velocity.y * BOUNCE_DAMPING;
			else
				this.velocity.y = 0;
		}
	}
	
}
