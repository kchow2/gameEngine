package textures;

public class ModelTexture {
	private int textureID;
	private float reflectivity = 0.0f;
	private float shineDamper = 0.0f;
	
	
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
	
}
