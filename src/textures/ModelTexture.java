package textures;

public class ModelTexture {
	private int textureID;
	private float reflectivity = 0.0f;
	private float shineDamper = 1.0f;
	
	boolean hasTransparency = false;
	boolean useFakeLighting = false;
	
	private int numberOfRows = 1;
	
	public ModelTexture(int id){
		this.textureID = id;
	}

	public int getID() {
		return textureID;
	}
	
	public float getReflectivity() {
		return reflectivity;
	}

	public float getShineDamper() {
		return shineDamper;
	}

	public void setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
	}

	public void setShineDamper(float shineDamper) {
		this.shineDamper = shineDamper;
	}

	public boolean isTransparent() {
		return hasTransparency;
	}

	public void setTransparent(boolean hasTransparency) {
		this.hasTransparency = hasTransparency;
	}

	public boolean getUseFakeLighting() {
		return useFakeLighting;
	}

	public void setUseFakeLighting(boolean useFakeLighting) {
		this.useFakeLighting = useFakeLighting;
	}

	public int getNumberOfRows() {
		return numberOfRows;
	}

	public void setNumberOfRows(int numberOfRows) {
		this.numberOfRows = numberOfRows;
	}
	
	

}
