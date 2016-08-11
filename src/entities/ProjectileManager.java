package entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import terrain.Terrain;

public class ProjectileManager {
	private static ProjectileManager instance;
	
	private static EntityMovementManager movementManager;
	private static EntityRenderingManager renderingManager;
	private static List<Projectile> entities = new ArrayList<Projectile>();
	
	public ProjectileManager(EntityRenderingManager renderingManager, EntityMovementManager movementManager){
		ProjectileManager.movementManager = movementManager;
		ProjectileManager.renderingManager = renderingManager;
		instance = this;
	}
	
	public static ProjectileManager get(){
		return ProjectileManager.instance;
	}
	
	public List<Projectile> getEntities(){
		return entities;
	}
	
	public void addEntity(Projectile e){
		entities.add(e);
		movementManager.addEntity(e);
		renderingManager.addEntity(e);
	}
	
	public void update(Terrain terrain){
		Iterator<Projectile> iter = entities.iterator();
		while(iter.hasNext()){
			Projectile e = iter.next();
			e.update(terrain);
			
			if(!e.isAlive()){
				iter.remove();
				movementManager.removeEntity(e);
				renderingManager.removeEntity(e);
			}
		}
	}
	
	public void cleanUp(){
		entities.clear();
	}
}
