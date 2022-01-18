package timeandspace;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javagames.util.FrameRate;
import javagames.util.KeyboardInput;
import javagames.util.Matrix3x3f;
import javagames.util.RelativeMouseInput;
import javagames.util.Vector2f;

public class CannonExample extends JFrame implements Runnable {
	private FrameRate frameRate;
	private BufferStrategy bs;
	private volatile boolean running;
	private Thread gameThread;
	private KeyboardInput keyboardInput;
	private RelativeMouseInput mouseInput;
	private Canvas canvas;
	
	private Vector2f[] cannon;
	private Vector2f[] worldCannon;
	private float cannonRot, cannonRotDelta;
	
	// We are assuming our bullet to be a single point for ease of calculation. When rendering, we will just draw a 2x2 pixel square around
	// this point in order to visualize it. That is why bullet is represented as a single Vector2f object
	private Vector2f bullet; // Geometry vector (A single point for our case) for the bullet to modify it's position
	private Vector2f worldBullet;
	private Vector2f velocity; // The velocity vector for the bullet
	
	private void createAndShowGUI() {
		canvas = new Canvas();
		canvas.setSize(640, 480);
		canvas.setBackground(Color.WHITE);
		canvas.setIgnoreRepaint(true);
		
		getContentPane().add(canvas);
		setTitle("Cannon Example");
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
		long lastTimeNS = currentTimeNS;
		double nsPerFrame;
		
		while(running) {
			currentTimeNS = System.nanoTime();
			nsPerFrame = currentTimeNS - lastTimeNS;
			gameLoop(nsPerFrame/1.0E9);
			lastTimeNS = currentTimeNS;
		}
	}
	
	private void initialize() {
		frameRate = new FrameRate();
		frameRate.initialize();
		
		velocity = new Vector2f();
		
		cannonRot = 0.0f;
		cannonRotDelta = (float)Math.toRadians(90.0);
		
		cannon = new Vector2f[] {
			new Vector2f(-0.5f, 0.125f),	// top-left
			new Vector2f(0.5f, 0.125f),		// top-right
			new Vector2f(0.5f, -0.125f),	// bottom-right
			new Vector2f(-0.5f, -0.125f)	// bottom-left
		};
		worldCannon = new Vector2f[cannon.length];
		
		// We are just shrinking the size of the cannon to 75%. Not necessary. Just for demonstration
		Matrix3x3f scale = Matrix3x3f.scale(0.75f, 0.75f);
		for(int i = 0; i < cannon.length; i++) {
			cannon[i] = scale.mul(cannon[i]);
		}
	}
	
	private void gameLoop(double timeDelta) {
		processInput(timeDelta);
		updateObjects(timeDelta);
		renderFrame();
		
		try {
			Thread.sleep(10);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void processInput(double timeDelta) {
		keyboardInput.poll();
		mouseInput.poll();
		
		// Press A to increase canon angle
		if(keyboardInput.keyDown(KeyEvent.VK_A)) {
			cannonRot += (cannonRotDelta * timeDelta);
		}
		
		// Press D to decrease canon angle
		if(keyboardInput.keyDown(KeyEvent.VK_D)) {
			cannonRot -= (cannonRotDelta * timeDelta);
		}
		
		if(keyboardInput.keyDownOnce(KeyEvent.VK_SPACE)) {
			
			// We are creating a velocity vector for the bullet. The bullet will move 7 units
			// per second along the x axis. Since our bullet will be a projectile, we are not setting any
			// y element of the velocity right now. It will be determined by the gravity that we will set in the updateObjects() function	
			velocity = new Vector2f(7, 0);
			// We want the velocity vector to rotate with the cannon. Otherwise the bullet will always shoot parallel
			// to ground	
			Matrix3x3f bulletRotationMatrix = Matrix3x3f.rotate(cannonRot);
			velocity = bulletRotationMatrix.mul(velocity);
			
			/*
			 * The above calculation can be done in the following way as well where we will firstly define the transformation matrix for the vector
			 * and then initialize a new vector(0, 0) and apply the transformation to it by multiplication.
			 *
			 * Matrix3x3f mat = Matrix3x3f.translate(7.0f, 0.0f);
			 * mat = mat.mul(Matrix3x3f.rotate(cannonRot));
			 * velocity = mat.mul(new Vector2f());
			 * 
			 * */
			
			// The bullet will also be created at the origin (0, 0) at first. We have to put appropriate transformations to put it in place
			// At first, lets create the transformation matrix for the bullet
			Matrix3x3f mat = Matrix3x3f.identity();
			// We want to the bullet to spawn at half the length of the cannon. Otherwise it will spawn at the middle of the cannon
			mat = Matrix3x3f.translate(0.375f, 0.0f);
			// We also have to rotate the bullet to match the direction of the cannon
			mat = mat.mul(Matrix3x3f.rotate(cannonRot));
			// Then translate it to the same lower left position where the cannon is located
			mat = mat.mul(Matrix3x3f.translate(-2.0f, -2.0f));
			// Then we create a new vector that will be zero in both direction by default and then apply the transformation matrix through multiplication
			// It doesn't matter the vector is initialize at (0, 0) since the transformation matrix will manipulate it and put in in the correct position 
			bullet = mat.mul(new Vector2f());
		}
	}
	
	private void updateObjects(double timeDelta) {
		// Whenever we create or spawn an object, it is created at the origin(0, 0). We have to apply transformation to it
		// to put it in its correct position.
		// Let's create the transformation matrix for the cannon which is spawned at (0, 0)
		Matrix3x3f mat = Matrix3x3f.identity();
		// Then, we will rotate the cannon according to the value of the angle
		mat = mat.mul(Matrix3x3f.rotate(cannonRot));
		// Next, we will move the cannon at bottom-left position
		mat = mat.mul(Matrix3x3f.translate(-2.0f, -2.0f));
		
		for(int i = 0; i < cannon.length; i++) {
			worldCannon[i] = mat.mul(cannon[i]);
		}
		
		if(bullet != null) {
			velocity.y += -(9.8 * timeDelta);
			bullet.x += (velocity.x * timeDelta);
			bullet.y += (velocity.y * timeDelta);
			worldBullet = new Vector2f(bullet);
			
			// If the bullet goes below the bottom edge of the window, destroy it
			if(bullet.y < -2.5f) {
				bullet = null;
			}
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
		frameRate.calculate();
		
		g.setColor(Color.BLACK);
		g.drawString(frameRate.getFrameRate(), 20, 20);
		g.drawString("(A) to raise, (D) to lower", 20, 35);
		g.drawString("Press Space to fire cannon", 20, 50);
		
		String velocityString = String.format("Velocity (%.2f, %.2f)", velocity.x, velocity.y);
		g.drawString(velocityString, 20, 65);
		
		float worldWidth = 5.0f;
		float worldHeight = 5.0f;
		float screenWidth = canvas.getWidth() - 1;
		float screenHeight = canvas.getHeight() - 1;
		
		float sx = screenWidth / worldWidth;
		float sy = screenHeight / worldHeight;
		float tx = screenWidth / 2.0f;
		float ty = screenHeight / 2.0f;
		
		Matrix3x3f viewPort = Matrix3x3f.scale(sx, -sy);
		viewPort = viewPort.mul(Matrix3x3f.translate(tx, ty));
		
		for(int i = 0; i < cannon.length; i++) {
			worldCannon[i] = viewPort.mul(worldCannon[i]);
		}
		
		drawPolygon(g, worldCannon);
		
		if(bullet != null) {
			worldBullet = viewPort.mul(worldBullet);
			g.drawRect((int)(worldBullet.x - 2), (int)(worldBullet.y - 2), 4, 4);
		}
	}
	
	private void drawPolygon(Graphics g, Vector2f[] polygon) {
		Vector2f p;
		Vector2f s = polygon[polygon.length - 1];
		
		for(int i = 0; i < polygon.length; i++) {
			p = polygon[i];
			g.drawLine((int)s.x, (int)s.y, (int)p.x, (int)p.y);
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
		final CannonExample app = new CannonExample();
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
