package models;

import java.util.ArrayList;
import java.util.List;

import physics.AABB;

public class ModelData {

	private float[] vertices;
	private float[] textureCoords;
	private float[] normals;
	private int[] indices;
	private float furthestPoint;
	
	private AABB aabb;	//the enclosing axis aligned bounding box
	
	private List<Hardpoint> hardpoints = new ArrayList<Hardpoint>();

	public ModelData(float[] vertices, float[] textureCoords, float[] normals, int[] indices,
			float furthestPoint, AABB aabb) {
		this.vertices = vertices;
		this.textureCoords = textureCoords;
		this.normals = normals;
		this.indices = indices;
		this.furthestPoint = furthestPoint;
		this.aabb = aabb;
	}
	
	public ModelData(float[] vertices, float[] textureCoords, float[] normals, int[] indices) {
		this.vertices = vertices;
		this.textureCoords = textureCoords;
		this.normals = normals;
		this.indices = indices;
		
		//find the furthest point and calculate AABB
		float furthestXZ = 0.0f;
		float furthestY = 0.0f;
		for(int i=0; i<vertices.length/3; i++){
			float x = vertices[i];
			float y = vertices[i+1];
			float z = vertices[i+2];
			float xz = (float) Math.sqrt(x*x+z*z);
			float d = (float) Math.sqrt(x*x+y*y+z*z);
			if(xz > furthestXZ){
				furthestXZ = xz;
			}
			if(y > furthestY){
				furthestY = y;
			}
			if(d > this.furthestPoint){
				this.furthestPoint = d;
			}
		}
		this.aabb = new AABB(furthestXZ, furthestY, furthestXZ);
	}

	public float[] getVertices() {
		return vertices;
	}

	public float[] getTextureCoords() {
		return textureCoords;
	}

	public float[] getNormals() {
		return normals;
	}

	public int[] getIndices() {
		return indices;
	}

	public float getFurthestPoint() {
		return furthestPoint;
	}
	
	public AABB getAABB(){
		return this.aabb;
	}
	
	public void addHardpoint(Hardpoint hardpoint){
		this.hardpoints.add(hardpoint);
	}
	
	public Hardpoint getHardpointByName(String name){
		for(Hardpoint h:hardpoints){
			if(h.name.equals(name)) 
				return h;
		}
		return null;
	}
	
	public List<Hardpoint> getHardpoints(){
		return hardpoints;
	}

}
