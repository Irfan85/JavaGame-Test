package javagames.chapter01_util;

public class FrameRate {
	private String frameRateString;
	private long lastFrameTime, delta;
	private int frameCount;
	
	public void initialize() {
		lastFrameTime = System.currentTimeMillis();
		frameRateString = "FPS: 0";
	}
	
	public void calculate() {
		long current = System.currentTimeMillis();
		delta += (current - lastFrameTime);
		lastFrameTime = current;
		frameCount++;
		
		if(delta > 1000) {
			delta -= 1000;
			frameRateString = String.format("FPS: %d", frameCount);
			frameCount = 0;
		}
	}
	
	public String getFrameRate() {
		return frameRateString;
	}
}
