package javagames.transform;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javagames.chapter01_util.FrameRate;
import javagames.chapter02_util.KeyboardInput;
import javagames.chapter03_util.Vector2f;

public class VectorGraphicsExample extends JFrame implements Runnable {
	
	private static final int SCREEN_W = 640;
	private static final int SCREEN_H = 480;
	
	private FrameRate frameRate;
	private BufferStrategy bs;
	private volatile boolean running;
	private Thread gameThread;
//	private RelativeMouseInput mouseInput;
	private KeyboardInput keyboardInput;
	
	private Vector2f[] polygon; // The polygon that will store the original data
	private Vector2f[] world; 	// The polygon where we'll make changes to manipulate it in the world
	
	private float tx, ty;
	private float vx, vy; // x and y velocity
	private float rot, rotStep;
	private float scale, scaleStep;
	private float sx, sxStep;
	private float sy, syStep;
	private boolean doTranslate;
	private boolean doScale;
	private boolean doRotate;
	private boolean doXShear;
	private boolean doYShear;
	
	public VectorGraphicsExample() {
		
	}
	
	private void createAndShowGUI() {
		Canvas canvas = new Canvas();
		canvas.setSize(SCREEN_W, SCREEN_H);
		canvas.setBackground(Color.BLACK);
		canvas.setIgnoreRepaint(true);
		
		getContentPane().add(canvas);
		setTitle("Vector Graphics Example");
		setIgnoreRepaint(true);
		pack();
		
		keyboardInput = new KeyboardInput();
		canvas.addKeyListener(keyboardInput);
		
//		mouseInput = new RelativeMouseInput(canvas);
//		canvas.addMouseListener(mouseInput);
//		canvas.addMouseMotionListener(mouseInput);
//		canvas.addMouseWheelListener(mouseInput);
		
		canvas.createBufferStrategy(2);
		bs = canvas.getBufferStrategy();
		canvas.requestFocus();
		
		setVisible(true);

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
		
		polygon = new Vector2f[] {
				new Vector2f(10, 0),
				new Vector2f(-10, 8),
				new Vector2f(0, 0),
				new Vector2f(-10, -8)
		};
		
		world = new Vector2f[polygon.length];
		reset();
	}
	
	private void reset() {
		tx = SCREEN_W / 2;
		ty = SCREEN_H / 2;
		vx = vy = 2;
		rot = 0.0f;
		rotStep = (float) Math.toRadians(1.0);
		scale = 1.0f;
		scaleStep = 0.1f;
		sx = sy = 0.0f;
		sxStep = syStep = 0.01f;
		doRotate = doScale = doTranslate = false;
		doXShear = doYShear = false;
	}
	
	private void gameLoop() {
		processInput();
		processObjects();
		renderFrame();
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void processInput() {
		keyboardInput.poll();
//		mouseInput.poll();
		
		if(keyboardInput.keyDownOnce(KeyEvent.VK_R))
			doRotate = !doRotate;
		if(keyboardInput.keyDownOnce(KeyEvent.VK_S))
			doScale = !doScale;
		if(keyboardInput.keyDownOnce(KeyEvent.VK_T))
			doTranslate = !doTranslate;
		if(keyboardInput.keyDownOnce(KeyEvent.VK_X))
			doXShear = !doXShear;
		if(keyboardInput.keyDownOnce(KeyEvent.VK_Y))
			doYShear = !doYShear;
		if(keyboardInput.keyDownOnce(KeyEvent.VK_SPACE))
			reset();
	}
	
	private void processObjects() {
		// Copy the data of the polygon at the original position
		// We'll make our changes in the 'world' polygon. This will help use to reset to the original polygon
		// Whenever we want
		for(int i = 0; i < polygon.length; i++)
			world[i] = new Vector2f(polygon[i]);
		
		if(doScale) {
			scale += scaleStep;
			if(scale < 1.0 || scale > 5.0) {
				scaleStep = -scaleStep;
			}
		}
		
		if(doRotate) {
			rot += rotStep;
			if(rot < 0.0f || rot > 2 * Math.PI) {
				rotStep = -rotStep;
			}
		}
		
		if(doTranslate) {
			tx += vx;
			if(tx < 0 || tx > SCREEN_W) {
				vx = -vx;
			}
			
			ty += vy;
			if(ty < 0 || ty > SCREEN_H) {
				vy = -vy;
			}
		}
		
		if(doXShear) {
			sx += sxStep;
			if(Math.abs(sx) > 2.0) {
				sxStep = -sxStep;
			}
		}
		
		if(doYShear) {
			sy += syStep;
			if(Math.abs(sy) > 2.0) {
				syStep = -syStep;
			}
		}
		
		for(int i = 0; i < world.length; i++) {
			// The ordering is important here and the result will change depending on the order of operations
			world[i].shear(sx,  sy);
			world[i].scale(scale, scale);
			world[i].rotate(rot);
			world[i].translate(tx, ty);
		}
	}
	
	private void renderFrame() {
		do {
			do {
				Graphics g = null;
				try {
					g = bs.getDrawGraphics();
					g.clearRect(0,  0, getWidth(), getHeight());
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
		g.setFont(new Font("Courier New", Font.PLAIN, 12));
		g.setColor(Color.GREEN);
		frameRate.calculate();
		
		g.drawString(frameRate.getFrameRate(), 20, 20);
		g.drawString("Translate(T): " + doTranslate, 20, 35);
		g.drawString("Rotate(R): " + doRotate, 20, 50);
		g.drawString("Scale(S): " + doScale, 20, 65);
		g.drawString("X-Shear(X): " + doXShear, 20, 80);
		g.drawString("Y-Shear(Y): " + doYShear, 20, 95);
		g.drawString("Press [SPACE] to reset: ", 20, 110);
		
		// Draw lines from point 'S' to 'P'
		Vector2f S = world[world.length - 1];
		Vector2f P = null;
		
		for(int i = 0; i < world.length; i++) {
			P = world[i];
			g.drawLine((int) S.x, (int) S.y, (int) P.x, (int) P.y);
			S = P;
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
		VectorGraphicsExample app = new VectorGraphicsExample();
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
