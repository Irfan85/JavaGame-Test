package javagames.render;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class RenderThreadExample extends JFrame implements Runnable {
	// 'volatile' keyword always keeps this variable in the main memory and
	// prevents JVM from caching it because it will be accessed by multiple
	// threads working inside different CPUs. So putting it in main memory will make sure
	// all the threads access the same data rather than any of the CPU caches
	private volatile boolean isRunning;
	private Thread gameThread;

	private void createAndShowGUI() {
		setSize(320, 240);
		setTitle("Render Thread");
		setVisible(true);

		gameThread = new Thread(this);
		gameThread.start();
	}

	@Override
	public void run() {
		isRunning = true;

		while (isRunning) {
			System.out.println("Game Loop");
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void onWindowClosing() {
		try {
			System.out.println("Stopping Thread...");
			isRunning = false;
			// Wait till the gameThread closes
			gameThread.join();
			System.out.println("Stopped!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.exit(0);
	}

	public static void main(String[] args) {
		RenderThreadExample app = new RenderThreadExample();
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
