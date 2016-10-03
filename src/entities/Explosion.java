package entities;

import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import terrain.Terrain;
import world.World;

public class Explosion extends Entity{

	private long lifetime_ms = 100;
	private float startRadius = 0.1f;
	private float endRadius = 1.75f;
	
	private long explosionStartTime;

	public Explosion(World world, TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, float size) {
		super(world, model, position, rotX, rotY, rotZ, scale);
		explosionStartTime = System.currentTimeMillis();
	}
	
	@Override
	public void update(Terrain terrain){
		
		long currentTime = System.currentTimeMillis();
		
		if(currentTime - explosionStartTime > lifetime_ms){
			this.setDead();
			//this.explosionStartTime = currentTime;//loop animation
		}	
		
		float explosionRadius = (endRadius - startRadius)*((float)(currentTime - explosionStartTime) / lifetime_ms) + startRadius;
		this.setScale(explosionRadius);
	}

	
}
