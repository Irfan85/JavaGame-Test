package javagames.transform;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javagames.util.FrameRate;
import javagames.util.KeyboardInput;
import javagames.util.Matrix3x3f;
import javagames.util.Vector2f;

public class MatrixMultiplyExample extends JFrame implements Runnable {
	private static final int SCREEN_W = 640;
	private static final int SCREEN_H = 480;
	
	private FrameRate frameRate;
	private BufferStrategy bs;
	private volatile boolean running;
	private Thread gameThread;
	private KeyboardInput keyboardInput;
	
	private float earthRot, earthRotDelta;
	private float moonRot, moonRotDelta;
	
	private boolean showStars;
	private int[] starCoords;
	private Random rand;
	
	public MatrixMultiplyExample() {
		rand = new Random();
	}
	
	private void createAndShowGUI() {
		Canvas canvas = new Canvas();
		canvas.setSize(SCREEN_W, SCREEN_H);
		canvas.setBackground(Color.BLACK);
		canvas.setIgnoreRepaint(true);
		
		getContentPane().add(canvas);
		setTitle("Matrix Multiplication Example");
		pack();
		
		keyboardInput = new KeyboardInput();
		canvas.addKeyListener(keyboardInput);
		
		setVisible(true);
		canvas.createBufferStrategy(2);
		bs = canvas.getBufferStrategy();
		canvas.requestFocus();
		
		gameThread = new Thread(this);
		gameThread.start();
	}
	
	@Override
	public void run() {
		running = true;
		initialize();
		
		while(running) {
			gameLoop();
		}
	}
	
	private void initialize() {
		frameRate = new FrameRate();
		frameRate.initialize();
		
		earthRotDelta = (float) Math.toRadians(0.5);
		moonRotDelta = (float) Math.toRadians(2.5);
		
		showStars = true;
		starCoords = new int[1000];
		
		for(int i = 0; i < starCoords.length - 1; i+=2) {
			starCoords[i] = rand.nextInt(SCREEN_W);
			starCoords[i + 1] = rand.nextInt(SCREEN_H);
		}
	}
	
	private void gameLoop() {
		processInput();
		renderFrame();
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void processInput() {
		keyboardInput.poll();
		
		if(keyboardInput.keyDownOnce(KeyEvent.VK_SPACE)) {
			showStars = !showStars;
		}
	}
	
	private void renderFrame() {
		do {
			do {
				Graphics g = null;
				try {
					g = bs.getDrawGraphics();
					g.clearRect(0, 0, getWidth(), getHeight());
					render(g);
				} finally {
					if(g != null) {
						g.dispose();
					}
				}
			} while(bs.contentsRestored());
			bs.show();
		} while(bs.contentsLost());
	}
	
	private void render(Graphics g) {
		g.setColor(Color.GREEN);
		frameRate.calculate();
		g.drawString(frameRate.getFrameRate(), 20, 20);
		g.drawString("Press [Space] to toggle stars", 20, 35);
		
		if(showStars) {
			g.setColor(Color.WHITE);
			for(int i = 0; i < starCoords.length - 1; i += 2) {
				g.fillRect(starCoords[i], starCoords[i+1], 1, 1);
			}
		}
		
		// Drawing the sun
		Matrix3x3f sunMat = Matrix3x3f.translate(SCREEN_W/2, SCREEN_H/2);
		Vector2f sun = sunMat.mul(new Vector2f());
		
		g.setColor(Color.YELLOW);
		g.fillOval((int) sun.x - 50, (int) sun.y - 50, 100, 100);
		
		// Drawing the Earth's orbit
		g.setColor(Color.WHITE);
		g.drawOval((int) sun.x - SCREEN_W/4, (int) sun.y - SCREEN_W/4, SCREEN_W/2, SCREEN_W/2);
		
		// Drawing the Earth
		Matrix3x3f earthMat = Matrix3x3f.translate(SCREEN_W/4, 0);
		earthMat = earthMat.mul(Matrix3x3f.rotate(earthRot));
		earthMat = earthMat.mul(sunMat); // Makes sun the origin
		earthRot += earthRotDelta;
		
		Vector2f earth = earthMat.mul(new Vector2f());
		g.setColor(Color.BLUE);
		g.fillOval((int) earth.x - 10, (int) earth.y - 10, 20, 20);
		
		// Drawing the Moon
		Matrix3x3f moonMat = Matrix3x3f.translate(30, 0);
		moonMat = moonMat.mul(Matrix3x3f.rotate(moonRot));
		moonMat = moonMat.mul(earthMat); // Makes earth the origin
		moonRot += moonRotDelta;
		
		Vector2f moon = moonMat.mul(new Vector2f());
		g.setColor(Color.LIGHT_GRAY);
		g.fillOval((int) moon.x - 5, (int) moon.y - 5, 10, 10);
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
		MatrixMultiplyExample app = new MatrixMultiplyExample();
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
