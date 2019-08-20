package edu.psu.geography;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.SwingUtilities;

/**
 * interface for map reverse data collector
 * 
 * @author xjz5168
 *
 */
public class MapReverseDataCollector extends JPanel implements ActionListener {
	static private final String newline = "\n";
	JButton searchButton;
	JButton chooseButton;
	JButton saveButton;
	JTextField textField;
	JTextField savePath;
	static JTextArea log;

	JCheckBox imageSearchChecker;
	JCheckBox imageDownloadChecker;
	JCheckBox labelDetectionChecker;
	JCheckBox entityDetectionChecker;

	public MapReverseDataCollector() {
		super(new BorderLayout());

		// Create the log first, because the action listeners
		// need to refer to it.
		log = new JTextArea(80, 120);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(log);

		searchButton = new JButton("Search");
		searchButton.addActionListener(this);

		textField = new JTextField(50);

		chooseButton = new JButton("Choose");
		chooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JFileChooser chooser = new JFileChooser();
				chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("choose a folder:");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (chooser.showSaveDialog(chooseButton) == JFileChooser.APPROVE_OPTION) {
					textField.setText(chooser.getSelectedFile().toString());
				} else {
					System.out.println("No Selection ");
				}

			}
		});

		imageSearchChecker = new JCheckBox("Image Search", true);
		imageDownloadChecker = new JCheckBox("Image Download", true);
		labelDetectionChecker = new JCheckBox("Image Label Detection", false);
		entityDetectionChecker = new JCheckBox("Web Entity Detection", false);

		// For layout purposes, put the buttons in a separate panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());

		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new FlowLayout());
		searchPanel.add(textField);
		searchPanel.add(chooseButton);
		searchPanel.add(searchButton);

		JPanel checkerPanel = new JPanel();
		checkerPanel.setLayout(new FlowLayout());
		checkerPanel.add(imageSearchChecker);
		checkerPanel.add(imageDownloadChecker);
		checkerPanel.add(labelDetectionChecker);
		checkerPanel.add(entityDetectionChecker);

		buttonPanel.add(searchPanel, BorderLayout.NORTH);
		buttonPanel.add(checkerPanel, BorderLayout.SOUTH);

		JPanel savePanel = new JPanel();
		savePanel.setLayout(new FlowLayout());
		savePath = new JTextField(50);

		saveButton = new JButton("Save To");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JFileChooser chooser = new JFileChooser();
				chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("choose a folder:");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showSaveDialog(saveButton) == JFileChooser.APPROVE_OPTION) {
					savePath.setText(chooser.getSelectedFile().toString());
				} else {
					System.out.println("No Selection ");
				}

			}
		});
		savePanel.add(savePath);
		savePanel.add(saveButton);

		// Add the buttons and the log to this panel.
		add(buttonPanel, BorderLayout.PAGE_START);
		add(savePanel, BorderLayout.CENTER);
		add(logScrollPane, BorderLayout.PAGE_END);

	}

	public void actionPerformed(ActionEvent e) {

		// Handle open button action.

		if (e.getSource() == searchButton) {
			log.setText("");
			String output = savePath.getText();
			String image_url = textField.getText();
			boolean b1 = imageSearchChecker.isSelected();
			boolean b2 = imageDownloadChecker.isSelected();
			boolean b3 = labelDetectionChecker.isSelected();
			boolean b4 = entityDetectionChecker.isSelected();
			File f = new File(output);
			if (!f.isDirectory()) {
				log.append("Not a valid output directory." + newline);
			} else {
				File image = new File(image_url);
				if (image.exists()) {

					if (image_url.endsWith(".jpg")||image_url.endsWith(".png")) {
						log.append("A valid local image." + newline);
						log.append("start query." + newline);
						MapReverseRunner.mapPostQuery(image_url, output, b1, b2, b3, b4);
					} else {
						log.append("It's NOT an image." + newline);
					}

				} else {
					boolean b = testImage(image_url);
					if (b) {
						log.append("A valid web image." + newline);
						log.append("start query." + newline);
						MapReverseRunner.mapGetQuery(image_url, output, b1, b2, b3, b4);
					} else {
						log.append("Not a valid file." + newline);
					}
				}
			}
			log.updateUI();
			log.setCaretPosition(log.getDocument().getLength());
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("MapReverse Data Collector");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add content to the window.
		frame.add(new MapReverseDataCollector());

		// Display the window.
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();
			}
		});
	}

	/**
	 * test a image link is validate.
	 */
	public Boolean testImage(String url) {
		try {
			BufferedImage image = ImageIO.read(new URL(url));
			if (image != null) {
				return true;
			} else {
				return false;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.err.println("URL error with image");
			return false;
		} catch (IOException e) {
			System.err.println("IO error with image");
			// TODO Auto-generated catch block
			return false;
		}
	}

}