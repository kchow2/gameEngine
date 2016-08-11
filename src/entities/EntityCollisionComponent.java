package entities;

import physics.AABB;
import physics.CollisionManager;
import terrain.Terrain;

public class EntityCollisionComponent implements IGameComponent{
	private AABB aabb;
	private CollisionManager collisionManager;
	
	public EntityCollisionComponent(CollisionManager collisionManager, AABB aabb){
		this.collisionManager = collisionManager;
		this.aabb = aabb;
	}
	
	@Override
	public void onCreate(Entity e){
		collisionManager.addCollisionDetection(e, aabb);
	}
	
	@Override
	public void update(Entity e, Terrain terrain){
		aabb.updatePos(e);
	}
	
	@Override
	public void processInput(EntityMovement movement){
		
	}
	
	@Override
	public void onDestroy(Entity e){
		collisionManager.removeCollisonDetection(e);
	}
}
