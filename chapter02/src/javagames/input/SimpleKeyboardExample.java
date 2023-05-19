package javagames.input;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javagames.chapter02_util.SimpleKeyboardInput;

public class SimpleKeyboardExample extends JFrame implements Runnable {
	private volatile boolean running;
	private Thread gameThread;
	// We will respond only when space is pressed once even if the user keep it pressed for a long time
	// For the arrow keys, however, we will continuously output their status 
	private boolean isSpacePressed;

	private SimpleKeyboardInput keys;
	
	public SimpleKeyboardExample() {
		keys = new SimpleKeyboardInput(); 
	}

	private void createAndShowGUI() {
		setTitle("Keyboard Input");
		setSize(320, 240);
		addKeyListener(keys);
		setVisible(true);
		
		gameThread = new Thread(this);
		gameThread.start();
	}
	
	@Override
	public void run() {
		running = true;
		while(running) {
			gameLoop();
		}
	}
	
	private void gameLoop() {
		if(keys.keyDown(KeyEvent.VK_SPACE)) {
			// If space just got pressed from a non-pressed state, then only output
			if(!isSpacePressed) {
				System.out.println("VK_SPACE");
			}
			isSpacePressed = true;
		} else {
			isSpacePressed = false;
		}
		
		if(keys.keyDown(KeyEvent.VK_UP)) {
			System.out.println("VK_UP");
		}
		
		if(keys.keyDown(KeyEvent.VK_DOWN)) {
			System.out.println("VK_DOWN");
		}
		
		if(keys.keyDown(KeyEvent.VK_LEFT)) {
			System.out.println("VK_LEFT");
		}
		
		if(keys.keyDown(KeyEvent.VK_RIGHT)) {
			System.out.println("VK_RIGHT");
		}
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void onWindowClosing() {
		try {
			running = false;
			gameThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static void main(String[] args) {
		SimpleKeyboardExample app = new SimpleKeyboardExample();
		app.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				app.onWindowClosing();
			}
		});
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				app.createAndShowGUI();
			}
		});
	}
}
