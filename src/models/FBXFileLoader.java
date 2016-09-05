package models;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import physics.AABB;

public class FBXFileLoader {

	private static final int HEADER_LENGTH = 27;
	
	private byte[] fileData;
	List<Vertex> vertices = new ArrayList<Vertex>();
	List<Vector2f> uvs = new ArrayList<Vector2f>();
	List<Integer> uvIndices = new ArrayList<Integer>();
	List<Vector3f> normals = new ArrayList<Vector3f>();
	List<Integer> indices = new ArrayList<Integer>();
	
	int animationCurveCount = 0;
	int animationCurveNodeCount = 0;
	
	public ModelData loadFile(String filename){
		
		try{
			File file = new File(filename);
			fileData = Files.readAllBytes(file.toPath());
			
			if(!readHeader()){
				return null;
			}
			
			return readContent();
		}
		catch(IOException e){
			e.printStackTrace();
			return null;
		}

	}
	
	/**
	 * 
	 * @param in - the DataInputStream to read from.
	 * @return true if the header was read successfully, false if a problem was found.
	 * FBX header format:
	 * Bytes 0-20 are the string "Kaydara FBX Binary  \x00" (with 2 spaces at the end)
	 * Bytes 21 - 22: [0x1A, 0x00] (unknown but all observed files show these bytes).
	 * Bytes 23 - 26: unsigned int, the version number. 7300 for version 7.3 for example.
	 */
	private boolean readHeader(){
		final byte[] headerMagicValues = "Kaydara FBX Binary  \u0000\u001a\u0000".getBytes();
		
		if(fileData.length < HEADER_LENGTH){
			//WTF - header is wrong size!
			System.err.println("FBXFileLoader: header was the wrong size! Expected "+HEADER_LENGTH+" bytes but received "+fileData.length+".");
			return false;
		}
		
		//check to see if header matches the magic values
		for(int i = 0; i < headerMagicValues.length; i++){
			if(headerMagicValues[i] != fileData[i]){
				System.err.println("FBXFileLoader: header did not contain the magic string!");
				return false;
			}
		}
		
		return true;
	}
	
	private ModelData readContent(){
		//System.out.println("Reading the FBX file...");
		//System.out.println("File length = "+fileData.length);
		
		NodeRecord objectsNode = null;
		
		int currentFileOffset = HEADER_LENGTH;
		NodeRecord nodeRecord = readNodeRecord(currentFileOffset);
		
		while(nodeRecord != null){
			currentFileOffset = nodeRecord.endOffset;	//The last object is a NULL object with the endOffset field set to 0.
			if(currentFileOffset > fileData.length){
				System.err.println("Unexpected end of file!");
				break;
			}
			else if(currentFileOffset == 0){
				break;
			}
			
			if(nodeRecord.name.equals("Objects")){
				objectsNode = nodeRecord;
			}

			nodeRecord = readNodeRecord(currentFileOffset);
		}
		
		//found the objects node. This is really the only thing we care about...
		if(objectsNode != null){
			//printNodeTree(objectsNode, 0);
			NodeRecord geometryNode, verticesNode, indicesNode, layerElementNormalNode, normalsNode, layerElementUVNode, uvNode, uvIndexNode;
			
			geometryNode = objectsNode.getNodeByName("Geometry");
			if(geometryNode != null){
				verticesNode = geometryNode.getNodeByName("Vertices");
				indicesNode = geometryNode.getNodeByName("PolygonVertexIndex");
				layerElementNormalNode = geometryNode.getNodeByName("LayerElementNormal");
				layerElementUVNode = geometryNode.getNodeByName("LayerElementUV");
				
				if(verticesNode == null || indicesNode == null || layerElementNormalNode == null){
					System.err.println("Error: the Geometry node could not be read!");
					return null;
				}
					
				//read the vertices
				PropertyRecord verticesPropertyRecord = verticesNode.properties.get(0);
				if(verticesPropertyRecord.typeCode == 'd'){
					ByteBuffer in = ByteBuffer.wrap(verticesPropertyRecord.data);
					in.order(ByteOrder.LITTLE_ENDIAN);
					for(int i = 0; i < verticesPropertyRecord.data.length / 24; i++){
						float x = (float)in.getDouble();
						float y = (float)in.getDouble();
						float z = (float)in.getDouble();
						vertices.add(new Vertex(i, new Vector3f(x,y,z)));
					}
				}
				else{
					System.err.println("Vertices in unknown format!");
					return null;
				}
				
				//read the indices
				PropertyRecord indicesPropertyRecord = indicesNode.properties.get(0);
				if(indicesPropertyRecord.typeCode == 'i'){
					ByteBuffer in = ByteBuffer.wrap(indicesPropertyRecord.data);
					in.order(ByteOrder.LITTLE_ENDIAN);
					for(int i = 0; i < indicesPropertyRecord.data.length / 4; i++){
						int index = in.getInt();
						indices.add(index);
					}
				}
				else{
					System.err.println("Indices in unknown format!");
					return null;
				}
				
				normalsNode = layerElementNormalNode.getNodeByName("Normals");	//for some stupid reason the normals are nested in "LayerElementNormal"
				if(normalsNode != null){
					//read the normals
					PropertyRecord normalsPropertyRecord = normalsNode.properties.get(0);
					if(normalsPropertyRecord.typeCode == 'd'){
						ByteBuffer in = ByteBuffer.wrap(normalsPropertyRecord.data);
						in.order(ByteOrder.LITTLE_ENDIAN);
						for(int i = 0; i < normalsPropertyRecord.data.length / 24; i++){
							float x = (float)in.getDouble();
							float y = (float)in.getDouble();
							float z = (float)in.getDouble();
							normals.add(new Vector3f(x,y,z));
						}
					}
					else{
						System.err.println("Normals in unknown format!");
						return null;
					}
				}
				else{
					System.err.println("Error: LayerElementNormal node missing!");
					return null;
				}
				
				if(layerElementUVNode == null){
					System.err.println("Error: LayerElementUV node missing!");
					return null;
				} else {
					//read the UV texture coords
					uvNode = layerElementUVNode.getNodeByName("UV");
					if(uvNode != null){
						//read the normals
						PropertyRecord uvPropertyRecord = uvNode.properties.get(0);
						if(uvPropertyRecord.typeCode == 'd'){
							ByteBuffer in = ByteBuffer.wrap(uvPropertyRecord.data);
							in.order(ByteOrder.LITTLE_ENDIAN);
							for(int i = 0; i < uvPropertyRecord.data.length / 16; i++){
								float u = (float)in.getDouble();
								float v = (float)in.getDouble();
								uvs.add(new Vector2f(u,v));
							}
						}
						else{
							System.err.println("UVs in unknown format!");
							return null;
						}
					} else {
						System.err.println("UVs missing!");
						return null;
					}
					
					//read the UV indices
					uvIndexNode = layerElementUVNode.getNodeByName("UVIndex");
					if(uvIndexNode != null){
						//read the normals
						PropertyRecord uvIndexPropertyRecord = uvIndexNode.properties.get(0);
						if(uvIndexPropertyRecord.typeCode == 'i'){
							ByteBuffer in = ByteBuffer.wrap(uvIndexPropertyRecord.data);
							in.order(ByteOrder.LITTLE_ENDIAN);
							for(int i = 0; i < uvIndexPropertyRecord.data.length / 4; i++){
								int index = in.getInt();
								uvIndices.add(index);
							}
						}
						else{
							System.err.println("UV Indices in unknown format!");
							return null;
						} 
					} else {
						System.err.println("UV Indices missing!");
						return null;
					}
				}
			}
		}
		else {
			System.err.println("Error: the Geometry node is missing!");
			return null;
		}
		
		//The FBX model may contain quads and N-gons, which will need to be converted to triangles.
		//triangulateModelData() takes the old indices representing a mix of triangles, quads, and n-gons and converts them to a new indices list of only triangles
		List<Integer> indices2 = new ArrayList<Integer>();
		List<Integer> uvIndices2 = new ArrayList<Integer>();
		List<Vector3f> normals2 = new ArrayList<Vector3f>();
		triangulateModelData(indices, uvIndices, normals, indices2, uvIndices2, normals2);
		this.indices = indices2;
		this.uvIndices = uvIndices2;
		this.normals = normals2;
		
		condenseVertices(vertices, indices, uvs, normals, uvIndices);
		
		float verticesArray[] = new float[vertices.size()*3];
		float uvArray[] = new float[vertices.size()*2];
		float normalsArray[] = new float[vertices.size()*3];
		float furthest = convertDataToArrays(vertices, uvs, normals, verticesArray, uvArray, normalsArray);
		
		int[] indicesArray = convertIndicesListToArray(indices);
		
		AABB aabb = new AABB(furthest*1.41421356f,furthest*1.41421356f,furthest*1.41421356f);
		ModelData data = new ModelData(verticesArray, uvArray, normalsArray, indicesArray,
				furthest, aabb);
		
		System.out.println("animationCurveCount="+animationCurveCount+" animationCurveNodeCount="+animationCurveNodeCount);
		return data;
	}
	
	
	private NodeRecord readNodeRecord(int offset){
		
		NodeRecord res = new NodeRecord();
		ByteBuffer in = ByteBuffer.wrap(this.fileData);
		in.order(ByteOrder.LITTLE_ENDIAN);
		in.position(offset);
		
		res.startOffset = offset;
		res.endOffset = in.getInt();
		res.numProperties = in.getInt();
		res.propertyListLen = in.getInt();
		res.nameLen = in.get();
		byte[] nameBytes = new byte[res.nameLen];
		in.get(nameBytes);
		res.name = new String(nameBytes);
		
		for(int i = 0; i < res.numProperties; i++){
			PropertyRecord property = readPropertyRecord(in);
			res.addProperty(property);
		}

		//read nested nodes
		int nestedNodeOffset = res.startOffset+13+res.nameLen+res.propertyListLen;

		while(nestedNodeOffset != 0 && nestedNodeOffset < res.endOffset){
			NodeRecord nestedNode = readNodeRecord(nestedNodeOffset);
			res.addChild(nestedNode);
			nestedNodeOffset = nestedNode.endOffset;
		}
		
		
		if(res.name.contains("Animation")){
			printNodeTree(res, 0);
			if(res.name.equals("AnimationCurve")){
				animationCurveCount++;
			}
			else if(res.name.equals("AnimationCurveNode")){
				animationCurveNodeCount++;
			}
		}
		return res;
	}
	
	private PropertyRecord readPropertyRecord(ByteBuffer in){
		PropertyRecord res = new PropertyRecord();
		res.typeCode = (char)in.get();
		switch(res.typeCode){
		case 'Y':
			res.data = readBytes(in, 2);
			break;
		case 'C':
			res.data = readBytes(in, 1);
			break;
		case 'I':
			res.data = readBytes(in, 4);
			break;
		case 'F':
			res.data = readBytes(in, 4);
			break;
		case 'D':
			res.data = readBytes(in, 8);
			break;
		case 'L':
			res.data = readBytes(in, 8);
			break;
		case 'f':
			res.data = readArray(in, 4);
			break;
		case 'd':
			res.data = readArray(in, 8);
			break;
		case 'l':
			res.data = readArray(in, 8);
			break;
		case 'i':
			res.data = readArray(in, 4);
			break;
		case 'b':
			res.data = readArray(in, 1);
			break;
		case 'S':	//string
			res.data = readString(in);
			break;
		case 'R':	//string
			res.data = readString(in);
			break;
			
		}
		
		
		return res;
	}
	
	private byte[] readBytes(ByteBuffer in, int count){
		byte[] res = new byte[count];
		for(int i = 0; i < count; i++){
			res[i] = in.get();
		}
		return res;
	}
	
	private byte[] readArray(ByteBuffer in, int dataSize){
		int arrayLength = in.getInt();
		int encoding = in.getInt();
		int compressedLength = in.getInt();
		
		int sizeInBytes = arrayLength*dataSize;
		byte[] res = new byte[sizeInBytes];
		
		if(encoding != 0){	// If encoding is non-zero then the array is gzip compressed and will need to be inflated.
			try{
			    Inflater decompresser = new Inflater();
			    decompresser.setInput(fileData, in.position(), compressedLength);
			    int resultLength = decompresser.inflate(res);
			    decompresser.end();
			}catch(DataFormatException e){
				e.printStackTrace();
			}
		}
		else{	//no compression
			for(int i = 0; i < res.length; i++){
				res[i] = in.get();
			}
		}
		return res;
	}
	
	private byte[] readString(ByteBuffer in){
		int length = in.getInt();
		byte[] res = new byte[length];
		for(int i = 0; i < length; i++){
			res[i] = in.get();
		}
		return res;
	}
	
	/**
	 * Prints out a tree representation of the NodeRecord and all of its children.
	 * Useful for debugging.
	 * @param n - The NodeRecord to be printed out.
	 * @param depth - should be 0.
	 */
	private void printNodeTree(NodeRecord n, int depth){
		String depthStr = "";
		for(int i = 0; i < depth; i++){
			depthStr+="-";
		}
		System.out.println(depthStr+n);
		for(NodeRecord child:n.nestedNodes){
			printNodeTree(child, depth+1);
		}
	}
	
	/**
	 * Transforms the vertex data read from the FBX file to a form our game can use. Since our vertices can only have one normal and UV coord per Vertex, we need to create a new Vertex
	 * every time the same vertex is used for a different face but with a different normal or UV.
	 * @param vertices - A list of vertices in the model
	 * @param indices - A list of vertex indices representing faces of the model. Assumed to be ONLY triangles, and negative indices already corrected.
	 * @param uvs - A list of UV texture coordinates.
	 * @param normals - A list of normals for each index in indices. Must be the same length as indices.
	 * @param uvIndex - An index to a uv coordinate for each index in indices. Must be the same length as indices.
	 */
	private static void condenseVertices(List<Vertex> vertices, List<Integer> indices, List<Vector2f> uvs, List<Vector3f> normals, List<Integer> uvIndex) {	
		for(int i = 0; i < indices.size(); i++){
			int vertexIdx = indices.get(i);
			
			int uvIdx = uvIndex.get(i);
			int normalIdx = i;
			
			Vertex currentVertex = vertices.get(vertexIdx);
			if(currentVertex.hasSameTextureAndNormal(uvIdx, normalIdx)){
				continue;
			}
			else if (!currentVertex.isSet()) {
				currentVertex.setTextureIndex(uvIdx);
				currentVertex.setNormalIndex(normalIdx);
			} else {
				dealWithAlreadyProcessedVertex(currentVertex, i, uvIdx, normalIdx, indices, vertices);
			}
		}
	}
	
	private static void dealWithAlreadyProcessedVertex(Vertex previousVertex, int index, int newTextureIndex,
			int newNormalIndex, List<Integer> indices, List<Vertex> vertices) {
		if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
			//indices.add(previousVertex.getIndex());
		} else {
			Vertex anotherVertex = previousVertex.getDuplicateVertex();
			if (anotherVertex != null) {
				dealWithAlreadyProcessedVertex(anotherVertex, index, newTextureIndex, newNormalIndex,
						indices, vertices);
			} else {
				Vertex duplicateVertex = new Vertex(vertices.size(), previousVertex.getPosition());
				duplicateVertex.setTextureIndex(newTextureIndex);
				duplicateVertex.setNormalIndex(newNormalIndex);
				previousVertex.setDuplicateVertex(duplicateVertex);
				vertices.add(duplicateVertex);
				indices.set(index, duplicateVertex.getIndex());
			}

		}
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

	private static void print_v(List<Vertex> vals){
		for(Vertex v:vals){
			System.out.println("Pos:{"+v.getPosition().x+", "+v.getPosition().y+", "+v.getPosition().z+"} UVIndex:"+v.getTextureIndex()+" NormalIndex:"+v.getNormalIndex());
		}
	}
	private static void print_i(List<Integer> indices){
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for(Integer i:indices){
			sb.append(i);
			sb.append(", ");
		}
		sb.append("}");
		System.out.println(sb.toString());
	}
	
	private static void removeUnusedVertices(List<Vertex> vertices){
		for(Vertex vertex:vertices){
			if(!vertex.isSet()){
				vertex.setTextureIndex(0);
				vertex.setNormalIndex(0);
			}
		}
	}
	
	/**
	 * Takes a list of indices representing a mix of triangles, quads, and n-gons as input and generates a new list of indices converting everything to triangles only.
	 * @param indices - The set of vertex indices to process. This is a mix of triangles, quads, and n-gons. The end of a polygon is denoted by a negative index value. This value must be corrected by taking 2's complement before using it.
	 * @param uvIndices - The set of UV indices corresponding to the indices.
	 * @param normals - The set of normals corresponding to the indices.
	 * @param outIndices - The output indices after processing. These indices represent triangles only. This list will be either the same size or larger than the input list.
	 * @param outUVIndices - The new UVIndices corresponding to each of the indices.
	 * @param outNormals - The new normals corresponding to each of the indices.
	 */
	private static void triangulateModelData(List<Integer> indices, List<Integer> uvIndices, List<Vector3f> normals, List<Integer> outIndices, List<Integer> outUVIndices, List<Vector3f> outNormals){
		assert(indices.size()==uvIndices.size() && indices.size()==normals.size());
		
		List<Integer> accumulator = new ArrayList<Integer>();
		List<Integer> accumulatorUV = new ArrayList<Integer>();
		List<Vector3f> accumulatorNorm = new ArrayList<Vector3f>();
		for(int i=0;i<indices.size();i++){
			int index = indices.get(i);

			if(index < 0){		//when the index < 0, it indicates the end of a polygon.
				accumulator.add(-index-1);	//Need to correct the index by taking 2's complement
				accumulatorUV.add(uvIndices.get(i));
				accumulatorNorm.add(normals.get(i));
				
				assert(accumulator.size() >= 3);	//the accumulator contains the polygon we are currently processing.
				for(int v = 0; v < accumulator.size()-2;v++){
					//for a polygon of order N, we define N-2 triangles that make it up
					//we pivot around v1, defining triangles such that the polygon winding is preserved (T(v1,v2,v3), T(v1,v3,v4), T(v1,v4,v5) etc)
					//we also need to copy the indices for the texture coordinates and the normals					
					outIndices.add(accumulator.get(0));
					outIndices.add(accumulator.get(v+1));
					outIndices.add(accumulator.get(v+2));
					outUVIndices.add(accumulatorUV.get(0));
					outUVIndices.add(accumulatorUV.get(v+1));
					outUVIndices.add(accumulatorUV.get(v+2));
					outNormals.add(accumulatorNorm.get(0));
					outNormals.add(accumulatorNorm.get(v+1));
					outNormals.add(accumulatorNorm.get(v+2));
				}
				
				accumulator.clear();
				accumulatorUV.clear();
				accumulatorNorm.clear();
			}
			else{
				accumulator.add(index);
				accumulatorUV.add(uvIndices.get(i));
				accumulatorNorm.add(normals.get(i));
			}
		}
	}
	
	
	private class NodeRecord{
		public int startOffset, endOffset, numProperties, propertyListLen, nameLen;
		public String name;
		public List<PropertyRecord> properties = new ArrayList<PropertyRecord>();
		public List<NodeRecord> nestedNodes = new ArrayList<NodeRecord>();
		
		public void addProperty(PropertyRecord prop){
			properties.add(prop);
		}
		
		public void addChild(NodeRecord n){
			nestedNodes.add(n);
		}
		
		public NodeRecord getNodeByName(String name){
			for(NodeRecord n:nestedNodes){
				if(n.name.equals(name)){
					return n;
				}
			}
			return null;
		}
		
		public String toString(){
			String types = "";
			for(PropertyRecord p:properties){
				types+=p.toString2();
				types += " ";
			}
			return String.format("NodeRecord:{name:'%s' offset:%d children:%d sizeInBytes:%d properties:%d {%s}}", name, startOffset, nestedNodes.size(), endOffset-startOffset, numProperties, types);
		}
	}
	
	private class PropertyRecord{
		public char typeCode;
		public byte[] data;
		
		public String toString(){
			return String.valueOf(typeCode);
		}
		
		public String toString2(){
			ByteBuffer buf = ByteBuffer.wrap(data);
			buf.order(ByteOrder.LITTLE_ENDIAN);
			switch(typeCode){
			case 'Y':
				return "Short{"+buf.getShort()+"}";
			case 'C':
				return "Boolean{"+buf.get()+"}";
			case 'I':
				return "Int{"+buf.getInt()+"}";
			case 'F':
				return "Float{"+buf.getFloat()+"}";
			case 'D':
				return "Double{"+buf.getDouble()+"}";
			case 'L':
				return "Long{"+buf.getLong()+"}";
			case 'f':
				return "Float[]{ size:"+data.length/4+"}";
			case 'd':
				return "Double[]{ size:"+data.length/8+"}";
			case 'l':
				return "Long[]{ size:"+data.length/8+"}";
			case 'i':
				return "Int[]{ size:"+data.length/4+"}";
			case 'b':
				return "Boolean[]{ size:"+data.length+"}";
			case 'S':
				return "String{'"+new String(data)+"'}";
			}
			
			
			return "???{}";
		}
	}
}
