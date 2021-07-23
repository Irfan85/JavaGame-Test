package javagames.util;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.channels.NonWritableChannelException;

import javax.swing.SwingUtilities;

public class RelativeMouseInput implements MouseListener, MouseMotionListener, MouseWheelListener {
	
	// In java, idle mouse is considered to be button 0. So including left, right and middle buttons there are total four.
	// So we'll not count idle mouse and consider three mouse buttons and subtract 1 from their index 
	private static final int BUTTON_COUNT = 3;
	
	private Point polledMousePos; // This will be updated with the game loop pulse to maintain consistency
	private Point currentMousePos; // This is the variable where immediate mouse position change will be recorded
	private boolean[] mouseButtonStates;
	private int[] polled;
	private int notches; // This indicates mouse wheel position. 
	private int polledNotches;

	private boolean relative;
	private int dx, dy;
	private Robot robot; // This robot will allow us to automatically put the cursor at the center of the window
	private Component component;
	
	public RelativeMouseInput(Component component) {
		polledMousePos = new Point(0, 0);
		currentMousePos = new Point(0, 0);
		mouseButtonStates = new boolean[BUTTON_COUNT];
		polled = new int[BUTTON_COUNT];
		
		this.component = component;

		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void poll() {
		if(isRelative())
			polledMousePos = new Point(dx, dy);
		else
			polledMousePos = new Point(currentMousePos);
		dx = dy = 0; // Resetting after polling is complete

		polledNotches = notches;
		notches = 0; // Resetting mouse wheel position after we acknowledge the change
		
		for(int i = 0; i < mouseButtonStates.length; i++) {
			if (mouseButtonStates[i]) 
				polled[i]++;
			else
				polled[i] = 0;
		}
	}
	
	public boolean isRelative() {
		return relative;
	}
	
	public void setRelative(boolean relative) {
		this.relative = relative;
		
		// If the user wants to enable relative mode, bring the cursor to window center
		if(isRelative())
			centerMouse();
	}
	
	public Point getPosition() {
		return polledMousePos;
	}
	
	public int getNotches() {
		return polledNotches;
	}
	
	public boolean buttonDown(int button) {
		return polled[button - 1] > 0;
	}
	
	public boolean buttonDownOnce(int button) {
		return polled[button - 1] == 1;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// We don't need this
	}

	@Override
	public synchronized void mousePressed(MouseEvent e) {
		int button = e.getButton() - 1;
		if(button >= 0 && button < mouseButtonStates.length)
			mouseButtonStates[button] = true;
	}

	@Override
	public synchronized void mouseReleased(MouseEvent e) {
		int button = e.getButton() - 1;
		if(button >= 0 && button < mouseButtonStates.length)
			mouseButtonStates[button] = false;
	}
	
	@Override
	public synchronized void mouseWheelMoved(MouseWheelEvent e) {
		notches += e.getWheelRotation();
	}

	@Override
	public synchronized void mouseMoved(MouseEvent e) {
		if(isRelative()) {
			Point p = e.getPoint();
			Point componentCenter = getComponentCenter();
			
			dx += (p.x - componentCenter.x);
			dy += (p.y - componentCenter.y);
			// After we're done calculating the relative distance, reset cursor to center
			centerMouse();
		} else {
			currentMousePos = e.getPoint();
		}
	}

	@Override
	public synchronized void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public synchronized void mouseEntered(MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public synchronized void mouseExited(MouseEvent e) {
		mouseMoved(e);
	}
	
	public Point getComponentCenter() {
		int w = component.getWidth() / 2;
		int h = component.getHeight() / 2;
		return new Point(w, h);
	}
	
	private void centerMouse() {
		if(robot != null && component.isShowing()) {
			Point componentCenter = getComponentCenter();
			// This method will convert the coordinate of the center of the window to the actual position of that particular point in the screen. (Absolute position)
			SwingUtilities.convertPointToScreen(componentCenter, component);
			// Robot will move the cursor to that point automatically
			robot.mouseMove(componentCenter.x, componentCenter.y);
		}
	}
}
