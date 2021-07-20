package javagames.util;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardInput implements KeyListener {
	private boolean[] keyStates;
	private int[] polled;
	
	public KeyboardInput() {
		keyStates = new boolean[256];
		polled = new int[256];
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Don't need this in our game
	}

	@Override
	public synchronized void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode >= 0 && keyCode < keyStates.length)
			keyStates[keyCode] = true;
	}

	@Override
	public synchronized void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode >= 0 && keyCode < keyStates.length)
			keyStates[keyCode] = false;
	}
	
	// This method will keep count of frames when a key remain pressed. Safe way to detect single or continuous pressing of keys
	public synchronized void poll() {
		for(int i = 0; i < keyStates.length; i++) {
			if(keyStates[i])
				polled[i]++;
			else {
				polled[i] = 0;
			}
		}
	}
	
	public boolean keyDown(int keyCode) {
		return polled[keyCode] > 0;
	}
	
	public boolean keyDownOnce(int keyCode) {
		return polled[keyCode] == 1;
	}
}
