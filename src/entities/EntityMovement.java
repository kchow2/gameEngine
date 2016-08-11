package entities;

public class EntityMovement {
	public int forward = 0;
	public int strafe = 0;
	public float turn = 0.0f;
	public float pitchUp = 0.0f;
	public float jump = 0.0f;
	public boolean fire = false;
	
	public void clearInputs(){
		forward = 0;
		strafe = 0;
		turn = 0;
		pitchUp = 0;
		jump = 0;
		fire = false;
	}
}
