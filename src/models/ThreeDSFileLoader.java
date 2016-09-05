package models;

import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import physics.AABB;

/**
 * 3DS file loader. 
 * For more info on the 3DS spec, see http://spacesimulator.net/tutorials/3ds_loader_tutorial.html, and http://www.martinreddy.net/gfx/3d/3DS.spec
 * @author Kevin
 *
 */
public class ThreeDSFileLoader {
	private static final int MAIN3DS = 0x4D4D;
	private static final int EDIT3DS = 0x3D3D;
	private static final int EDIT_MATERIAL = 0xAFFF;
	private static final int EDIT_OBJECT = 0x4000;
	private static final int OBJ_TRIMESH = 0x4100;
	private static final int TRI_VERTEXL = 0x4110;
	private static final int TRI_VERTEXOPTIONS = 0x4111;
	private static final int TRI_FACEL1 = 0x4120;
	private static final int TRI_MAPPINGCOORDS = 0x4140;
	private static final int TRI_MAPPINGSTANDARD = 0x4170;
	private static final int KEYF3DS = 0xB000;
	 
	private static final int COL_RGB = 0x0010;	//COLOR CHUNK TYPES
	private static final int COL_TRU = 0x0011;
	private static final int COL_UNK = 0x0013;
	
	private static final int HEADER_SIZE = 6;
	
	byte[] fileData;
	String objName;
	private List<Vertex> vertices = new ArrayList<Vertex>();
	private List<Integer> indices = new ArrayList<Integer>();
	private List<Vector2f> textureCoords = new ArrayList<Vector2f>();
	
	public ModelData loadFile(String filename){
		
		try{
			File file = new File(filename);
			fileData = Files.readAllBytes(file.toPath());
			
			ByteBuffer data = ByteBuffer.wrap(fileData).order(ByteOrder.LITTLE_ENDIAN);
			if(!readMainChunk(data))
				return null;
			
			return createModelData();
		}
		catch(IOException e){
			e.printStackTrace();
			return null;
		}

	}
	
	private ModelData createModelData(){
		int[] indicesArray = convertIndicesListToArray(indices);
		float[] verticesArray = new float[vertices.size()*3];
		float[] uvArray = new float[vertices.size()*2];
		float[] normalsArray = new float[vertices.size()*3];
		List<Vector3f> normals = calculateNormals(vertices, indices);
		for(int i = 0; i < vertices.size(); i++){
			vertices.get(i).setNormalIndex(i);
			vertices.get(i).setTextureIndex(i);
		}
		float furthest = convertDataToArrays(vertices, textureCoords, normals, verticesArray, uvArray, normalsArray);
		
		AABB aabb = new AABB(furthest,furthest,furthest);
		return new ModelData(verticesArray, uvArray, normalsArray, indicesArray, 0, aabb);
	}
	
	private List<Vector3f> calculateNormals(List<Vertex> vertices, List<Integer> indices){
		List<Vector3f> faceNormals = new ArrayList<Vector3f>();
		int triangleCount = indices.size()/3;
		for(int i = 0;i < triangleCount; i++){		
			Vertex v0 = vertices.get(indices.get(3*i));
			Vertex v1 = vertices.get(indices.get(3*i+1));
			Vertex v2 = vertices.get(indices.get(3*i+2));
			
			Vector3f e1 = Vector3f.sub(v1.getPosition(), v0.getPosition(), null);
			Vector3f e2 = Vector3f.sub(v2.getPosition(), v0.getPosition(), null);
			Vector3f n = Vector3f.cross(e1, e2, null);
			n.normalise();
			faceNormals.add(n);
		}
		
		//get all normals of the faces the vertex is associated with and get an average. We'll use this as the normal for that vertex.
		List<Vector3f> result = new ArrayList<Vector3f>();
		int faceCount = 0;	//how many faces this vertex is a part of
		for(int v = 0; v < vertices.size(); v++){
			Vector3f averageNormal = new Vector3f();
			for(int i = 0; i < indices.size(); i++){
				if(indices.get(i) == v){
					Vector3f faceNormal = faceNormals.get(i/3);
					averageNormal.x += faceNormal.x;
					averageNormal.y += faceNormal.y;
					averageNormal.z += faceNormal.z;
					faceCount++;
				}
			}
			if(faceCount == 0){
				result.add(new Vector3f(0,1,0));	//WTF: the vertex is not used by any faces. In this case, it doesn't even matter what we provide as the normal since it wont be used anyways/
			} else {
				averageNormal.scale( 1.0f / faceCount);
				result.add(averageNormal);
			}
			faceCount = 0;
		}
		assert(result.size()==vertices.size());
		
		return result;
	}
	
	private boolean readMainChunk(ByteBuffer data){
		ChunkHeader chunkHeader = new ChunkHeader();
		readChunkHeader(data, chunkHeader);
		
		if(chunkHeader.chunkID != MAIN3DS)
			return false;
		
		//read sub chunks
		ChunkHeader subChunkHeader = new ChunkHeader();
		while(hasNextSubchunk(data, chunkHeader)){
			readChunkHeader(data, subChunkHeader);
			
			switch(subChunkHeader.chunkID){
			case EDIT3DS:
				readEditChunk(data, subChunkHeader);
				break;
			case KEYF3DS:
				System.out.println("found keyframer chunk");
				break;
			}
			skipToEndOfChunk(data, subChunkHeader);
		}
		return true;
	}
	
	private void readEditChunk(ByteBuffer data, ChunkHeader header){
		while(hasNextSubchunk(data, header)){
			ChunkHeader subChunkHeader = new ChunkHeader();
			readChunkHeader(data, subChunkHeader);
			
			switch(subChunkHeader.chunkID){
			case EDIT_OBJECT:
				readObjectChunk(data, subChunkHeader);
				break;
			case EDIT_MATERIAL:
				break;
			}
			skipToEndOfChunk(data, subChunkHeader);
		}
	}
	
	private void readObjectChunk(ByteBuffer data, ChunkHeader header){
		this.objName = readString(data);	
		while(hasNextSubchunk(data, header)){
			ChunkHeader subChunkHeader = new ChunkHeader();
			readChunkHeader(data, subChunkHeader);
			
			switch(subChunkHeader.chunkID){
			case OBJ_TRIMESH:
				readTriMeshChunk(data, subChunkHeader);
				break;
			}
			skipToEndOfChunk(data, subChunkHeader);
		}
	}
	
	private void readTriMeshChunk(ByteBuffer data, ChunkHeader header){
		while(hasNextSubchunk(data, header)){
			ChunkHeader subChunkHeader = new ChunkHeader();
			readChunkHeader(data, subChunkHeader);
			
			switch(subChunkHeader.chunkID){
			case TRI_VERTEXL:
				readVertexList(data, subChunkHeader);
				break;
			case TRI_VERTEXOPTIONS:
				break;
			case TRI_FACEL1:
				readFacesList(data, subChunkHeader);
				break;
			case TRI_MAPPINGCOORDS:
				readTextureCoordsList(data, subChunkHeader);
				break;
			case TRI_MAPPINGSTANDARD:
				break;
				
			}
			skipToEndOfChunk(data, subChunkHeader);
		}
	}
	
	private void readVertexList(ByteBuffer data, ChunkHeader header){	
		int vertexCount = data.getShort();
		for(int i = 0; i < vertexCount; i++){
			float x = data.getFloat();
			float y = data.getFloat();
			float z = data.getFloat();
			this.vertices.add(new Vertex(i, new Vector3f(x,y,z)));
		}
		//System.out.println(String.format("read %d vertices.", vertexCount));
	}
	
	private void readFacesList(ByteBuffer data, ChunkHeader header){
		int triangleCount = data.getShort();
		for(int i = 0; i < triangleCount; i++){
			this.indices.add((int)data.getShort());
			this.indices.add((int)data.getShort());
			this.indices.add((int)data.getShort());
			int unused = data.getShort();	//'face flag'. Unknown what this is used for.
		}
		//System.out.println(String.format("read %d triangles (%d indices).", triangleCount, indices.size()));
	}
	
	private void readTextureCoordsList(ByteBuffer data, ChunkHeader header){
		int vertexCount = data.getShort();
		for(int i = 0; i < vertexCount; i++){
			float u = data.getFloat();
			float v = data.getFloat();
			this.textureCoords.add(new Vector2f(u,v));
		}
		//System.out.println(String.format("read %d texture coords.", vertexCount));
	}
	
	private void readChunkHeader(ByteBuffer data, ChunkHeader header){
		try{
			header.startOffset = data.position();
			header.chunkID = data.getShort();
			header.chunkLength = data.getInt();
		} catch (BufferUnderflowException e){
			e.printStackTrace();
		}
		
		assert(header.chunkLength > 0);
		assert(header.startOffset + header.chunkLength < data.limit());
	}
	
	private boolean hasNextSubchunk(ByteBuffer data, ChunkHeader header){
		return data.position() + HEADER_SIZE < header.startOffset + header.chunkLength;
	}
	
	private void skipToEndOfChunk(ByteBuffer data, ChunkHeader header){
		data.position(header.startOffset + header.chunkLength);
	}
	
	private String readString(ByteBuffer data){
		StringBuilder sb = new StringBuilder();
		byte c;
		while((c = data.get()) != 0){
			sb.append((char) c);
		}
		return sb.toString();
	}
	
	private static int[] convertIndicesListToArray(List<Integer> indices) {
		int[] indicesArray = new int[indices.size()];
		for (int i = 0; i < indicesArray.length; i++) {
			indicesArray[i] = indices.get(i);
		}
		return indicesArray;
	}

	private static float convertDataToArrays(List<Vertex> vertices, List<Vector2f> textures,
			List<Vector3f> normals, float[] verticesArray, float[] texturesArray,
			float[] normalsArray) {
		float furthestPoint = 0;
		for (int i = 0; i < vertices.size(); i++) {
			Vertex currentVertex = vertices.get(i);
			if (currentVertex.getLength() > furthestPoint) {
				furthestPoint = currentVertex.getLength();
			}
			Vector3f position = currentVertex.getPosition();
			Vector2f textureCoord = textures.get(currentVertex.getTextureIndex());
			Vector3f normalVector = normals.get(currentVertex.getNormalIndex());
			verticesArray[i * 3] = position.x;
			verticesArray[i * 3 + 1] = position.y;
			verticesArray[i * 3 + 2] = position.z;
			texturesArray[i * 2] = textureCoord.x;
			texturesArray[i * 2 + 1] = 1 - textureCoord.y;
			normalsArray[i * 3] = normalVector.x;
			normalsArray[i * 3 + 1] = normalVector.y;
			normalsArray[i * 3 + 2] = normalVector.z;
		}
		return furthestPoint;
	}
	
	
	private class ChunkHeader{
		int startOffset, chunkID, chunkLength;
		
		public String toString(){
			return String.format("Chunk{chunkID: 0x%x startOffset: %d length:%d", chunkID, startOffset, chunkLength);
		}
	}
}
