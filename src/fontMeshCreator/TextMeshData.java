package fontMeshCreator;

/**
 * Stores the vertex data for all the quads on which a text will be rendered.
 * @author Karl
 *
 */
public class TextMeshData {
	
	private float[] vertexPositions;
	private float[] textureCoords;
	
	protected TextMeshData(float[] vertexPositions, float[] textureCoords){
		this.vertexPositions = vertexPositions;
		this.textureCoords = textureCoords;
	}

	public float[] getVertexPositions() {
		return vertexPositions;
	}

	public float[] getTextureCoords() {
		return textureCoords;
	}

	public int getVertexCount() {
		return vertexPositions.length/2;
	}
	
	public float getWidth(){
		if(vertexPositions.length == 0)
			return 0;
		
		float min = vertexPositions[0];
		float max = vertexPositions[0];
		for(int i = 0; i < vertexPositions.length; i+=2){
			if(vertexPositions[i] > max)
				max = vertexPositions[i];
			if(vertexPositions[i] < min)
				min = vertexPositions[i];
		}
		return (max - min) / 2.0f;	//convert from GL to screen coords
	}
	
	public float getHeight(){
		if(vertexPositions.length == 0)
			return 0;
		
		float min = vertexPositions[1];
		float max = vertexPositions[1];
		for(int i = 1; i < vertexPositions.length; i+=2){
			if(vertexPositions[i] > max)
				max = vertexPositions[i];
			if(vertexPositions[i] < min)
				min = vertexPositions[i];
		}
		return (max - min) / 2.0f;	//convert from GL to screen coords
	}

}
