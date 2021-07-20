package javagames.render;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javagames.util.FrameRate;

public class HelloWorldApp extends JFrame{
	private FrameRate frameRate;
	
	public HelloWorldApp() {
		frameRate = new FrameRate();
	}

	private void createAndShowGUI() {
		GamePanel gamePanel = new GamePanel();
		gamePanel.setBackground(Color.BLACK);
		gamePanel.setPreferredSize(new Dimension(320, 240));
		getContentPane().add(gamePanel);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Hello World!");
		pack();
		frameRate.initialize();
		setVisible(true);
	}
	
	public static void main(String[] args) {
		HelloWorldApp helloWorldApp = new HelloWorldApp();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				helloWorldApp.createAndShowGUI();
			}
		});
	}
	
	// The GamePanel class where components are drawn
	class GamePanel extends JPanel {
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			
			frameRate.calculate();
			g.setColor(Color.WHITE);
			g.drawString(frameRate.getFrameRate(), 30, 30);

			repaint();
		}
	}
}
