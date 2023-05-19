package javagames.input;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javagames.chapter01_util.FrameRate;
import javagames.chapter02_util.KeyboardInput;
import javagames.chapter02_util.SimpleMouseInput;

public class SimpleMouseExample extends JFrame implements Runnable {
	private FrameRate frameRate;
	private BufferStrategy bufferStrategy;
	private volatile boolean isRunning;
	private Thread gameThread;
	
	private KeyboardInput keyboardInput;
	private SimpleMouseInput mouseInput;
	
	private ArrayList<Point> linePoints;
	private boolean isCurrentlyDrawingLine;
	
	private Color[] colors = {
			Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW
	};
	private int selectedColor;
	
	public SimpleMouseExample() {
		frameRate = new FrameRate();
		linePoints = new ArrayList<>();
	}
	
	private void createAndShowGUI() {
		Canvas canvas = new Canvas();
		canvas.setSize(640, 480);
		canvas.setBackground(Color.black);
		canvas.setIgnoreRepaint(true);
		
		keyboardInput = new KeyboardInput();
		canvas.addKeyListener(keyboardInput);
		
		mouseInput = new SimpleMouseInput();
		canvas.addMouseListener(mouseInput);
		canvas.addMouseMotionListener(mouseInput);
		canvas.addMouseWheelListener(mouseInput);
		
		getContentPane().add(canvas);
		setTitle("Simple Mouse Example");
		setIgnoreRepaint(true);
		pack();
		setVisible(true);
		
		canvas.createBufferStrategy(2);
		bufferStrategy = canvas.getBufferStrategy();
		canvas.requestFocus(); // This will make the canvas start capturing inputs
		
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
		
		// If mouseButton is pressed for the first time, start drawing
		if(mouseInput.buttonDownOnce(MouseEvent.BUTTON1))
			isCurrentlyDrawingLine = true;
		// If user is currently pressing down the button, keep drawing lines
		if(mouseInput.buttonDown(MouseEvent.BUTTON1)) {
			linePoints.add(mouseInput.getPosition());
		// If the button is not pressed, but we're still in drawing mode, stop drawing
		// by inserting a null object to the line
		} else if(isCurrentlyDrawingLine) {
			linePoints.add(null);
			isCurrentlyDrawingLine = false;
		}
		
		// If 'C' is pressed, clear the drawing
		if(keyboardInput.keyDownOnce(KeyEvent.VK_C))
			linePoints.clear();
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
		} while (bufferStrategy.contentsLost());
	}
	
	private void render(Graphics g) {
		// We will cycle through the 4 colors defined in the array above by mouse wheel
		selectedColor += mouseInput.getNotches();
		Color color = colors[Math.abs(selectedColor % colors.length)];
		g.setColor(color);
		
		frameRate.calculate();
		g.drawString(frameRate.getFrameRate(), 30, 30);
		g.drawString("Use mouse to draw lines", 30, 45);
		g.drawString("Press C to clear the drawing", 30, 60);
		g.drawString("Turn mouse wheel to cycle through colors", 30, 75);
		g.drawString(mouseInput.getPosition().toString(), 30, 90);
		
		for(int i = 0; i < linePoints.size() - 1; i++) {
			Point p1 = linePoints.get(i);
			Point p2 = linePoints.get(i + 1);
			
			if((p1 != null) && (p2 != null)) {
				g.drawLine(p1.x, p1.y, p2.x, p2.y);
			}
		}
	}
	
	private void onWindowClosing() {
		isRunning = false;
		try {
			gameThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static void main(String[] args) {
		SimpleMouseExample app = new SimpleMouseExample();
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
