package models;

public class RawModel {
	private static final int MAX_VBOS = 16;
	private int vaoId;
	private int[] vboIDs = new int[MAX_VBOS];
	private int vertexCount;
	
	public RawModel(int vaoId, int vertexCount){
		this.vaoId = vaoId;
		this.vertexCount = vertexCount;
	}
	
	public int getVaoID() {
		return vaoId;
	}
	
	public void setVboID(int attributeIndex, int vboID){
		vboIDs[attributeIndex] = vboID;
	}
	
	public int getVboID(int attributeIndex){
		return vboIDs[attributeIndex];
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
}
