package javagames.transform;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javagames.chapter01_util.FrameRate;
import javagames.chapter02_util.KeyboardInput;
import javagames.chapter02_util.RelativeMouseInput;

public class PolarCoordinateExample extends JFrame implements Runnable {
	
	private static final int SCREEN_W = 640;
	private static final int SCREEN_H = 480;
	
	private FrameRate frameRate;
	private BufferStrategy bs;
	private volatile boolean running;
	private Thread gameThread;
	private RelativeMouseInput mouseInput;
	private KeyboardInput keyboardInput;
	
	private Point coOrd;
	
	public PolarCoordinateExample() {
		
	}
	
	private void createAndShowGUI() {
		Canvas canvas = new Canvas();
		canvas.setSize(SCREEN_W, SCREEN_H);
		canvas.setBackground(Color.BLACK);
		canvas.setIgnoreRepaint(true);
		
		getContentPane().add(canvas);
		setTitle("Polar Coordinate Example");
		setIgnoreRepaint(true);
		pack();
		
		keyboardInput = new KeyboardInput();
		canvas.addKeyListener(keyboardInput);
		
		mouseInput = new RelativeMouseInput(canvas);
		canvas.addMouseListener(mouseInput);
		canvas.addMouseMotionListener(mouseInput);
		canvas.addMouseWheelListener(mouseInput);
		
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
		
		coOrd = new Point();
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
		mouseInput.poll();
		
		coOrd = mouseInput.getPosition();
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
			} while (bs.contentsRestored());

			bs.show();

		} while (bs.contentsLost());
	}
	
	private void render(Graphics g) {
		g.setFont(new Font("Courier New", Font.BOLD, 24));
		g.setColor(Color.GREEN);
		frameRate.calculate();
		g.drawString(frameRate.getFrameRate(), 20, 40);
		
		int cx = SCREEN_W / 2; // Center x
		int cy = SCREEN_H / 2; // Center Y
		g.setColor(Color.GRAY);
		g.drawLine(0, cy, SCREEN_W, cy); // Draw x-axis
		g.drawLine(cx, 0, cx, SCREEN_H); // Draw y-axis
		
		g.setColor(Color.GREEN);
		g.drawLine(cx, cy, coOrd.x, coOrd.y); // Line drawn from center to the point
		
		int px = coOrd.x - cx; // x distance from point to center
		// We had to do the 'py' in reverse since y start from the top of the screen
		int py = cy - coOrd.y; // y distance from point to center
		double r = Math.sqrt(Math.pow(px, 2) + Math.pow(py, 2));
		// 'atan2' function is explicitly used to calculate polar co-ordinate angle in radians
		double rad = Math.atan2(py, px);
		double degrees = Math.toDegrees(rad);
		// Adjust negative angles to put them in 0 to 360 range
		if(degrees < 0) {
			degrees = 360 + degrees;
		}
		
		double sx = r * Math.cos(rad); // Converted cartesian x
		double sy = r * Math.sin(rad); // Converted cartesian y
		// \u00b0 is the degree sign in unicode
		String polar = String.format("(%.0f, %.0f\u00b0)", r, degrees);
		g.drawString(polar, 20, 60);
		String cartesian = String.format("(%.0f, %.0f)", sx, sy);
		g.drawString(cartesian, 20, 80);
		
		g.setColor(Color.WHITE);
		// Using original co-ordinates px, py here to prove that the conversion is correct
		g.drawString(String.format("(%s, %s)", px, py), coOrd.x, coOrd.y);
		
		g.setColor(Color.BLUE);
		// The arc is imagined to be bounded by a rectangle whose upper left point is at (x, y) and has a dimension of width X height
		// Putting the diameter of a sphere '2*r' will make sure the arc is drawn as a part of the sphere 
		// Reference: https://docs.oracle.com/en/java/javase/11/docs/api/java.desktop/java/awt/Graphics.html#drawArc(int,int,int,int,int,int)
		g.drawArc((int) (cx - r), (int) (cy - r), (int) (2*r), (int) (2*r), 0, (int) degrees);
		
		// This rectangle is drawn to demonstrate how the drawArc() method actually works
		g.setColor(Color.RED);
		g.drawRect((int) (cx - r), (int) (cy - r), (int) (2*r), (int) (2*r));
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
		PolarCoordinateExample app = new PolarCoordinateExample();
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
