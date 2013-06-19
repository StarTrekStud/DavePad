/*
 * DavePad
 * @author Dave Oji
 * @version 0.1
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

class DavePad {
	public static void main(String[] args) {
		DavePad gui = new DavePad();
	}
	
	private JFrame frame;
	private JTextArea text;
	private JLabel lineCol;
	private InputStream fontLocation;
	private Font inputFont;
	private Font textFont;
	private JPanel textPanel;
	private JScrollPane textScrollPane;
	private JFileChooser fcOpen;
	private JFileChooser fcSave;
	private Date d;
	
	public DavePad() {
		frame = new JFrame("DavePad");
		frame.setJMenuBar(createMenuBar());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(450, 550));
		frame.setLocation(600, 300);
		text = new JTextArea(25, 50);
		text.setTabSize(3);
		text.setMargin(new Insets(2, 4, 2, 4));
		text.setFocusable(true);
		text.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				try {
					int caretpos = text.getCaretPosition();
					int line = text.getLineOfOffset(caretpos);
					int col = text.getCaretPosition() - text.getLineStartOffset(line);
					updateCaretPos(line, col);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		lineCol = new JLabel("Line: " + " Column: ");
		JPanel lineColPanel = new JPanel();
		lineColPanel.setLayout(new BorderLayout());
		lineColPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		lineColPanel.add(lineCol, BorderLayout.EAST);
		frame.setLayout(new BorderLayout());
		textPanel = new JPanel();
		textPanel.setLayout(new BorderLayout());
		textPanel.setBackground(Color.lightGray);
		textScrollPane = new JScrollPane(text);
		textScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		textPanel.add(textScrollPane, BorderLayout.CENTER);
		textPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		frame.add(textPanel, BorderLayout.CENTER);
		frame.add(lineColPanel, BorderLayout.SOUTH);
		fontLocation = getClass().getResourceAsStream("DavePad.ttf");
		
		try {
			inputFont = Font.createFont(Font.TRUETYPE_FONT, fontLocation);
		} catch (FontFormatException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Font formatting exception!");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "I/O Exception - can't find font!");			
		}
		
		textFont = inputFont.deriveFont((13.0f));
		text.setFont(textFont);
		frame.setVisible(true);
	}
	
	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem fileOpen = new JMenuItem("Open...");
		JMenuItem fileSave = new JMenuItem("Save...");
		fileSave.addActionListener(new FileSaveListener());
		fileOpen.addActionListener(new FileOpenListener());
		fileMenu.add(fileOpen); 
		fileMenu.add(fileSave);
		JMenu textMenu = new JMenu("Text");
		JMenuItem textConvertToCSV = new JMenuItem("Convert To CSV...");
		JMenuItem textConvertToStory = new JMenuItem("Convert To Story...");
		textConvertToCSV.addActionListener(new csvListener());
		textConvertToStory.addActionListener(new storyListener());
		textMenu.add(textConvertToCSV);
		textMenu.add(textConvertToStory);
		menuBar.add(fileMenu); menuBar.add(textMenu);
		return menuBar;
	}
	
	public class FileSaveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			fcSave = new JFileChooser();
			d = new Date();
			int returnVal = fcSave.showSaveDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					File saveFile = fcSave.getSelectedFile();
					PrintStream ps = new PrintStream(saveFile);
					ps.print(text.getText());
					System.out.println("DavePad successfully saved @ " + d.toString());
				} catch (FileNotFoundException fnfe) {
					System.out.println("Can't locate save file!");
					fnfe.printStackTrace();
				} 
			} else {
				System.out.println("DavePad save cancelled @ " + d.toString());
			}
		}
	}
	
	public class FileOpenListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			fcOpen = new JFileChooser();
			d = new Date();
			int returnVal = fcOpen.showOpenDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					File openFile = fcOpen.getSelectedFile();
					BufferedReader openReader = new BufferedReader(new FileReader(openFile));
					text.read(openReader, "Reader for the text area.");
					while(openReader.ready()) {
						openReader.read();
					}
					System.out.println("DavePad successfully opened @ " + d.toString());
				} catch (FileNotFoundException fnfe) {
					System.out.println("Can't locate open file!");
					fnfe.printStackTrace();
				} catch (IOException ioe) {
					System.out.println("I/O Exception!");
					ioe.printStackTrace();
				}
			}
		}
	}
	
	public class csvListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String[] lines;
			String[] elements;
			String[] actions;
			String[] steps;
			String content = "";
			String newline = System.getProperty("line.separator");
			lines = text.getText().split("\\n");
			content = content + "Action,LocatorType,LocatorValue,Data,Assertion" + newline;
			for (int i = 0; i < lines.length; i++) {
				if (lines[i].indexOf("Action,LocatorType,LocatorValue,Data,Assertion") != -1) {
					break;
				}
				else {
					if (lines[i].indexOf("ELEMENT") != -1) {
						elements = lines[i].split("ELEMENT");	
						if (elements.length >= 2) {
							actions = elements[0].split(" ");
							//System.out.println(actions[2]);
							steps = elements[1].split("=");
							if (actions[2].equals("click")) {
								content = content + actions[2].trim() + "," + steps[0].trim() + "," + steps[1].trim() + newline;
							}
							if (actions[2].equals("clickAt")) {
								content = content + actions[2].trim() + "," + steps[0].trim() + "," + steps[1].trim() + newline;
							}
							if (actions[2].equals("contextMenuAt")) {
								content = content + actions[2].trim() + "," + steps[0].trim() + "," + steps[1].trim() + newline;
							}
							if (actions[2].equals("doubleClick")) {
								content = content + actions[2].trim() + "," + steps[0].trim() + "," + steps[1].trim() + newline;
							}
							if (actions[2].equals("waitForElementPresent")) {
								content = content + actions[2].trim() + "," + steps[0].trim() + "," + steps[1].trim() + newline;
							}
							if (actions[2].equals("waitForElementNotPresent")) {
								content = content + actions[2].trim() + "," + steps[0].trim() + "," + steps[1].trim() + newline;
							}
							if (actions.length >= 4) {
								if (actions[2].equals("select")) {
									content = content + actions[2].trim() + "," + steps[0].trim() + "," + steps[1].trim() + "," + actions[3].trim() + newline;
								}
								if (actions[2].equals("type")) {
									content = content + actions[2].trim() + "," + steps[0].trim() + "," + steps[1].trim() + "," + actions[3].trim() + newline;
								}
								if (actions[2].equals("verifyText")) {
									content = content + actions[2].trim() + "," + steps[0].trim() + "," + steps[1].trim() + "," + actions[3].trim() + newline;
								}
							}
						}
					}	
					else {
						if (lines[i].indexOf(" ") != -1) {
							actions = lines[i].split(" ");
							if (actions.length >= 3) {
								//System.out.println(actions[2]);
								if (actions[2].trim().equals("getAlert")) {
									content = content + actions[2].trim() + "," + "," + "," + "," + newline;
								}
								if (actions[2].trim().equals("getConfirmation")) {
									content = content + actions[2].trim() + "," + "," + "," + "," + newline;
								}
								if (actions.length >= 4) {
									if (actions[2].equals("pause")) {
										content = content + actions[2].trim() + "," + "," + "," + actions[3].trim() + newline;
									}
								}
							}
						}
					}
					text.selectAll();
					text.replaceSelection(content);
				}
			}
		}
	}
	
	public class storyListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String[] lines;
			String[] steps;
			String content = "";
			String newline = System.getProperty("line.separator");
			lines = text.getText().split("\\n");
			for (int i = 0; i < lines.length; i++) {
				if (lines[0].contains("Narrative: ")) {
					break;
				}
				else {
					if (i == 0) {
						content = content + "Narrative: " + newline + "Scenario: " + newline + "Given browser is at " + newline;
					}
					steps = lines[i].split(",");
					//System.out.println(steps[0]);
					if (steps[0].equals("click")) {
						content = content + "When I " + steps[0] + " ELEMENT " + steps[1] + " = " + steps [2] + newline;
					}
					if (steps[0].equals("clickAt")) {
						content = content + "When I " + steps[0] + " ELEMENT " + steps[1] + " = " + steps [2] + newline;
					}
					if (steps[0].equals("contextMenuAt")) {
						content = content + "When I " + steps[0] + " ELEMENT " + steps[1] + " = " + steps [2] + newline;
					}
					if (steps[0].equals("doubleClick")) {
						content = content + "When I " + steps[0] + " ELEMENT " + steps[1] + " = " + steps [2] + newline;
					}
					if (steps[0].equals("pause")) {
						content = content + "When I " + steps[0] + " " + steps[3] + " milliseconds" + newline;
					}
					if (steps[0].equals("select")) {
						content = content + "When I " + steps[0] + " " + steps[3] + " in ELEMENT " + steps[1] + " = " + steps [2] + newline;
					}
					if (steps[0].equals("type")) {
						content = content + "When I " + steps[0] + " " + steps[3] + " in ELEMENT " + steps[1] + " = " + steps [2] + newline;
					}
					if (steps[0].equals("verifyText")) {
						content = content + "When I " + steps[0] + " " + steps[3] + " in ELEMENT " + steps[1] + " = " + steps [2] + newline;
					}
					if (steps[0].equals("waitForElementPresent")) {
						content = content + "When I " + steps[0] + " ELEMENT " + steps[1] + " = " + steps [2] + newline;
					}
					if (steps[0].equals("waitForElementNotPresent")) {
						content = content + "When I " + steps[0] + " ELEMENT " + steps[1] + " = " + steps [2] + newline;
					}
					if (steps[0].equals("getAlert")) {
						content = content + "When I " + steps[0] + newline;
					}
					if (steps[0].equals("getConfirmation")) {
						content = content + "When I " + steps[0] + newline;
					}
					if (i + 1 == lines.length) {
						content = content + "Then the browser should close";
						text.selectAll();
						text.replaceSelection(content);
					}
				}
			}
		}
	}
	
	private void updateCaretPos(int linenum, int colnum) {
		linenum++; colnum++;
		lineCol.setText("Line: " + linenum + " Column: " + colnum);
	}
}