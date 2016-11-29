package entities;

import org.lwjgl.util.vector.Vector3f;

import particles.ParticleTexture;

//parameters for instantiating a particle component
public class ParticleSystemParams{
	public ParticleSystemParams(ParticleTexture texture, float pps, float speed){
		this.texture = texture;
		this.pps = pps;
		this.speed = speed;
	}
	public ParticleTexture texture;
	public float pps;		//particles emitted per second
	public float speed = 1.0f;
	public float gravity = 0.0f;
	public float lifeLength = 1.0f;
	public float scale = 1.0f;
	public float emitCone = 0.0f;	//particles are emitted randomly in a cone with this angle
	public Vector3f direction = new Vector3f(0,1,0);	//default direction is to emit upwards
}
