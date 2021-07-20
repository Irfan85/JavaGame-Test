package javagames.util;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SimpleKeyboardInput implements KeyListener {

	// An array to hold the states of all keys
	boolean[] keyStates;
	
	public SimpleKeyboardInput() {
		keyStates = new boolean[256];
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// We don't need this for the game
	}

	// We are making this method synchronized to make it thread safe
	@Override
	public synchronized void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode >= 0 && keyCode < keyStates.length) {
			keyStates[keyCode] = true;
		}
	}

	@Override
	public synchronized void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode >= 0 && keyCode < keyStates.length) {
			keyStates[keyCode] = false;
		}
	}
	
	// Returns true if a key is currently in pressed down state
	public boolean keyDown(int keyCode) {
		return keyStates[keyCode];
	}

}
