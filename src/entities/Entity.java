package entities;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import models.Hardpoint;
import models.TexturedModel;
import renderEngine.DisplayManager;
import terrain.Terrain;
import toolbox.Maths;

public class Entity {

	public static float GRAVITY = 20.0f;
	public static float BOUNCE_DAMPING = 0.6f;
	public static float BOUNCE_CUTOFF_THRESHOLD = 0.05f;
	
	protected boolean isJumping = false;
	protected boolean isGravityAffected = true;
	protected boolean useMovementDamping = true;
	
	private TexturedModel model;
	protected Vector3f position;
	protected Vector3f velocity = new Vector3f(0f,0.0f,0f);
	protected Vector3f acceleration = new Vector3f(0.0f, 0.0f, 0.0f);
	protected Vector3f forces = new Vector3f(0.0f,0.0f,0.0f);
	protected float rotX, rotY, rotZ;
	protected float scale;
	protected boolean isAlive;
	
	private int textureIndex = 0;
	
	private List<IGameComponent> components = new ArrayList<IGameComponent>();
	private List<Hardpoint> hardpoints = new ArrayList<Hardpoint>();
	
	public Entity(TexturedModel model, Vector3f position, float rotX,
			float rotY, float rotZ, float scale) {
		super();
		this.model = model;
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
		this.isAlive = true;
	}
	
	public void addComponent(IGameComponent component){
		this.components.add(component);
		component.onCreate(this);
	}
	
	public void setGravityAffected(boolean isGravityAffected){
		this.isGravityAffected = isGravityAffected;
	}
	
	public void setUseMovementDamping(boolean useMovementDamping){
		this.useMovementDamping = useMovementDamping;
	}
	
	public void addForce(float fx, float fy, float fz){
		this.forces.x += fx;
		this.forces.y += fy;
		this.forces.z += fz;
	}
	
	public void setTextureOffset(int offset){
		this.textureIndex = offset;
	}
	
	public float getTextureXOffset(){
		int col = textureIndex % model.getTexture().getNumberOfRows();
		return (float) col / (float) model.getTexture().getNumberOfRows();
	}
	public float getTextureYOffset(){
		int row = textureIndex / model.getTexture().getNumberOfRows();
		return (float) row / (float) model.getTexture().getNumberOfRows();
	}
	
	public Entity(TexturedModel model, int textureIndex, Vector3f position, float rotX,
			float rotY, float rotZ, float scale) {
		super();
		this.textureIndex = textureIndex;
		this.model = model;
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
	}
	
	public void increasePosition(float dx, float dy, float dz){
		this.position.x += dx;
		this.position.y += dy;
		this.position.z += dz;
	}
	
	public void increaseRotation(float rx, float ry, float rz){
		this.rotX += rx;
		this.rotY += ry;
		this.rotZ += rz;
	}

	public TexturedModel getModel() {
		return model;
	}

	public void setModel(TexturedModel model) {
		this.model = model;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}
	
	public Vector3f getVelocity(){
		return velocity;
	}
	
	public Vector3f getAcceleration(){
		return acceleration;
	}

	public float getRotX() {
		return rotX;
	}

	public void setRotX(float rotX) {
		this.rotX = rotX;
	}

	public float getRotY() {
		return rotY;
	}

	public void setRotY(float rotY) {
		this.rotY = rotY;
	}

	public float getRotZ() {
		return rotZ;
	}

	public void setRotZ(float rotZ) {
		this.rotZ = rotZ;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}
	
	public boolean isAlive(){
		return isAlive;
	}
	
	public void setDead(){
		this.isAlive = false;
		for(int i = 0; i < this.components.size(); i++){
			components.get(i).onDestroy(this);
		}
	}
	
	public Vector3f getLookVec(){
		Vector4f direction = new Vector4f(0,0,1,0);
		Matrix4f mat = Maths.createTransformationMatrix(new Vector3f(0,0,0), rotX, rotY, rotZ, 1.0f);
		Matrix4f.transform(mat, direction, direction);

		return new Vector3f(direction);
	}
	
	protected void onTerrainCollide(Terrain terrain){
		
	}
	
	public void addHardpoint(Hardpoint hardpoint){
		this.hardpoints.add(hardpoint);
	}
	
	protected Hardpoint getHardpoint(String name){
		for(Hardpoint h:hardpoints){
			if(h.name.equals(name))
				return h;
		}
		return null;
	}
	public Vector3f getHardpointWorldPos(String hardpointName){
		Hardpoint h = getHardpoint(hardpointName);
		if(h != null){
			Matrix4f entityTrans = Maths.createTransformationMatrix(this.position, rotX, rotY, rotZ, 1.0f);
			Matrix4f temp = Matrix4f.mul(entityTrans, h.transform, null);
			Vector4f res = new Vector4f(0,0,0, 1.0f);
			Matrix4f.transform(temp, res, res);
			return new Vector3f(res);
		}
		else
			return null;
	}
	
	
	public void update(Terrain terrain){
		Vector3f pos = this.getPosition();
		float terrainHeight = terrain.getTerrainHeight(pos.x, pos.z);
		
		float dt = DisplayManager.getFrameTimeSeconds();
		float grav = isGravityAffected ? GRAVITY : 0;
		
		//zero out the forces every frame. Each component is responsible for recalculating forces every tick.
		this.forces.set(0.0f,0.0f,0.0f);
		
		for(int i = 0; i < this.components.size(); i++){
			components.get(i).update(this, terrain);
		}
		
		//calculate the acceleration of this object based on the forces and the mass
		float mass = 1.0f;
		this.acceleration.x = this.forces.x / mass;
		this.acceleration.y = this.forces.y / mass - grav;
		this.acceleration.z = this.forces.z / mass;
		
		this.position.x += this.velocity.x*dt + 0.5f*acceleration.x*dt*dt;
		this.position.y += this.velocity.y*dt + 0.5f*acceleration.y*dt*dt;
		this.position.z += this.velocity.z*dt + 0.5f*acceleration.z*dt*dt;
		this.velocity.x += this.acceleration.x*dt;
		this.velocity.y += this.acceleration.y*dt;
		this.velocity.z += this.acceleration.z*dt;
		
		if(this.position.y - terrainHeight < 0){	//check if the player is not trying to jump, otherwise the player will be stuck to the ground when jumping while traveling uphill
			this.position.y = terrainHeight;
			if(this.velocity.y < 0 && -this.velocity.y*dt > BOUNCE_CUTOFF_THRESHOLD)
				this.velocity.y = -this.velocity.y * BOUNCE_DAMPING;
			else if(this.velocity.y < 0){
				this.velocity.y = 0;
			}
			this.onTerrainCollide(terrain);
		}
		
		//movement damping when object is not accelerating
		if(useMovementDamping && Math.abs(this.acceleration.x) < 0.001 && Math.abs(this.acceleration.z) < 0.001){
			float xDamp = Math.min(Math.abs(velocity.x), 12.0f);
			xDamp = this.velocity.x < 0 ? -xDamp : xDamp;
			this.velocity.x -= xDamp*dt;
			float zDamp = Math.min(Math.abs(velocity.z), 12.0f);
			zDamp = this.velocity.z < 0 ? -zDamp : zDamp;
			this.velocity.z -= zDamp*dt;
		}
		
		
	}
	
	public void processMovementInput(EntityMovement movement){
		for(int i = 0; i < this.components.size(); i++){
			components.get(i).processInput(movement);
		}
	}
}
