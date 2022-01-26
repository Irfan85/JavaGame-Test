package javagames.util;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/*
 * This is the game framework class that will provide the necessary 
 * boiler plate code for creating any game. It will provide utilities such as 
 * creating window, input, render etc. Games will extend this class so we have to make usable variables
 * and methods 'protected' instead of 'private' 
 * */
public class SimpleFramework extends JFrame implements Runnable {
	
	// Private fields that are only the concern of the framework
	private BufferStrategy bs;
	private volatile boolean running;
	private Thread gameThread;
	
	// These fields will be shared the the game
	protected FrameRate frameRate;
	protected Canvas canvas;
	protected KeyboardInput keyboardInput;
	protected RelativeMouseInput mouseInput;
	
	// We're using the prefix 'app' to get easy access to game properties in the autocomplete suggestion
	protected String appTitle = "Untitled Game";
	protected int appWidth = 640;
	protected int appHeight = 480;
	protected float appBorderScale = 0.8f; // The ratio of the canvas and JFrame when aspect ratio is being maintained
	protected float appWorldWidth = 2.0f; // Our world will have a dimension of 2x2 units
	protected float appWorldHeight = 2.0f;
	protected long appSleepTime = 10; // The sleep between each iteration of the game loop
	protected boolean appMaintainAspectRatio = false; // We're setting this false by default since in some circumstances, we may not need it. For example, when tha game window has fixed height and width
	
	protected Color appBackgroundColor = Color.BLACK; // Background color for the canvas;
	protected Color appBorderColor = Color.LIGHT_GRAY; // The color of the gap between the canvas and the JFrame when the aspect ratio is beging adjusted
	protected Color appFpsTextColor = Color.GREEN; // The color of the FPS text
	protected Font appFont = new Font("Courier New", Font.PLAIN, 14); // The default font that we want to use in the game. We have to apply it in the components manually however
	
	protected void createAndShowGUI() {
		canvas = new Canvas();
		canvas.setBackground(appBackgroundColor);
		canvas.setIgnoreRepaint(true);
		
		getContentPane().add(canvas);
		setLocationByPlatform(true); // Spawn the window at the default location in the screen that native windowing system provides
	
		if(appMaintainAspectRatio) {
			getContentPane().setBackground(appBorderColor);
			setSize(appWidth, appHeight);
			setLayout(null);
			getContentPane().addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					onComponentResized(e);
				}
			});
		} else {
			canvas.setSize(appWidth, appHeight);
			pack();
		}
		
		setTitle(appTitle);
		
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
	
	protected void onComponentResized(ComponentEvent e) {
		Dimension size = getContentPane().getSize();
		
		int viewPortWidth = (int) (size.width * appBorderScale);
		int viewPortHeight = (int) (size.height * appBorderScale);
		int viewPortX = (size.width - viewPortWidth) / 2;
		int viewPortY = (size.height - viewPortHeight) / 2;
		
		int newWidth = viewPortWidth;
		int newHeight = (int) (viewPortWidth * appWorldHeight / appWorldWidth);
		if(newHeight > viewPortHeight) {
			newWidth = (int) (viewPortWidth * appWorldWidth / appWorldHeight);
			newHeight = viewPortHeight;
		}
		
		viewPortX += ((viewPortWidth - newWidth) / 2);
		viewPortY += ((viewPortHeight - newHeight) / 2);
		
		canvas.setLocation(viewPortX, viewPortY);
		canvas.setSize(newWidth, newHeight);
	}
	
	protected Matrix3x3f getViewPortTransformMatrix() {
		return Utility.createViewPortMatrix(appWorldWidth, appWorldHeight, canvas.getWidth(), canvas.getHeight());
	}
	
	protected Matrix3x3f getReverseViewPortTransformMatrix() {
		return Utility.createReverseViewPortMatrix(appWorldWidth, appWorldHeight, canvas.getWidth(), canvas.getHeight());
	}
	
	// Returns mouse cursor co-ordinate in terms of the world co-ordinate system
	protected Vector2f getWorldMousePosition() {
		Matrix3x3f screenToWorldMatrix = getReverseViewPortTransformMatrix();
		Point mousePoint = mouseInput.getPosition();
		Vector2f screenMousePosition = new Vector2f(mousePoint.x, mousePoint.y);
		
		return screenToWorldMatrix.mul(screenMousePosition);
	}
	
	// Returns the relative mouse cursor co-ordinate in terms of the world co-ordinate system. Note that since
	// relative position just says how much change has been made with respect to the previous position, we don't need a translation matrix for that. Only the
	// scaling matrix will be enough to scale the co-ordinate points
	protected Vector2f getRelativeWorldMousePosition() {
		float sx = appWorldWidth / (canvas.getWidth() - 1);
		float sy = appWorldHeight / (canvas.getHeight() - 1);
		
		Matrix3x3f reverseScaleMatrix = Matrix3x3f.scale(sx, -sy);
		Point mousePoint = mouseInput.getPosition();
		
		return reverseScaleMatrix.mul(new Vector2f(mousePoint.x, mousePoint.y));
	}
	
	@Override
	public void run() {
		running = true;
		
		// All the game object initialization code will go in the initialize() method. However, window or canvas initialization code should not go here. They
		// should be kept in createAndShowGUI() method or in the constructor.
		initialize();
		
		long currentTimeNS = System.nanoTime();
		long lastTimeNS = currentTimeNS;
		double nsPerFrame;
		
		while(running) {
			currentTimeNS = System.nanoTime();
			nsPerFrame = currentTimeNS - lastTimeNS;
			gameLoop(nsPerFrame / 1.0E9);
			lastTimeNS = currentTimeNS;
		}
		
		// Post game termination code should go in the terminate() method
		terminate();
	}
	
	protected void initialize() {
		frameRate = new FrameRate();
		frameRate.initialize();
	}
	
	// We're keeping this empty by default
	protected void terminate() {
		
	}
	
	private void gameLoop(double delta) {
		processInput(delta);
		updateObjects(delta);
		renderFrame();
		
		try {
			Thread.sleep(appSleepTime);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected void processInput(double delta) {
		keyboardInput.poll();
		mouseInput.poll();
	}

	// We're keeping this empty by default
	protected void updateObjects(double delta) {
		
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
	
	// We're just rendering the FPS string by default in the boiler plate or template
	// code. The users can override this method and draw their own stuff
	protected void render(Graphics g) {
		frameRate.calculate();
		
		g.setFont(appFont);
		g.setColor(appFpsTextColor);
		g.drawString(frameRate.getFrameRate(), 20, 20);
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
	
	// This launcher function will be called from the main method to launch the app
	protected static void launchApp(final SimpleFramework app) {
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
