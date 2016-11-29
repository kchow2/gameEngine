package renderEngine;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import physics.AABB;
import physics.CollisionManager;
import shaders.WireframeShader;
import toolbox.Maths;

public class DebugBoundingBoxRenderer {
	private WireframeShader shader;
	private RawModel boxModel;
	
	public DebugBoundingBoxRenderer(Loader loader, WireframeShader shader, Matrix4f projectionMatrix){
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
		
		loadBoxVAO(loader);
	}
	
	public void loadBoxVAO(Loader loader){
		//defines a unit cube centered on the origin
		float[] positions = {
				-.5f,-.5f, .5f,
				-.5f, .5f, .5f,
				 .5f, .5f, .5f,
				 .5f,-.5f, .5f,
				-.5f,-.5f,-.5f,
				-.5f, .5f,-.5f,
				 .5f, .5f,-.5f,
				 .5f,-.5f,-.5f,
		};
		int indices[] = {
				0,2,3,
				0,3,2,
				3,6,2,
				3,7,6,
				7,4,5,
				7,6,5,
				4,0,1,
				4,5,1,
				0,4,7,
				0,7,3,
				1,2,6,
				1,6,5,
		};
		
		this.boxModel = loader.loadToVAO(positions, indices);
	}
	
	public void renderAABBs(List<CollisionManager.CollisionEntry> collisionObjects){
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GL11.glDisable(GL11.GL_CULL_FACE);
		//GL11.glDisable(GL11.GL_DEPTH_TEST);
		for(CollisionManager.CollisionEntry entry:collisionObjects){
			renderAABB(entry.entity, entry.aabb);
		}
		GL11.glEnable(GL11.GL_CULL_FACE);
		//GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}
	
	
	private void renderAABB(Entity entity, AABB aabb){
		prepareInstance(entity, aabb);
		
		GL30.glBindVertexArray(boxModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		
		GL11.glDrawElements(GL11.GL_TRIANGLES, boxModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
		
		MasterRenderer.enableCulling();
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
	
	private void prepareInstance(Entity entity, AABB aabb){	
		float scaleX = (aabb.x2 - aabb.x1);
		float scaleY = (aabb.y2 - aabb.y1);
		float scaleZ = (aabb.z2 - aabb.z1);
		float x = (aabb.x1 + aabb.x2) / 2.0f;
		float y = (aabb.y1 + aabb.y2) / 2.0f;
		float z = (aabb.z1 + aabb.z2) / 2.0f;
		
		Vector3f pos = new Vector3f(x,y,z);
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(
				pos, 0,0,0, scaleX, scaleY, scaleZ);	//rot = 0, since AABB is axis aligned!
		shader.loadTransformationMatrix(transformationMatrix);
	}
}
