package entities;

import terrain.Terrain;

public interface IGameComponent {
	public void onCreate(Entity e);
	public void processInput(EntityMovement movement);
	public void update(Entity e, Terrain terrain);
	public void onDestroy(Entity e);
}
