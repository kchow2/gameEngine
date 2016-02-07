package entities;

import org.lwjgl.util.vector.Vector3f;

public class Light {

	protected Vector3f position, colour;

	public Light(Vector3f position, Vector3f colour) {
		this.position = position;
		this.colour = colour;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public Vector3f getColour() {
		return colour;
	}

	public void setColor(Vector3f colour) {
		this.colour = colour;
	}
	
	
}
