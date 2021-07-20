package javagames.input;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javagames.util.KeyboardInput;

public class KeyboardExample extends JFrame implements Runnable {
	private volatile boolean isRunning;
	private Thread gameThread;

	private KeyboardInput keyboardInput;

	public KeyboardExample() {
		keyboardInput = new KeyboardInput();
	}

	private void createAndShowGUI() {
		setSize(320, 240);
		setTitle("Keyboard Example");
		addKeyListener(keyboardInput);
		setVisible(true);

		gameThread = new Thread(this);
		gameThread.start();
	}

	@Override
	public void run() {
		isRunning = true;
		while (isRunning) {
			gameLoop();
		}
	}

	private void gameLoop() {
		// At the very beginning of each pulse of the game loop, we'll start polling key
		// inputs
		keyboardInput.poll();

		// Only one press for space bar
		if (keyboardInput.keyDownOnce(KeyEvent.VK_SPACE))
			System.out.println("VK_SPACE");

		// Continuous pressing for arrow keys
		if (keyboardInput.keyDown(KeyEvent.VK_UP))
			System.out.println("VK_UP");
		if (keyboardInput.keyDown(KeyEvent.VK_DOWN))
			System.out.println("VK_DOWN");
		if (keyboardInput.keyDown(KeyEvent.VK_LEFT))
			System.out.println("VK_LEFT");
		if (keyboardInput.keyDown(KeyEvent.VK_RIGHT))
			System.out.println("VK_RIGHT");

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void onWindowClosing() {
		isRunning = false;
		try {
			gameThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static void main(String[] args) {
		KeyboardExample app = new KeyboardExample();
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
