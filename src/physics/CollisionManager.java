package physics;

import java.util.ArrayList;

import entities.Player;

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
		for(AABB aabb1:aabbs){
			for(AABB aabb2:aabbs){
				if(aabb1.doesCollide(aabb2)){
					resolveCollision(aabb1, aabb2);
				}
			}
		}
	}
	
	private static void resolveCollision(AABB aabb1, AABB aabb2){
		System.out.println("Collision detected! aabb1="+aabb1.getCenter()+" aabb2=" + aabb2.getCenter());
	}
	
}
