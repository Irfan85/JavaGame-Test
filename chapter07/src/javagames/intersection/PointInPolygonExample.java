package javagames.intersection;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javagames.chapter03_util.Matrix3x3f;
import javagames.chapter03_util.Vector2f;
import javagames.chapter05_util.SimpleFramework;
import javagames.chapter05_util.Utility;

public class PointInPolygonExample extends SimpleFramework {
	
	private static final int MAX_POINTS = 10000;
	
	private ArrayList<Vector2f> polygon;
	private ArrayList<Vector2f> worldPolygon;
	
	private ArrayList<Vector2f> insidePoints;
	private ArrayList<Vector2f> outsidePoints;
	
	private Vector2f mousePos;
	private boolean mouseIsHovering;
	private boolean winding;
	
	public PointInPolygonExample() {
		appWidth =  640;
		appHeight = 480;
		appTitle = "Point In Polygon Example";
		appBackgroundColor = Color.WHITE;
		appFpsTextColor = Color.BLACK;
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		
		polygon = new ArrayList<>();
		worldPolygon = new ArrayList<>();
		
		insidePoints = new ArrayList<>();
		outsidePoints = new ArrayList<>();
		
		mousePos = new Vector2f();
	}
	
	@Override
	protected void processInput(double delta) {
		super.processInput(delta);
		
		mousePos = getWorldMousePosition();
		
		// Press space to toggle winding
		if(keyboardInput.keyDownOnce(KeyEvent.VK_SPACE)) {
			winding = !winding;
		}
		
		// Draw polygon as the user clicks left mouse button
		if(mouseInput.buttonDownOnce(MouseEvent.BUTTON1)) {
			polygon.add(mousePos);
		}
		
		// Clear the polygon if the user clicks the right mouse button
		if(mouseInput.buttonDownOnce(MouseEvent.BUTTON3)) {
			polygon.clear();
		}
	}
	
	private boolean pointInPolygon(Vector2f point, List<Vector2f> polygon, boolean winding) {
		// We will use it as the winding number when we will use winding.
		// Otherwise, it will be used as a boolean (0/1)
		int inside = 0;
		
		// A polygon must contain at least 3 points
		if(polygon.size() > 2) {
			Vector2f startPoint = polygon.get(polygon.size() - 1);
			boolean startPointIsAbove = startPoint.y >= point.y;
			
			for(Vector2f endPoint : polygon) {
				boolean endPointIsAbove = endPoint.y >= point.y;
				
				// The start and end point has to be in the opposite direction of the 
				// point to make intersection possible
				if(startPointIsAbove != endPointIsAbove) {
					// Applying (y-y1) = m*(x-x1) formula
					float m = (endPoint.y - startPoint.y) / (endPoint.x - startPoint.x);
					float x = startPoint.x + ((point.y - startPoint.y) / m);
					
					// Intersection will be possible only if intersection occurs ahead of the point
					if(x >= point.x) {
						if(winding) {
							// If start point is above(clockwise) add 1. Otherwise (counter-clockwise) subtract 1
							inside += startPointIsAbove ? 1 : -1;
						} else {
							// If we're not using winding, just flip the value of inside
							// so that we can implement that event-odd scenario
							inside  = inside == 1 ? 0 : 1;
						}
					}
				}
				
				// The end point of the current edge will be the start point of the next edge
				startPointIsAbove = endPointIsAbove;
				startPoint = endPoint;
			}
		}
		
		return inside != 0;
	}
	
	@Override
	protected void updateObjects(double delta) {
		super.updateObjects(delta);

		mouseIsHovering = pointInPolygon(mousePos, polygon, winding);
		
		Random rand = new Random();
		
		insidePoints.clear();
		outsidePoints.clear();
		
		for(int i = 0; i < MAX_POINTS; i++) {
			float x = (rand.nextFloat() * 2.0f) - 1.0f;
			float y = (rand.nextFloat() * 2.0f) - 1.0f;
			
			Vector2f point = new Vector2f(x, y);
			
			if(pointInPolygon(point, polygon, winding)) {
				insidePoints.add(point);
			} else {
				outsidePoints.add(point);
			}
		}
	}
	
	@Override
	protected void render(Graphics g) {
		super.render(g);
		
		g.drawString("Winding: " + (winding ? "On" : "Off"), 20, 35);
		String mousePosStr = String.format("Mouse: (%.2f, %.2f)", mousePos.x, mousePos.y);
		g.drawString(mousePosStr, 20, 50);
		g.drawString("Left-Click to add points", 20, 65);
		g.drawString("Right-Click to clear points", 20, 80);
		g.drawString("Space bar to toggle winding",	20, 95);
		
		Matrix3x3f viewPortMatrix = getViewPortTransformMatrix();
		
		// Draw the polygon
		if(polygon.size() > 1) {
			worldPolygon.clear();
			
			for(Vector2f vector : polygon) {
				worldPolygon.add(viewPortMatrix.mul(vector));
			}
			
			g.setColor(mouseIsHovering ? Color.GREEN : Color.BLUE);
			
			Utility.drawPolygon(g, worldPolygon);
		}
		
		// Draw random points (Outside points red, inside points blue)
		g.setColor(Color.BLUE);
		for(Vector2f vector: insidePoints) {
			Vector2f point = viewPortMatrix.mul(vector);
			g.fillRect((int)point.x, (int)point.y, 1, 1);
		}
		
		g.setColor(Color.RED);
		for(Vector2f vector : outsidePoints) {
			Vector2f point = viewPortMatrix.mul(vector);
			g.fillRect((int)point.x, (int)point.y, 1, 1);
		}
		
	}

	public static void main(String[] args) {
		launchApp(new PointInPolygonExample());
	}

}
