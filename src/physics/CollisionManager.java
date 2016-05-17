package physics;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import entities.MobileEntity;

public class CollisionManager {

	private static ArrayList<AABB> aabbs = new ArrayList<AABB>();
	
	public static void addBoundingBox(AABB aabb){
		aabbs.add(aabb);
	}
	
	public static void checkCollisions(){
		//update AABBs
		for(AABB aabb:aabbs){
			aabb.updatePos();
		}
		
		//checking collisions is an O(N^2) operation
		for(int i = 0; i < aabbs.size(); i++){
			for(int j = i+1; j < aabbs.size(); j++){
				AABB aabb1 = aabbs.get(i);
				AABB aabb2 = aabbs.get(j);
				if(aabb1 == aabb2 || aabb1.getEntity() == aabb2.getEntity())
					continue;
				if(aabb1.doesCollide(aabb2)){
					resolveCollision(aabb1, aabb2);
				}
			}
		}
	}
	
	private static void resolveCollision(AABB aabb1, AABB aabb2){
		MobileEntity entity1 = aabb1.getEntity();
		MobileEntity entity2 = aabb2.getEntity();
		Vector3f pos1 = entity1.getPosition();
		Vector3f pos2 = entity2.getPosition();
		Vector3f v1 = entity1.getVelocity();
		Vector3f v2 = entity2.getVelocity();
		
		final float MASS1 = 1.0f;
		final float MASS2 = 1.0f;
		final float COEFF_REST = 0.7f;
		//Vector3f entity1toEntity2Vec = new Vector3f(pos2.x-pos1.x, pos2.y-pos1.y, pos2.z-pos1.z);
		
		
		
		float v1dotv2 = Vector3f.dot(v1, v2);
		if(v1dotv2 < 0 || v1.lengthSquared() < 0.001 || v2.lengthSquared() < 0.001){
			System.out.println("BEFORE COLLISION v1="+v1+" v2="+v2);
			float v1Initial = v1.length();
			float v2Initial = v2.length();
			float v1Final = ((MASS2-MASS1)*v1Initial+2*MASS2*v2Initial)/(MASS1+MASS2);
			float v2Final = ((MASS1-MASS2)*v2Initial+2*MASS1*v1Initial)/(MASS1+MASS2);
			
			System.out.println("v1="+v1Initial+" v2="+v2Initial+" v1f="+v1Final+" v2f="+v2Final);
			
			Vector3f collisionDirection = new Vector3f(pos2.x-pos1.x, pos2.y-pos1.y, pos2.z-pos1.z);
			collisionDirection.normalise(collisionDirection);
			
			
			
			v1.x = -collisionDirection.x*v1Final;
			v1.y = -collisionDirection.y*v1Final;
			v1.x = -collisionDirection.z*v1Final;
			
			v2.x = collisionDirection.x*v2Final;
			v2.y = collisionDirection.y*v2Final;
			v2.z = collisionDirection.z*v2Final;
			System.out.println("AFTER COLLISION v1="+v1+" v2="+v2);
			//System.out.println("Collision detected! aabb1="+aabb1.getCenter()+" aabb2=" + aabb2.getCenter());
		}
		else{
			//System.out.println("Ignoring collision since objects are moving apart...");
		}
		
	}
	
}
