package timeandspace;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javagames.chapter01_util.FrameRate;
import javagames.chapter02_util.KeyboardInput;
import javagames.chapter02_util.RelativeMouseInput;
import javagames.chapter03_util.Matrix3x3f;
import javagames.chapter03_util.Vector2f;

public class ScreenMappingExample extends JFrame implements Runnable {
	private Canvas canvas;
	private FrameRate frameRate;
	private BufferStrategy bs;
	private volatile boolean running;
	private Thread gameThread;
	private KeyboardInput keyboardInput;
	private RelativeMouseInput mouseInput;
	
	private Vector2f[] triangle;
	private Vector2f[] worldTriangle; // We will modify this one
	
	private Vector2f[] rectangle;
	private Vector2f[] worldRectangle;	
	
	private void createAndShowGUI() {
		canvas = new Canvas();
		canvas.setSize(640, 480);
		canvas.setBackground(Color.WHITE);
		canvas.setIgnoreRepaint(true);
		
		getContentPane().add(canvas);
		setTitle("Screen Mapping Example");
		setIgnoreRepaint(true);
		pack();
		
		keyboardInput = new KeyboardInput();
		canvas.addKeyListener(keyboardInput);
		
		mouseInput = new RelativeMouseInput(canvas);
//		canvas.addMouseListener(mouseInput);
//		canvas.addMouseMotionListener(mouseInput);
//		canvas.addMouseWheelListener(mouseInput);
		
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
		
		long currentTimeNS = System.nanoTime();
		long lastTImeNS = currentTimeNS;
		double nsPerFrame;
		
		while(running) {
			currentTimeNS = System.nanoTime();
			nsPerFrame = currentTimeNS - lastTImeNS;
			gameLoop(nsPerFrame/1.0E9);
			lastTImeNS = currentTimeNS;
		}
	}
	
	private void initialize() {
		frameRate = new FrameRate();
		frameRate.initialize();
		
		triangle = new Vector2f[] {
				new Vector2f(0.0f, 0.5f),
				new Vector2f(-0.5f, -0.5f),
				new Vector2f(0.5f, -0.5f)
		};
		
		worldTriangle = new Vector2f[triangle.length];
		
		rectangle = new Vector2f[] {
				new Vector2f(-1.0f, 1.0f),
				new Vector2f(1.0f, 1.0f),
				new Vector2f(1.0f, -1.0f),
				new Vector2f(-1.0f, -1.0f)
		};
		
		worldRectangle = new Vector2f[rectangle.length];
	}
	
	private void gameLoop(double timeDelta) {
		processInput(timeDelta);
		updateObjects(timeDelta);
		renderFrame();
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// This function does nothing in this example since we are not using any input
	private void processInput(double timeDelta) {
		keyboardInput.poll();
		mouseInput.poll();
	}
	
	// This function also does nothing in this example since we are not moving or changing any object
	private void updateObjects(double timeDelta) {

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
					if (g != null) {
						g.dispose();
					}
				}
			} while (bs.contentsRestored());
			
			bs.show();
		} while (bs.contentsLost());
	}
	
	private void render(Graphics g) {
		frameRate.calculate();
		g.setColor(Color.BLACK);
		g.drawString(frameRate.getFrameRate(), 20, 20);
		
		// The dimension of our world in our own co-ordinate system
		float worldWidth = 2.0f;
		float worldHeight = 2.0f;
		// Just like arrays, pixels are counted from 0 but the getWidth() or getHeight() methods
		// return the actual number of pixels. So we have to subtract them by 1
		float screenWidth = canvas.getWidth() - 1;
		float screenHeight = canvas.getHeight() - 1;
		
		// Scaling variables
		float sx = screenWidth / worldWidth;
		float sy = screenHeight / worldHeight;
		
		// Transformation variables
		float tx = screenWidth / 2.0f;
		float ty = screenHeight / 2.0f;
		
		// Creating the viewport matrix. Scaling by a negative number automatically flips the object
		Matrix3x3f viewPort = Matrix3x3f.scale(sx, -sy);
		viewPort = viewPort.mul(Matrix3x3f.translate(tx, ty));
		
		// Drawing the triangle
		for(int i = 0; i < triangle.length; i++) {
			worldTriangle[i] = viewPort.mul(triangle[i]);
		}
		
		drawPolygon(g, worldTriangle);
		
		// Drawing the border rectangle
		for(int i = 0; i < rectangle.length; i++) {
			worldRectangle[i] = viewPort.mul(rectangle[i]);
		}
		
		drawPolygon(g, worldRectangle);
	}
	
	private void drawPolygon(Graphics g, Vector2f[] polygon) {
		// We will keep drawing lines from point 's' to point 'p'
		Vector2f p;
		Vector2f s = polygon[polygon.length - 1];
		
		for(int i = 0; i < polygon.length; i++) {
			p = polygon[i];
			g.drawLine((int) s.x, (int) s.y, (int) p.x, (int) p.y);
			s = p;
		}
	}
	
	private void onWindowClosing() {
		try {
			running = false;
			gameThread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		System.exit(0);
	}

	public static void main(String[] args) {
		final ScreenMappingExample app = new ScreenMappingExample();
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
