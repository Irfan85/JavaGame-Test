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
import javagames.util.RelativeMouseInput;

public class TimeDeltaExample extends JFrame implements Runnable {
	private FrameRate frameRate;
	private BufferStrategy bs;
	private volatile boolean running;
	private Thread gameThread;
	
	private KeyboardInput keyboardInput;
	private RelativeMouseInput mouseInput;
	
	private Canvas canvas;
	
	private float angle;
	private float step;
	private long sleepTime;
	
	private void createAndShowGUI() {
		canvas = new Canvas();
		canvas.setSize(480, 480);
		canvas.setBackground(Color.WHITE);
		canvas.setIgnoreRepaint(true);
		
		getContentPane().add(canvas);
		setTitle("Time Delta Example");
		setIgnoreRepaint(true);
		pack();
		
		keyboardInput = new KeyboardInput();
		canvas.addKeyListener(keyboardInput);
		
//		mouseInput = new RelativeMouseInput(canvas);
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
			gameLoop(nsPerFrame / 1.0E9);
			lastTimeNS = currentTimeNS;
		}
	}
	
	private void initialize() {
		frameRate = new FrameRate();
		frameRate.initialize();
		
		angle = 0.0f;
		// We want to rotate step by step such that our line completes PI/2 radians
		// or 90 degrees in 1 second
		step = (float) Math.PI / 2.0f;
	}
	
	private void gameLoop(double timeDelta) {
		processInput(timeDelta); // In this argument, processInput function won't make use of the timeDelta. So this parameter is redundant
		updateObjects(timeDelta);
		renderFrame();

		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void processInput(double timeDelta) {
		keyboardInput.poll();
//		mouseInput.poll();
		
		// Increase sleep time when user presses up arrow. This will decrease frame rate
		if(keyboardInput.keyDownOnce(KeyEvent.VK_UP)) {
			sleepTime += 10;
		}
		
		// Decrease sleep time when user presses down arrow. This will increase frame rate
		if(keyboardInput.keyDownOnce(KeyEvent.VK_DOWN)) {
			sleepTime -= 10;
		}
		
		// We don't want to exceed the delay of 1000ms (1s) since this may cause input lags
		if(sleepTime > 1000) {
			sleepTime = 1000;
		}
		
		if(sleepTime < 0) {
			sleepTime = 0;
		}
	}
	
	private void updateObjects(double deltaTime) {
		// Our angle will increase frame by by at such a rate so that when 1s will be
		// passed, our angle will be increased by 90 degrees. This is because we have set the step
		// size as PI/2 (90 degrees) and also calculated our deltaTime as a fraction of seconds. So, when
		// the overall total deltaTime will reach 1 which actually stands for 1 full second, our angle will be increased by a full
		// 'step' or 90 degrees. As we're relying on timeDelta rather than the frame rate, this rate of change of the angle won't be affected
		// by the frameRate and will behave same on both slow and fast computers. Yes, just like any other game, the animation will look smooth when the framerate is high
		// but the rate of change will be in every platform.
		angle += step * deltaTime;
		
		if(angle > 2*Math.PI) {
			angle -= 2*Math.PI;
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
			}while(bs.contentsRestored());
			
			bs.show();
		}while(bs.contentsLost());
	}
	
	private void render(Graphics g) {
		frameRate.calculate();
		g.setColor(Color.BLACK);
		g.drawString(frameRate.getFrameRate(), 20, 20);
		g.drawString("Up arrow increases sleep time", 20, 35);
		g.drawString("Down arrow decreases sleep time", 20, 50);
		g.drawString("Sleep time (ms): " + sleepTime, 20, 65);
		
		// Top-left point of the circle
		int x = canvas.getWidth() / 4;
		int y = canvas.getHeight() / 4;
		// Width and height of the circle. We want to make it half of the canvas width and height. It will change it's size if we resize the window
		int w = canvas.getWidth() / 2;
		int h = canvas.getHeight() / 2;
		g.drawOval(x, y, w, h);
		
		// Our circle/oval will change shape with as we resize the window. So we need to calculate
		// radius of both height and width sides
		float rw = w/2;
		float rh = h/2;
		// One end of the line will be at center (w, h). Another will will be on the circumference. We have to determine the point on
		// the circumference in Cartesian format. We already know the polar format since we know the radius and the angle. We will utilize
		// these informations for conversion
		int rx = (int) (rw * Math.cos(angle)); // Cartesian x
		int ry = (int) (rh * Math.sin(angle)); // Cartesian y
		
		// In order to put the Cartesian point on the circumference of the circle, we need to add them with center point of the circle
		// The following formula does this thing. If we don't do this our point will not be on the circumference and sometime go beyond the window as
		// sin and cos of an angle can also be negative. Actually what happened is when we determined the Cartesian points, it actually determined the points on the 
		// circumference of the circle that was centered at (0, 0) or the top-left corer of the screen. So we're just shifting the circle by width/2 and height/2 in order
		// to bring it in the middle of the screen where we actually drew our circle before.
		int cx = (int) (rx+w);
		int cy = (int) (ry+h);
		
		// Draw clock hand
		// 'w' and 'h' corresponds to the center of the circle.
		g.drawLine(w, h, cx, cy);
		
		// Draw dot at the end
		g.drawRect(cx-2, cy-2, 4, 4);
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
		final TimeDeltaExample app = new TimeDeltaExample();
		app.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				app.onWindowClosing();
			}
		});
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				app.createAndShowGUI();
			}
		});
	}

}
