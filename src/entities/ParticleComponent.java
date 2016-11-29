package entities;

import org.lwjgl.util.vector.Vector3f;

import particles.ParticleSystem;
import particles.ParticleTexture;
import terrain.Terrain;

public class ParticleComponent implements IGameComponent {

	private Entity entity;
	private ParticleSystemParams params;
	private ParticleSystem particleSystem;
	
	public ParticleComponent(ParticleSystemParams params){
		this.params = params;
		this.particleSystem = new ParticleSystem(params.texture, params.pps, params.speed, params.gravity, params.lifeLength, params.scale);
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
