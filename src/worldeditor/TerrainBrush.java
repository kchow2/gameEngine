package worldeditor;

import terrain.Terrain;

public class TerrainBrush {

	public enum BrushMode {PAINT, RAISE, LOWER, SMOOTH};
	public enum BrushShape {SQUARE, CIRCLE};
	
	//a shape to do terrain modification in world editor
	float brushSize;	//the radius of the brush effect on the terrain
	float brushStrength;
	float brushHeight;
	BrushMode mode;
	BrushShape shape;
	
	public TerrainBrush(float size, BrushMode mode, BrushShape shape, float strength, float brushHeight){
		setBrushSize(size);
		setBrushMode(mode);
		setBrushShape(shape);
		setBrushStrength(strength);
		setBrushHeight(brushHeight);
	}
	
	
	public void setBrushSize(float size){
		this.brushSize = size;
	}
	
	public void setBrushMode(BrushMode mode){
		this.mode = mode;
	}
	
	public void setBrushShape(BrushShape shape){
		this.shape = shape;
	}
	
	public void setBrushStrength(float strength){
		this.brushStrength = strength;
	}
	
	public void setBrushHeight(float height){
		this.brushHeight = height;
	}
	
	public void applyBrush(Terrain terrain, float xCenter, float zCenter, float dt){
		assert(brushSize > 0);
		int gridX = terrain.worldToGridX(xCenter);
		int gridZ = terrain.worldToGridZ(zCenter);

		if(gridX < 0 || gridX > terrain.getGridXCount())
			return;
		if(gridZ < 0 || gridZ > terrain.getGridZCount())
			return;
		
		int halfLength = (int)brushSize - 1;
		int isOdd = brushSize % 2 == 0 ? 0 : 1;
		switch(shape){
		case SQUARE:
			for(int j=-halfLength; j<halfLength+isOdd; j++){
				for(int i=-halfLength; i<halfLength+isOdd; i++){
					modifyTerrainHeight(terrain, gridX+i, gridZ+j, mode, brushHeight, brushStrength, dt);
				}
			}
			break;
		case CIRCLE:
			for(int j=-halfLength; j<halfLength+isOdd; j++){
				for(int i=-halfLength; i<halfLength+isOdd; i++){
					if(i*i+j*j < brushSize*brushSize ){
						modifyTerrainHeight(terrain, gridX+i, gridZ+j, mode, brushHeight, brushStrength, dt);
					}
				}
			}
			break;
		}
		terrain.refreshTerrain();
	}
	
	private void modifyTerrainHeight(Terrain terrain, int gridX, int gridZ, BrushMode mode, float brushHeight, float strength, float dt){
		float oldHeight = terrain.getTerrainGridHeight(gridX, gridZ);
		
		float newHeight = oldHeight;
		switch(mode){
		case PAINT:
			newHeight = brushHeight;
			break;
		case RAISE:
			newHeight = oldHeight+strength*dt;
			break;
		case LOWER:
			newHeight = oldHeight-strength*dt;
			break;
		case SMOOTH:
			float hl = terrain.getTerrainGridHeight(gridX-1, gridZ);
			float hr = terrain.getTerrainGridHeight(gridX+1, gridZ);
			float ht = terrain.getTerrainGridHeight(gridX, gridZ+1);
			float hb = terrain.getTerrainGridHeight(gridX, gridZ-1);
			//newHeight = (float)Math.sqrt((hl*hl + hr*hr + ht*ht + hb*hb)/4.0f);
			newHeight = (hl+hr+ht+hb) / 4.0f;
			break;
		}	
		
		terrain.setTerrainHeight(gridX, gridZ, newHeight);
	}
}
