package entities;

import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import terrain.Terrain;

public class Explosion extends Entity{

	private long lifetime_ms = 300;
	private float startRadius = 1.0f;
	private float endRadius = 20.0f;
	
	private long explosionStartTime;
	
	
	public Explosion(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(model, position, rotX, rotY, rotZ, scale);
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
