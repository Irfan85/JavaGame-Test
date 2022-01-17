package javagames.render;

import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class DisplayModeExample extends JFrame {
	private DisplayMode currentDisplayMode;
	private GraphicsDevice graphicsDevice;
	private JComboBox<DisplayModeWrapper> displayModeComboBox;

	public DisplayModeExample() {
		// Getting user graphics device from the local graphics environment
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
		// Getting the current(Windowed) display mode
		currentDisplayMode = graphicsDevice.getDisplayMode();
		// Populating the available display modes list
		displayModeComboBox = new JComboBox<DisplayModeWrapper>(listDisplayModes());
	}

	private DisplayModeWrapper[] listDisplayModes() {
		ArrayList<DisplayModeWrapper> displayModeWrappers = new ArrayList<>();

		for (DisplayMode displayMode : graphicsDevice.getDisplayModes()) {
			// We will only use 32 bit modes. It's also accepted if the device supports
			// multiple bit depths
			if (displayMode.getBitDepth() == 32 || displayMode.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI) {
				DisplayModeWrapper displayModeWrapper = new DisplayModeWrapper(displayMode);
				// Same resolution but different bit modes may result in duplicates. So we're
				// making
				// sure each displayMode wrapper appears only once. We had to implement
				// DisplayModeWrapper
				// 'equals' method to utilize the ArrayList 'contains' method
				if (!displayModeWrappers.contains(displayModeWrapper)) {
					displayModeWrappers.add(displayModeWrapper);
				}
			}
		}

		// Java doesn't support array casting. So ArrayList provides this method
		return displayModeWrappers.toArray(new DisplayModeWrapper[0]);
	}

	private JPanel createMainPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.add(displayModeComboBox);

		JButton enterFullScreenButton = new JButton("Enter Full Screen");
		enterFullScreenButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onEnterFullScreen();
			}
		});
		mainPanel.add(enterFullScreenButton);

		JButton exitFullScreenButton = new JButton("Exit Full Screen");
		exitFullScreenButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onExitFullScreen();
			}
		});
		mainPanel.add(exitFullScreenButton);

		return mainPanel;
	}

	public void onEnterFullScreen() {
		// Check if the graphics device supports full screen
		if (graphicsDevice.isFullScreenSupported()) {
			// Get the selected DisplayMode from the comboBox
			DisplayMode fullScreenDisplayMode = getSelectedDisplayMode();
			// Enter full screen
			graphicsDevice.setFullScreenWindow(this);
			graphicsDevice.setDisplayMode(fullScreenDisplayMode);
		}
	}

	public void onExitFullScreen() {
		// At first check whether we are actually in full-screen mode or not
		if(graphicsDevice.getFullScreenWindow() != null) {
			// Go back to the windowed display mode
			graphicsDevice.setDisplayMode(currentDisplayMode);
			graphicsDevice.setFullScreenWindow(null);
		} else {
			System.out.println("Already in windowed mode.");
		}
	}

	public DisplayMode getSelectedDisplayMode() {
		DisplayModeWrapper selectedDisplayModeWrapper = (DisplayModeWrapper) displayModeComboBox.getSelectedItem();
		DisplayMode displayMode = selectedDisplayModeWrapper.displayMode;
		return displayMode;
	}

	private void createAndShowGUI() {
		// There are many ways to add swing components in a JFrame. This is just one of them.
		Container container = getContentPane();
		container.add(createMainPanel());
		container.setIgnoreRepaint(true);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Display Mode Test");
		pack();
		setVisible(true);
	}

	public static void main(String[] args) {
		DisplayModeExample app = new DisplayModeExample();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				app.createAndShowGUI();
			}
		});
	}

	// Creating a wrapper class for DisplayMode to easily list them in swing
	// components
	class DisplayModeWrapper {
		private DisplayMode displayMode;

		public DisplayModeWrapper(DisplayMode displayMode) {
			this.displayMode = displayMode;
		}

		@Override
		public boolean equals(Object obj) {
			DisplayModeWrapper otherDisplayModeWrapper = (DisplayModeWrapper) obj;

			if (displayMode.getHeight() != otherDisplayModeWrapper.displayMode.getHeight())
				return false;
			if (displayMode.getWidth() != otherDisplayModeWrapper.displayMode.getWidth())
				return false;

			return true;
		}

		@Override
		public String toString() {
			return displayMode.getWidth() + " x " + displayMode.getHeight();
		}
	}
}
