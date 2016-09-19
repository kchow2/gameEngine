package models;

import org.lwjgl.util.vector.Matrix4f;

public class Hardpoint {
	public String name;
	public Matrix4f transform;
	
	public Hardpoint(String name, Matrix4f transform){
		this.name = name;
		this.transform = transform;
	}
	public String toString(){
		return name + "\n" + transform.toString();
	}
}
