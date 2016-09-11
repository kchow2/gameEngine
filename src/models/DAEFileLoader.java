package models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import physics.AABB;

public class DAEFileLoader {
	DAEParserHandler handler = new DAEParserHandler();

	public ModelData load(String filename) {
		try {
			File file = new File(filename);
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(file, handler);
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return handler.getModel();
	}

	private class DAEParserHandler extends DefaultHandler {
		
		private boolean library_geometries = false;
		private boolean geometry_node = false;
		private boolean mesh_node = false;
		private boolean polyList_node = false;
		private boolean source_node = false;
		private boolean vertices_node = false;
		private boolean floatArray_node = false;
		private boolean vCount_node = false;
		private boolean p_node = false;
		
		ColladaGeometry geometry;
		ColladaMesh mesh;
		ColladaSource source;
		ColladaPolyList polyList;
		ColladaFloatArray floatArray;
		ColladaNameArray nameArray;
		ColladaInput input;
		ColladaVertices vertices;
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if(qName.equals("library_geometries")){
				library_geometries = true;
			} 
			else if(library_geometries && qName.equals("geometry")){
				beginGeometry(attributes.getValue("id"), attributes.getValue("name"));
			}
			else if(geometry_node && qName.equals("mesh")){
				beginMesh(geometry);
			}
			else if(mesh_node && qName.equals("polylist")){
				beginPolyList(attributes.getValue("material"), attributes.getValue("count"));
			}
			else if(mesh_node && qName.equals("source")){
				beginSource(attributes.getValue("id"));
			}
			else if(mesh_node && qName.equals("vertices")){
				beginVertices(attributes.getValue("id"));
			}
			else if(source_node && qName.equals("float_array")){
				beginFloatArray(attributes.getValue("id"), attributes.getValue("count"));
			}
			else if(polyList_node && qName.equals("input")){
				beginInput(attributes.getValue("semantic"), attributes.getValue("source"), attributes.getValue("offset"), attributes.getValue("set"));
			}
			else if(polyList_node && qName.equals("vcount")){
				beginVCount();
			}
			else if(polyList_node && qName.equals("p")){
				beginP();
			}
			else if(vertices_node && qName.equals("input")){
				beginVerticesInput(attributes.getValue("semantic"), attributes.getValue("source"));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(qName.equals("library_geometries")){
				library_geometries = false;
			}
			else if(library_geometries && qName.equals("geometry")){
				endGeometry();
			}
			else if(geometry_node && qName.equals("mesh")){
				endMesh();
			}
			else if(mesh_node && qName.equals("polylist")){
				endPolyList();
			}
			else if(mesh_node && qName.equals("source")){
				endSource();
			}
			else if(mesh_node && qName.equals("vertices")){
				endVertices();
			}
			else if(source_node && qName.equals("float_array")){
				endFloatArray();
			}
			else if(polyList_node && qName.equals("input")){
				endInput();
			}
			else if(polyList_node && qName.equals("vcount")){
				endVCount();
			}
			else if(polyList_node && qName.equals("p")){
				endP();
			}
			else if(vertices_node && qName.equals("input")){
				endVerticesInput();
			}
		}

		@Override
		public void characters(char ch[], int start, int length) throws SAXException {
			if(floatArray_node){
				try{
					String[] floats = new String(ch, start, length).split(" ");
					for(String f:floats){
						floatArray.data.add(Float.valueOf(f));
					}
				} catch(NumberFormatException e){
					System.err.println("DAEFileLoader: error reading floatArray!");
				}
			}
			else if(vCount_node){
				try{
					String[] vals = new String(ch, start, length).split(" ");
					for(String v:vals){
						this.polyList.vcount.add(Integer.valueOf(v));
					}
				} catch(NumberFormatException e){
					System.err.println("DAEFileLoader: error reading vcount value!");
				}
			}
			else if(p_node){
				try{
					String[] vals = new String(ch, start, length).split(" ");
					for(String v:vals){
						this.polyList.indices.add(Integer.valueOf(v));
					}
				} catch(NumberFormatException e){
					System.err.println("DAEFileLoader: error reading vcount value!");
				}
			}
		}
		
		//returns a model in the format used by our game from the model data we read from the collada file.
		public ModelData getModel(){
			if(!isDataValid()){
				System.err.println("Error: data failed to validate!");
				return null;
			}
			
			String positionsSourceID = this.geometry.mesh.vertices.input.source;
			String normalsSourceID = getInputBySemantic("NORMAL").source;
			String texCoordSourceID = getInputBySemantic("TEXCOORD").source;
			ColladaSource positionsSource = findSourceByID(positionsSourceID);
			ColladaSource normalsSource = findSourceByID(normalsSourceID);
			ColladaSource texCoordsSource = findSourceByID(texCoordSourceID);
			
			int vOffset = Integer.valueOf(getInputBySemantic("VERTEX").offset);
			int nOffset = Integer.valueOf(getInputBySemantic("NORMAL").offset);
			int tOffset = Integer.valueOf(getInputBySemantic("TEXCOORD").offset);
			
			List<Integer> p = this.geometry.mesh.polyList.indices;
			List<Integer> vcount = this.geometry.mesh.polyList.vcount;
			List<Vertex> vertices = new ArrayList<Vertex>();
			List<Vector3f> normals = new ArrayList<Vector3f>();
			List<Vector2f> texCoords = new ArrayList<Vector2f>();
			List<Integer> indices = new ArrayList<Integer>();
			List<Integer> normalIndices = new ArrayList<Integer>();
			List<Integer> uvIndices = new ArrayList<Integer>();
			for(int i = 0; i < positionsSource.floatArray.data.size()/3; i++){
				float x = positionsSource.floatArray.data.get(3*i);
				float y = positionsSource.floatArray.data.get(3*i+1);
				float z = positionsSource.floatArray.data.get(3*i+2);
				vertices.add(new Vertex(i, new Vector3f(x,y,z)));
			}
			for(int i = 0; i < normalsSource.floatArray.data.size()/3; i++){
				float x = normalsSource.floatArray.data.get(3*i);
				float y = normalsSource.floatArray.data.get(3*i+1);
				float z = normalsSource.floatArray.data.get(3*i+2);
				normals.add(new Vector3f(x,y,z));
			}
			for(int i = 0; i < texCoordsSource.floatArray.data.size()/2; i++){
				float x = texCoordsSource.floatArray.data.get(2*i);
				float y = texCoordsSource.floatArray.data.get(2*i+1);
				texCoords.add(new Vector2f(x,y));
			}
			
			for(int face = 0; face < vcount.size(); face++ ){
				int vertexCount = vcount.get(face);
				if(vertexCount != 3){
					System.err.println("Error: the model is not triangulated!");
					return null;
				}
				for(int v = 0; v < vertexCount; v++){
					//process a single triangle
					int vIndex = p.get(face*9+v*3+vOffset);
					int nIndex = p.get(face*9+v*3+nOffset);
					int tIndex = p.get(face*9+v*3+tOffset);
					
					indices.add(vIndex);
					normalIndices.add(nIndex);
					uvIndices.add(tIndex);
				}
			}
			
			System.out.println("//////////////////////");
			System.out.println("vertices:"+vertices.size());
			System.out.println("normals:"+normals.size());
			System.out.println("uvs:"+texCoords.size());
			System.out.println("indices:"+indices.size());
			System.out.println("normalIndices:"+normalIndices.size());
			System.out.println("uvIndices:"+uvIndices.size());
			System.out.println("//////////////////////");
			
			for(int i = 0; i < vertices.size(); i++){
				System.out.println(vertices.get(i).getPosition());
			}
			System.out.println("////////////////////////");
			System.out.println(p.toString());
			for(int i = 0; i < indices.size()/3; i++){
				System.out.println(indices.get(i)+","+indices.get(i+1)+","+indices.get(i+2));
			}
			
			condenseVertices(vertices, normals, texCoords, indices, normalIndices, uvIndices);
			float verticesArray[] = new float[vertices.size()*3];
			float uvArray[] = new float[vertices.size()*2];
			float normalsArray[] = new float[vertices.size()*3];
			float furthest = convertDataToArrays(vertices, texCoords, normals, verticesArray, uvArray, normalsArray);
			
			int[] indicesArray = convertIndicesListToArray(indices);
			
			AABB aabb = new AABB(furthest,furthest,furthest);
			ModelData modelData = new ModelData(verticesArray, uvArray, normalsArray, indicesArray,
					furthest, aabb);
			
			return modelData;
		}
		
		/**
		 * Transforms the vertex data read from the DAE file to a form our game can use. Since our vertices can only have one normal and UV coord per Vertex, we need to create a new Vertex
		 * every time the same vertex is used for a different face but with a different normal or UV.
		 * @param vertices - A list of vertices in the model
		 * @param normals - A list of normals
		 * @param uvs - A list of UV texture coordinates.
		 * @param indices - A list of vertex indices representing faces of the model. Assumed to be ONLY triangles.
		 * @param normalIndices - A list of normal indices. Must be the same length as indices.
		 * @param uvIndices - An list of uv indices. Must be the same length as indices.
		 */
		private void condenseVertices(List<Vertex> vertices, List<Vector3f> normals, List<Vector2f> uvs, List<Integer> indices, List<Integer> normalIndices, List<Integer> uvIndices) {	
			for(int i = 0; i < indices.size(); i++){
				int vertexIdx = indices.get(i);
				int normalIdx = normalIndices.get(i);
				int uvIdx = uvIndices.get(i);
				
				
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
		
		private void dealWithAlreadyProcessedVertex(Vertex previousVertex, int index, int newTextureIndex,
				int newNormalIndex, List<Integer> indices, List<Vertex> vertices) {
			if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
				
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
		
		private int[] convertIndicesListToArray(List<Integer> indices) {
			int[] indicesArray = new int[indices.size()];
			for (int i = 0; i < indicesArray.length; i++) {
				indicesArray[i] = indices.get(i);
			}
			return indicesArray;
		}

		private float convertDataToArrays(List<Vertex> vertices, List<Vector2f> textures,
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
		
		private void beginGeometry(String geometryID, String geometryName){
			geometry_node = true;
			ColladaGeometry geom = new ColladaGeometry();
			geom.geometryID = geometryID;
			geom.geometryName = geometryName;
			this.geometry = geom;
		}
		
		private void endGeometry(){
			geometry_node = false;
		}
		
		private void beginMesh(ColladaGeometry geometry){
			mesh_node = true;
			this.mesh = new ColladaMesh();
		}
		
		private void endMesh(){
			mesh_node = false;
			geometry.mesh = mesh;
		}
		
		private void beginPolyList(String material, String count){
			polyList_node = true;
			this.polyList = new ColladaPolyList();
		}
		
		private void endPolyList(){
			polyList_node = false;
			this.mesh.polyList = this.polyList;
		}
		
		private void beginSource(String id){
			source_node = true;
			this.source = new ColladaSource();
			this.source.id = id;
		}
		
		private void endSource(){
			source_node = false;
			this.mesh.sources.add(source);
		}
		
		private void beginVertices(String id){
			vertices_node = true;
			this.vertices = new ColladaVertices();
		}
		
		private void endVertices(){
			vertices_node = false;
			this.mesh.vertices = this.vertices;
		}
		
		private void beginFloatArray(String id, String length){
			floatArray_node = true;
			this.floatArray = new ColladaFloatArray();
			this.floatArray.id = id;
		}
		
		private void endFloatArray(){
			floatArray_node = false;
			this.source.floatArray = floatArray;
		}
		
		private void beginInput(String semantic, String source, String offset, String set){
			this.input = new ColladaInput();
			this.input.semantic = semantic;
			this.input.source = source.substring(1);
			this.input.offset = offset;
			this.input.set = set;		
		}
		
		private void endInput(){
			polyList.inputs.add(this.input);
		}
		
		private void beginVCount(){
			vCount_node = true;
		}
		
		private void endVCount(){
			vCount_node = false;
		}
		private void beginP(){
			p_node = true;
		}
		private void endP(){
			p_node = false;
		}
		private void beginVerticesInput(String semantic, String source){
			this.vertices.input = new ColladaInput();
			this.vertices.input.semantic = semantic;
			this.vertices.input.source = source.substring(1);
		}
		private void endVerticesInput(){
			
		}
		
		private boolean isDataValid(){
			if(this.geometry == null){
				System.err.println("Error: failed to read geometry.");
				return false;
			}
			else if(this.geometry.mesh == null){
				System.err.println("Error: failed to read mesh.");
				return false;
			}
			else if(this.geometry.mesh.vertices == null){
				System.err.println("Error: failed to read vertices.");
				return false;
			}
			else if(this.geometry.mesh.vertices.input == null){
				System.err.println("Error: failed to read vertices input source.");
				return false;
			}
			else if(this.geometry.mesh.polyList == null){
				System.err.println("Error: failed to read polylist.");
				return false;
			}
			else if(this.geometry.mesh.sources.size() < 3){
				System.err.println("Error: failed to read sources.");
				return false;
			}
			else if(getInputBySemantic("NORMAL") == null){
				System.err.println("Error: failed to read normals.");
				return false;
			}
			else if(getInputBySemantic("TEXCOORD") == null){
				System.err.println("Error: failed to read tex coords.");
				return false;
			}
			
			return true;
		}
		
		public ColladaSource findSourceByID(String id){
			for(ColladaSource src: this.geometry.mesh.sources){
				if(src.id.equals(id))
					return src;
			}
			return null;
		}
		public ColladaInput getInputBySemantic(String semantic){
			for(ColladaInput input:this.geometry.mesh.polyList.inputs){
				if(input.semantic.equals(semantic))
					return input;
			}
			return null;
		}
		//////////////////////////////////////////////////////
		/////////COLLADA classes for storing data/////////////
		//////////////////////////////////////////////////////
		
		private class ColladaGeometry {
			public String geometryID;
			public String geometryName;
			public ColladaMesh mesh;
			public String toString(){ return "{geometryID:'"+geometryID+"' geometryName:'"+geometryName+"' Mesh:"+mesh+"}";}
		}
		
		private class ColladaMesh {
			List<ColladaSource> sources = new ArrayList<ColladaSource>();
			ColladaPolyList polyList;
			ColladaVertices vertices;
			public String toString(){ return "{"+"Sources:"+sources.size()+" polyList:"+polyList+"}";}
		}
		
		private class ColladaSource {
			String id;
			ColladaFloatArray floatArray;
			ColladaNameArray nameArray;
			public String toString(){ String type = floatArray == null ? "name_array" : "float_array";return String.format("{id:%s, type=%s}", id, type); }
		}
		
		private class ColladaPolyList{
			String material;
			int polyCount;
			int indexCount;
			List<Integer> vcount = new ArrayList<Integer>();
			List<Integer> indices = new ArrayList<Integer>();
			List<ColladaInput> inputs = new ArrayList<ColladaInput>();
			public String toString(){ return "{inputs:"+inputs.size()+" indices:"+indices.size()+"}";}
		}
		
		private class ColladaInput{
			String semantic, source, offset, set;
		}
		
		private class ColladaFloatArray{
			String id;
			List<Float> data = new ArrayList<Float>();
		}
		private class ColladaNameArray{
			String id;
			List<String> data = new ArrayList<String>();
		}
		private class ColladaVertices{
			String id;
			ColladaInput input;
		}
	}
}
