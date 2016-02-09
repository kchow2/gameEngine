package entities;

import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;

public class Entity {
	private TexturedModel model;
	protected Vector3f position;
	protected float rotX, rotY, rotZ;
	protected float scale;
	
	private int textureIndex = 0;
	
	public Entity(TexturedModel model, Vector3f position, float rotX,
			float rotY, float rotZ, float scale) {
		super();
		this.model = model;
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
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
}
