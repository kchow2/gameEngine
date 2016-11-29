
package toolbox;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;

public class Maths {
	
	public static float barycentricInterpolate( Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos){
		float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
		float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
		float l3 = 1.0f - l1 - l2;
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}
	
	public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.scale(new Vector3f(scale.x, scale.y, 1f), matrix, matrix);
		return matrix;
	}

	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale){
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.rotate((float)Math.toRadians(ry), new Vector3f(0,1,0), matrix, matrix);
		Matrix4f.rotate((float)Math.toRadians(rx), new Vector3f(1,0,0), matrix, matrix);
		Matrix4f.rotate((float)Math.toRadians(rz), new Vector3f(0,0,1), matrix, matrix);
		Matrix4f.scale(new Vector3f(scale,scale,scale), matrix, matrix);
		return matrix;
	}
	
	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scaleX, float scaleY, float scaleZ){
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.rotate((float)Math.toRadians(ry), new Vector3f(0,1,0), matrix, matrix);
		Matrix4f.rotate((float)Math.toRadians(rx), new Vector3f(1,0,0), matrix, matrix);
		Matrix4f.rotate((float)Math.toRadians(rz), new Vector3f(0,0,1), matrix, matrix);
		Matrix4f.scale(new Vector3f(scaleX,scaleY,scaleZ), matrix, matrix);
		return matrix;
	}
	
	public static Matrix4f createViewMatrix(Camera camera){
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.rotate((float)Math.toRadians(camera.getPitch()), new Vector3f(1,0,0), matrix, matrix);
		Matrix4f.rotate((float)Math.toRadians(camera.getYaw()), new Vector3f(0,1,0), matrix, matrix);
		Vector3f cameraPos = camera.getPosition();
		Vector3f negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		Matrix4f.translate(negativeCameraPos, matrix, matrix);
		return matrix;
	}
	
	public static float capAngleTo180(float angle_degrees){
		float res = angle_degrees % 360.0f;
		if(res < -180.0f)
			res += 360.0f;
		else if(res > 180.0f)
			res -= 360.0f;
		return res;
	}
	
	//Gets the 3D distance between two points
	public static float getDistance(Vector3f v1, Vector3f v2){
		float x = v2.x - v1.x;
		float y = v2.y - v1.y;
		float z = v2.z - v1.z;
		return (float) Math.sqrt(x*x + y*y + z*z);
	}
	
	//Gets the 3D distance between two points
	public static float getDistanceSq(Vector3f v1, Vector3f v2){
		float x = v2.x - v1.x;
		float y = v2.y - v1.y;
		float z = v2.z - v1.z;
		return x*x + y*y + z*z;
	}
	
	//Linear interpolation between 2 values, with blend factor of alpha
	public static float lerp(float f1, float f2, float alpha){
		return f1*alpha + (1.0f-alpha)*f2;
	}
}
