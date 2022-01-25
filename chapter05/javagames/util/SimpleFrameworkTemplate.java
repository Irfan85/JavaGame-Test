package javagames.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/*
 * A template for the Simple framework for testing purpose
 * */
public class SimpleFrameworkTemplate extends SimpleFramework {

	public SimpleFrameworkTemplate() {
		appBackgroundColor = Color.WHITE;
		appBorderColor = Color.LIGHT_GRAY;
		appFont = new Font("Courier New", Font.PLAIN, 14);
		appBorderScale = 0.9f;
		appFpsTextColor = Color.BLACK;
		appWidth = 640;
		appHeight = 480;
		appMaintainAspectRatio = true;
		appSleepTime = 10;
		appTitle = "Framework Template";
		appWorldWidth = 2.0f;
		appWorldHeight = 2.0f;
	}
	
	@Override
	protected void initialize() {
		super.initialize();
	}
	
	@Override
	protected void processInput(double delta) {
		super.processInput(delta);
	}
	
	@Override
	protected void updateObjects(double delta) {
		super.updateObjects(delta);
	}
	
	@Override
	protected void render(Graphics g) {
		super.render(g);
		
		Vector2f circlePosition = getViewPortTransformMatrix().mul(new Vector2f(0, 0));
		g.drawOval((int) circlePosition.x, (int) circlePosition.y, 20, 20);
	}
	
	@Override
	protected void terminate() {
		super.terminate();
	}
	
	public static void main(String[] args) {
		launchApp(new SimpleFrameworkTemplate());
	}

}
