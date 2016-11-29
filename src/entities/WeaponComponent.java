package entities;

import models.TexturedModel;
import particles.ParticleTexture;
import terrain.Terrain;

public class WeaponComponent implements IGameComponent {

	private long shotDelay_ms;
	private float shotSpeed;
	private float lifetime;
	private long lastFireTime = 0;
	private TexturedModel projectileModel;
	private TexturedModel explosionModel;
	private ParticleTexture particleTexture;
	private Entity entity;
	
	public WeaponComponent(TexturedModel projectileModel, TexturedModel explosionModel, ParticleTexture particleTexture, float shotDelay, float shotSpeed, float lifetime){
		this.projectileModel = projectileModel;
		this.explosionModel = explosionModel;
		this.particleTexture = particleTexture;
		this.shotDelay_ms = (long)(shotDelay * 1000.0f);
		this.shotSpeed = shotSpeed;
		this.lifetime = lifetime;
	}
	
	
	@Override
	public void onCreate(Entity e) {
		this.entity = e;
	}

	@Override
	public void processInput(EntityMovement movement) {
		long t = System.currentTimeMillis();
		if(movement.fire && (t > lastFireTime + shotDelay_ms)){
			Projectile projectile = new Projectile(entity.world, projectileModel, explosionModel, this.entity.getHardpointWorldPos("HP_CANNON"), this.entity.getRotX(),this.entity.getRotY(),this.entity.getRotZ(),1.0f, shotSpeed, lifetime);
			projectile.addComponent(new ParticleComponent(new ParticleSystemParams(particleTexture, 100, 2.0f)));
			ProjectileManager.get().addEntity(projectile);
			lastFireTime = t;
		}
	}

	@Override
	public void update(Entity e, Terrain terrain) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy(Entity e) {
		// TODO Auto-generated method stub
		
	}

}
