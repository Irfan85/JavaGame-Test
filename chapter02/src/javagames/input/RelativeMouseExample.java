package javagames.input;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javagames.chapter01_util.FrameRate;
import javagames.chapter02_util.KeyboardInput;
import javagames.chapter02_util.RelativeMouseInput;

public class RelativeMouseExample extends JFrame implements Runnable {
	
	private BufferStrategy bufferStrategy;
	private volatile boolean isRunning;
	private Thread gameThread;
	private Canvas canvas;
	private Point point;
	private boolean disableCursor;

	private FrameRate frameRate;
	private KeyboardInput keyboardInput;
	private RelativeMouseInput mouseInput;
	
	public RelativeMouseExample() {
		frameRate = new FrameRate();
		point = new Point(0, 0);
		disableCursor = false;
	}
	
	private void createAndShowGUI() {
		canvas = new Canvas();
		canvas.setSize(640, 480);
		canvas.setBackground(Color.BLACK);
		canvas.setIgnoreRepaint(true);

		keyboardInput = new KeyboardInput();
		canvas.addKeyListener(keyboardInput);
		
		mouseInput = new RelativeMouseInput(canvas); // This parameter would be "this" for full screen mode
		canvas.addMouseListener(mouseInput);
		canvas.addMouseMotionListener(mouseInput);
		canvas.addMouseWheelListener(mouseInput);
		
		getContentPane().add(canvas);
		pack();
		setVisible(true);
		
		canvas.createBufferStrategy(2);
		bufferStrategy = canvas.getBufferStrategy();
		canvas.requestFocus();
		
		gameThread = new Thread(this);
		gameThread.start();
	}
	
	@Override
	public void run() {
		isRunning = true;
		frameRate.initialize();
		
		while(isRunning) {
			gameLoop();
		}
	}
	
	private void gameLoop() {
		processInputs();
		renderFrame();
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void processInputs() {
		keyboardInput.poll();
		mouseInput.poll();
		
		Point p = mouseInput.getPosition();
		if(mouseInput.isRelative()) {
			point.x += p.x;
			point.y += p.y;
		} else {
			point.x = p.x;
			point.y = p.y;
		}
		
		// Wrap the rectangle around the screen
		if(point.x + 25 < 0)
			point.x = canvas.getWidth() - 1;
		else if (point.x > canvas.getWidth() - 1)
			point.x = -25;
		
		if(point.y + 25 < 0)
			point.y = canvas.getHeight() - 1;
		else if(point.y > canvas.getHeight() - 1)
			point.y = -25;
		
		// Press 'space' to toggle relative mode
		if(keyboardInput.keyDownOnce(KeyEvent.VK_SPACE))
			mouseInput.setRelative(!mouseInput.isRelative());
		
		// Press 'C' to toggle cursor visibility
		if(keyboardInput.keyDownOnce(KeyEvent.VK_C)) {
			disableCursor = !disableCursor;
			if(disableCursor)
				hideCursor();
			else
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			
		}
	}
	
	private void hideCursor() {
		// Put an empty image container as a cursor. This will hide the cursor
		Toolkit toolkit = Toolkit.getDefaultToolkit(); // java.awt.Toolkit
		Image image = toolkit.createImage(""); // An empty path means no image
		Point point = new Point(0, 0);
		String name = "aRandomCursorName";
		Cursor cursor = toolkit.createCustomCursor(image, point, name);
		setCursor(cursor);
	}
	
	private void renderFrame() {
		do {
			do {
				Graphics g = null;
				try {
					g = bufferStrategy.getDrawGraphics();
					g.clearRect(0, 0, getWidth(), getHeight());
					render(g);
				} finally {
					if(g != null)
						g.dispose();
				}
			} while (bufferStrategy.contentsRestored());
			
			bufferStrategy.show();
		} while(bufferStrategy.contentsLost());
	}
	
	private void render(Graphics g) {
		frameRate.calculate();
		
		g.setColor(Color.GREEN);
		g.drawString(mouseInput.getPosition().toString(), 20, 20);
		g.drawString("Relative: " + mouseInput.isRelative(), 20, 35);
		g.drawString("Press Space to switch mouse modes", 20, 50);
		g.drawString("Press C to toggle cursor visibility", 20, 65);
		g.drawOval(mouseInput.getComponentCenter().x, mouseInput.getComponentCenter().y, 5, 5);
		
		g.setColor(Color.WHITE);
		g.drawRect(point.x, point.y, 25, 25);
	}
	
	private void onWindowClosing() {
		isRunning = false;
		try {
			gameThread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static void main(String[] args) {
		RelativeMouseExample app = new RelativeMouseExample();
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
