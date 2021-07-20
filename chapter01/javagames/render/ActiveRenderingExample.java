package javagames.render;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javagames.util.FrameRate;

public class ActiveRenderingExample extends JFrame implements Runnable {
	private BufferStrategy bufferStrategy;
	private volatile boolean isRunning;
	private Thread gameThread;

	private FrameRate frameRate;

	public ActiveRenderingExample() {
		frameRate = new FrameRate();
	}

	private void createAndShowGUI() {
		// Setting up the canvas where we will draw
		Canvas canvas = new Canvas();
		canvas.setSize(320, 240);
		canvas.setBackground(Color.BLACK);
		// Since we're handling the rendering ourselves, we want canvas to ignore paint
		// messages from the operating system
		canvas.setIgnoreRepaint(true);

		// Setting up the JFrame and attach the canvas to it
		getContentPane().add(canvas);
		setTitle("Active Rendering");
		// JFrame should also ignore OS paint calls
		setIgnoreRepaint(true);
		// This method makes sure JFrame automatically adjusts its size based on the components
		// that it contains. This way we don't have to set the size explicitly
		pack();
		setVisible(true);

		// We will work with double buffer
		canvas.createBufferStrategy(2);
		bufferStrategy = canvas.getBufferStrategy();

		gameThread = new Thread(this);
		gameThread.start();
	}

	@Override
	public void run() {
		isRunning = true;
		frameRate.initialize();

		while (isRunning) {
			gameLoop();
		}
	}

	private void gameLoop() {
		// Reference: https://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferStrategy.html
		// Repeat rendering if graphics content just becomes unavailable
		do {
			// If graphics content just got restored, redraw it to maintain consistency
			do {
				// In case we don't have the graphics content, we may get an exception here
				Graphics g = null;
				try {
					g = bufferStrategy.getDrawGraphics();
					// Clear the entire window with a black rectangle
					g.clearRect(0, 0, getWidth(), getHeight());
					// Then begin rendering
					render(g);
				} finally {
					if (g != null) {
						g.dispose();
					}
				}
			} while (bufferStrategy.contentsRestored());

			// Display the buffer that just got rendered
			bufferStrategy.show();

		} while (bufferStrategy.contentsLost());
	}

	private void render(Graphics g) {
		frameRate.calculate();
		g.setColor(Color.GREEN);
		g.drawString(frameRate.getFrameRate(), 30, 30);
	}
	
	private void onWindowClosing() {
		try {
			isRunning = false;
			gameThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static void main(String[] args) {
		ActiveRenderingExample app = new ActiveRenderingExample();
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
