package timeandspace;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

public class ViewportRatio extends JFrame implements Runnable {
	private Canvas canvas;
	private FrameRate frameRate;
	private BufferStrategy bs;
	private volatile boolean running;
	private Thread gameThread;
	private KeyboardInput keyboardInput;
	private RelativeMouseInput mouseInput;
	
	private Vector2f[] triangle;
	private Vector2f[] worldTriangle; // We will modify this one
	
	// The dimension of our world in our own co-ordinate system. We are using 16:9 aspect ratio
	private float worldWidth = 16.0f;
	private float worldHeight = 9.0f ;
	
	private void createAndShowGUI() {
		canvas = new Canvas();
		canvas.setBackground(Color.WHITE);
		canvas.setIgnoreRepaint(true);
		
		setLayout(null);
		setSize(640, 640);
		setTitle("Screen Mapping Example");
		// NOTE: In this example, it has been observed that setting setIgnoreRepaint() to false for the JFrame causes wired issues and 
		// artifacts while resizing windows. Perhaps the window messages from the operating system are essential for the JFrame. However, we are ignoring the repaint
		// calls for the canvas just like before.
		
		getContentPane().setBackground(Color.LIGHT_GRAY);
		getContentPane().add(canvas);
		getContentPane().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				onComponentResized(e);
			}
		});
		
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
	
	private void onComponentResized(ComponentEvent e) {
		System.out.println("onComponentResized() has been called");
		
		Dimension size = getContentPane().getSize();
		
		// We want the size of the viewport(canvas) to be 3/4th the size of the window(JFrame) so that we
		// can easily see how the viewport is maintaining its aspect ratio. It's just for demonstration purpose.
		// As stated early, we will turn our canvas into 16:9 ratio, but we are setting these for just for starting with a size.
		// Later, either the height or the width will be adjusted according to the ratio whichever if the perfect fit
		int vw = size.width * 3 / 4; // Viewport width
		int vh = size.height * 3 / 4; // Viewport height
		
		// As our viewport is smaller than the window, there will be extra gray areas. We want to put our viewport just at the center
		// So, we have to determine the difference between the width and height, take half of the difference and put the top-left corner of the
		// canvas at that particular co-ordinate as this will make both the horizontal and vertical gaps between the viewport(canvas) and window(JFrame) equal
		int vx = (size.width - vw) / 2; // Viewport x position (of top-left point)
		int vy = (size.height - vh) / 2; // Viewport y position (of top-left point)
		
		// Now, we will adjust the size of our viewport so that it respects our desired 16:9 aspect ratio. We can just fix the view port width and make the height compatible
		// with the ratio
		int newW = vw;
		int newH = (int) ((vw * worldHeight)/worldWidth);
		// However, sometimes, the calculated height might exceed our maximum specified height which is 3/4th of the window height. In that case, we will use the old value of height and make the width compatible
		// with the ratio
		if(newH > vh) {
			newH = vh;
			newW = (int) ((vh * worldWidth)/worldHeight);
		}
		
		// As we have just resized our viewport to maintain the 16:9 aspect ratio, the viewport won't be at center anymore as more or less gaps will be created in horizontal or vertical direction. We will use
		// similar procedure as before to put it back to center. We will measure the difference between old and new height and width, divide them by half and add those value to both x and y direction so that the gaps between
		// both horizontal and vertical direction becomes equal again
		vx += (vw - newW) / 2;
		vy += (vh - newH) / 2;
		
		canvas.setLocation(vx, vy);
		canvas.setSize(newW, newH);
	}
	
	@Override
	public void run() {
		System.out.println("run() has been called");
		
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
				new Vector2f(0.0f, 2.25f),
				new Vector2f(-4.0f, -2.25f),
				new Vector2f(4.0f, -2.25f)
		};
		
		worldTriangle = new Vector2f[triangle.length];
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
		Matrix3x3f viewPort = Matrix3x3f.identity();
		viewPort = viewPort.mul(Matrix3x3f.scale(sx, -sy));
		viewPort = viewPort.mul(Matrix3x3f.translate(tx, ty));
		
		// Drawing the triangle
		for(int i = 0; i < triangle.length; i++) {
			worldTriangle[i] = viewPort.mul(triangle[i]);
		}
		
		drawPolygon(g, worldTriangle);
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
		final ViewportRatio app = new ViewportRatio();
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
