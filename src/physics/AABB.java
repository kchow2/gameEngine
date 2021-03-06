package physics;

import org.lwjgl.util.vector.Vector3f;

import entities.Entity;

public class AABB {
	public float xSize, ySize, zSize;
	public float x1,x2,y1,y2,z1,z2;
	
	public AABB(float xSize, float ySize, float zSize){
		this.xSize = xSize;
		this.ySize = ySize;
		this.zSize = zSize;
		this.x1 = -xSize/2.0f;
		this.x2 = xSize/2.0f;
		this.y1 = 0;
		this.y2 = ySize;
		this.z1 = -zSize/2.0f;
		this.z2 = zSize/2.0f;
	}
	
	public AABB(AABB aabb) {
		this(aabb.xSize, aabb.ySize, aabb.zSize);
	}

	//update the AABB position to move with the entity
	public void updatePos(Entity sourceEntity){
		Vector3f pos = sourceEntity.getPosition();
		this.x1 = pos.x - xSize/2.0f;
		this.x2 = pos.x + xSize/2.0f;
		this.y1 = pos.y;
		this.y2 = pos.y + ySize;
		this.z1 = pos.z - zSize/2.0f;
		this.z2 = pos.z + zSize/2.0f;
	}
	
	public boolean doesCollide(AABB aabb){
		if(aabb == this)
			return false;
		
		boolean b1 = this.x2 < aabb.x1 || this.x1 > aabb.x2;
		boolean b2 = this.y2 < aabb.y1 || this.y1 > aabb.y2;
		boolean b3 = this.z2 < aabb.z1 || this.z1 > aabb.z2;
		return !(b1 || b2 || b3);
	}
	
	public String toString(){
		return String.format("xMin=%f xMax=%f yMin=%f yMax=%f zMin=%f zMax=%f", x1,x2,y1,y2,z1,z2);
	}
}
