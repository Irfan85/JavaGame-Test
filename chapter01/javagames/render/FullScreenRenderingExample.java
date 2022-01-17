package javagames.render;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javagames.util.FrameRate;

public class FullScreenRenderingExample extends JFrame implements Runnable {
	private BufferStrategy bufferStrategy;
	private volatile boolean running;
	private Thread gameThread;
	private GraphicsDevice graphicsDevice;
	private DisplayMode currentDisplayMode;

	private FrameRate frameRate;

	public FullScreenRenderingExample() {
		frameRate = new FrameRate();
	}

	private void createAndShowGUI() {
		setIgnoreRepaint(true);
		// Since our application will run full screen, we don't need decorations like
		// close button and stuff
		setUndecorated(true);
		setBackground(Color.BLACK);
		
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
		currentDisplayMode = graphicsDevice.getDisplayMode();

		if (!graphicsDevice.isFullScreenSupported()) {
			System.err.println("Your device doesn't support full screen");
			System.exit(0);
		}
		graphicsDevice.setFullScreenWindow(this);
		graphicsDevice.setDisplayMode(getFullScreenDisplayMode());

		// In full screen mode, we'll get the buffer strategy directly from the JFrame
		// instead
		// of a canvas
		createBufferStrategy(2);
		bufferStrategy = getBufferStrategy();

		// Press 'ESC' to exit. Since we don't have window decoration, we have to
		// implement it ourselves
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					shutDownApp();
			}
		});
		
		// Start the gameThread
		gameThread = new Thread(this);
		gameThread.start();
	}

	private DisplayMode getFullScreenDisplayMode() {
		// We will use a fixed display mode for simplicity and not fetching it
		// from graphicsDevice. But that would be the recommended approach. Were just
		// assuming this display mode is supported by the device
		return new DisplayMode(1920, 1080, 32, DisplayMode.REFRESH_RATE_UNKNOWN);
	}

	private void shutDownApp() {
		try {
			running = false;
			gameThread.join();
			System.out.println("Game loop stopped!");

			// Exit full screen
			graphicsDevice.setDisplayMode(currentDisplayMode);
			graphicsDevice.setFullScreenWindow(null);
			System.out.println("Display Restored.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	@Override
	public void run() {
		running = true;
		frameRate.initialize();
		while (running) {
			gameLoop();
		}
	}

	private void gameLoop() {
		do {
			do {
				Graphics g = null;
				try {
					g = bufferStrategy.getDrawGraphics();
					// Clear Screen
					g.clearRect(0, 0, getWidth(), getHeight());
					// Draw
					render(g);
				} finally {
					if (g != null)
						g.dispose();
				}
			} while (bufferStrategy.contentsRestored());

			// Display the buffer
			bufferStrategy.show();
		} while (bufferStrategy.contentsLost());
	}

	private void render(Graphics g) {
		frameRate.calculate();
		g.setColor(Color.GREEN);
		g.drawString(frameRate.getFrameRate(), 30, 30);
		g.drawString("Press ESC to exit.", 30, 60);
	}

	public static void main(String[] args) {
		FullScreenRenderingExample app = new FullScreenRenderingExample();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				app.createAndShowGUI();
			}
		});
	}

}
