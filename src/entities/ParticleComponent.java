package entities;

import particles.ParticleSystem;
import particles.ParticleTexture;
import terrain.Terrain;

public class ParticleComponent implements IGameComponent {

	private Entity entity;
	private ParticleTexture particleTexture;
	private ParticleSystem particleSystem;
	
	public ParticleComponent(ParticleTexture particleTexture){
		this.particleTexture = particleTexture;
		this.particleSystem = new ParticleSystem(particleTexture, 200.0f, 1.0f, 0.0f, 1.0f, 1.0f);
	}
	
	@Override
	public void onCreate(Entity e) {
		this.entity = e;
	}

	@Override
	public void processInput(EntityMovement movement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Entity e, Terrain terrain) {
		particleSystem.generateParticles(entity.getPosition());
	}

	@Override
	public void onDestroy(Entity e) {
		// TODO Auto-generated method stub
	}

}
