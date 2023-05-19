package javagames.chapter02_util;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.channels.NonWritableChannelException;

public class SimpleMouseInput implements MouseListener, MouseMotionListener, MouseWheelListener {
	
	// In java, idle mouse is considered to be button 0. So including left, right and middle buttons there are total four.
	// So we'll not count idle mouse and consider three mouse buttons and subtract 1 from their index 
	private static final int BUTTON_COUNT = 3;
	
	private Point polledMousePos; // This will be updated with the game loop pulse to maintain consistency
	private Point currentMousePos; // This is the variable where immediate mouse position change will be recorded
	private boolean[] mouseButtonStates;
	private int[] polled;
	private int notches; // This indicates mouse wheel position. 
	private int polledNotches;
	
	public SimpleMouseInput() {
		polledMousePos = new Point(0, 0);
		currentMousePos = new Point(0, 0);
		mouseButtonStates = new boolean[BUTTON_COUNT];
		polled = new int[BUTTON_COUNT];
	}
	
	public synchronized void poll() {
		polledMousePos = new Point(currentMousePos);
		polledNotches = notches;
		notches = 0; // resetting mouse wheel position after we acknowledge the change
		
		for(int i = 0; i < mouseButtonStates.length; i++) {
			if (mouseButtonStates[i]) 
				polled[i]++;
			else
				polled[i] = 0;
		}
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
		currentMousePos = e.getPoint();
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
}
