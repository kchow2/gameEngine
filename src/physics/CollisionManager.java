package physics;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import entities.Entity;
import toolbox.Maths;

public class CollisionManager {

	private  ArrayList<CollisionEntry> collisionObjects = new ArrayList<CollisionEntry>();
	
	public void addCollisionDetection(Entity entity, AABB aabb){
		collisionObjects.add(new CollisionEntry(entity, aabb));
	}
	
	public void removeCollisonDetection(Entity e){
		for(int i = 0 ; i < collisionObjects.size(); i++){
			if(collisionObjects.get(i).entity == e){
				collisionObjects.remove(i);
			}
		}
	}
	
	public int getObjectCount(){
		return collisionObjects.size();
	}
	
	public void checkCollisions(){
		//update AABBs	
		for(CollisionEntry obj:collisionObjects){
			obj.aabb.updatePos(obj.entity);
		}
		
		//check collisions naively is an O(N^2) operation
		//TODO: spacial partitioning to reduce number of collision checks
		for(int i = 0; i < collisionObjects.size(); i++){
			for(int j = i+1; j < collisionObjects.size(); j++){
				AABB aabb1 = collisionObjects.get(i).aabb;
				AABB aabb2 = collisionObjects.get(j).aabb;
				Entity entity1 = collisionObjects.get(i).entity;
				Entity entity2 = collisionObjects.get(j).entity;
				if(entity1 == entity2)
					continue;
				float dx = ((aabb1.x2+aabb1.x1)/2) - ((aabb2.x2+aabb2.x1)/2);
				float dy = ((aabb1.y2+aabb1.y1)/2) - ((aabb2.y2+aabb2.y1)/2);
				float dz = ((aabb1.z2+aabb1.z1)/2) - ((aabb2.x2+aabb2.z1)/2);
				float dist = (float)Math.sqrt(dx*dx+dy*dy+dz*dz);
				//System.out.println("dist="+dist);
				if(aabb1.doesCollide(aabb2)){
					resolveCollision(collisionObjects.get(i), collisionObjects.get(j));
				}
			}
		}
	}
	
	private void resolveCollision(CollisionEntry obj1, CollisionEntry obj2){
		Entity entity1 = obj1.entity;
		Entity entity2 = obj2.entity;
		Vector3f pos1 = entity1.getPosition();
		Vector3f pos2 = entity2.getPosition();
		Vector3f v1 = entity1.getVelocity();
		Vector3f v2 = entity2.getVelocity();
		
		final float MASS1 = 1.0f;
		final float MASS2 = 1.0f;
		final float COEFF_REST = 0.7f;

		//System.out.println("pos1="+pos1+" pos2="+pos2);
		//System.out.println("BEFORE COLLISION v1="+v1+" v2="+v2);
		float v1Initial = v1.length();
		float v2Initial = v2.length();
		float v1Final = COEFF_REST*((MASS2-MASS1)*v1Initial+2*MASS2*v2Initial)/(MASS1+MASS2);
		float v2Final = COEFF_REST*((MASS1-MASS2)*v2Initial+2*MASS1*v1Initial)/(MASS1+MASS2);
		
		//System.out.println("v1="+v1Initial+" v2="+v2Initial+" v1f="+v1Final+" v2f="+v2Final);
		
		Vector3f collisionDirection = new Vector3f(pos2.x-pos1.x, pos2.y-pos1.y, pos2.z-pos1.z);
		if(collisionDirection.lengthSquared() > 0.0001f){
			collisionDirection.normalise(collisionDirection);
			
			//add a 'pushing' force for 2 objects that are stuck inside each other with close to 0 velocity to push them apart
			if(v1Final < 0.01f && v2Final < 0.01f){
				v1Final = 0.5f;
				v2Final = 0.5f;
			}
			
			v1.x = -collisionDirection.x*v1Final;
			v1.y = -collisionDirection.y*v1Final;
			v1.x = -collisionDirection.z*v1Final;
			
			v2.x = collisionDirection.x*v2Final;
			v2.y = collisionDirection.y*v2Final;
			v2.z = collisionDirection.z*v2Final;
			
			//System.out.println("Collision detected! aabb1="+aabb1.getCenter()+" aabb2=" + aabb2.getCenter());
		}
		else{
			//the objects are exactly in the same position, so we can't get a valid collision direction
			//resolve it by jiggling the second object a little bit.
			v2.x += 0.01f;
			
		}
	}
	
	public class CollisionEntry{
		public Entity entity;
		public AABB aabb;
		
		public CollisionEntry(Entity e, AABB aabb){
			this.entity = e;
			this.aabb = aabb;
		}
	}
	
}
