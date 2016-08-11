package renderEngine;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

public class DisplayManager {
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;
	private static final int FPS_CAP = 120;
	
	private static long lastFrameTime;
	private static float delta;
	private static long lastFrameCountTime;
	private static int frameCount;
	private static int lastFps;
	
	public static void createDisplay(){
		ContextAttribs attribs = new ContextAttribs(3,3)
		.withForwardCompatible(true)
		.withProfileCore(true);
		
		try {
			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
			Display.create(new PixelFormat(), attribs);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
		GL11.glViewport(0, 0, WIDTH, HEIGHT);
		lastFrameTime = getCurrentTime();
		lastFrameCountTime = lastFrameTime;
		frameCount = 0;
		lastFps = 0;
	}
	public static void updateDisplay(){
		Display.sync(FPS_CAP);
		Display.update();
		
		long currentFrameTime = getCurrentTime();
		delta = (currentFrameTime - lastFrameTime) / 1000.0f;
		lastFrameTime = currentFrameTime;
		
		//fps counter that updates every 1/2 second
		frameCount++;
		if(currentFrameTime - lastFrameCountTime > 500){
			lastFrameCountTime = currentFrameTime;
			lastFps = frameCount * 2;
			frameCount = 0;
			//System.out.println("here");
		}
	}
	
	public static int getScreenWidth(){
		return WIDTH;
	}
	
	public static int getScreenHeight(){
		return HEIGHT;
	}
	
	public static float getFrameTimeSeconds(){
		return delta;
	}
	
	public static int getFps(){
		return lastFps;
	}
	
	public static void closeDisplay(){
		Display.destroy();
	}
	
	private static long getCurrentTime(){
		return Sys.getTime()*1000 / Sys.getTimerResolution();
	}

}
