/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 * Copyright 2013 Joseph Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package NewtynReconcile;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultCaret;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import excelUtils.ExcelUtils;
import excelUtils.FileUtils;
import excelUtils.Helper;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Task Outline:
 * 
 * 1. Importing operations
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

public class ReconcileGUI extends JPanel implements Function {
	private static final long serialVersionUID = -1720922260200783230L;

	private final static boolean debug = false;

	/* GUI Properties */
	private static JFrame frame;
	private JButton startButton;
	private JButton instructionButton;
	private JButton settingsButton;
	private JButton closeButton;
	private JEditorPane taskOutput;
	private JScrollPane outputScrollPane;
	private Task parent = null;

	private ThreadedTask task = null;


	/* Application settings */
	private Properties settings;
	private String userSettingsFilename = ".user_settings.properties";
	private String defaultSettingsFilename = ".default_settings.properties";
	private String userSettingsPath = FileUtils.joinPath(pwd, userSettingsFilename);
	private String defaultSettingsPath = FileUtils.joinPath(pwd, defaultSettingsFilename);
	private String setting_save_path;
	private String setting_advent_input_path;
	private String setting_input_path;
	private String setting_bnp_input_path;
	private String setting_gs_input_path;
	private String setting_citi_input_path;
	private String setting_citi_cash;
	private String setting_gs_cash_new;
	private String setting_gs_cash_nte;
	private String setting_bnp_cash;
	//private String setting_citi_mtm;
	private String setting_gs_mtm;
	private String setting_bnp_mtm;

	/* Private debugging simplified print methods */
	@SuppressWarnings("unused")
	private static void println() {
		if (debug) System.out.println();
	}
	@SuppressWarnings("unused")
	private static void print(Object o) {
		if (debug) System.out.print(o);
	}
	@SuppressWarnings("unused")
	private static void println(Object o) {
		if (debug) System.out.println(o);
	}


	/* * * * * * * * * * * * * * * * *
	 * 
	 * Constructors
	 * 
	 * * * * * * * * * * * * * * * * */
	public ReconcileGUI(final JFrame frame) {
		super(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		start(frame);
	}
	public ReconcileGUI(final JFrame frame, Task parentTask) {
		super(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.parent = parentTask;
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				if (parent != null) {
					parent.signalClose();
				}
				frame.dispose();
			}
		});
		start(frame);
	}

	/* BEGIN GUI METHODS */
	// Create GUI
	public void start(final JFrame frame) {
		frame.setMinimumSize(new Dimension(750,400));
		frame.setLayout(new BorderLayout());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.setMinimumSize(new Dimension(500,500));

		startButton = new JButton("Start");
		startButton.setActionCommand("start");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				disableButtons();
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				task = new ThreadedTask();
				task.execute();
				return;
			}
		});

		closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (parent != null) {
					parent.signalClose();
				}
				frame.dispose();
			}
		});

		instructionButton = new JButton("Instructions");
		instructionButton.setActionCommand("instructions");
		instructionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableButtons();
				createAndShowInstructions();
			}
		});

		settingsButton = new JButton("Settings");
		settingsButton.setActionCommand("settings");
		settingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableButtons();
				createAndShowSettings();
			}
		});
		
		
		taskOutput = new JEditorPane();
		taskOutput.setPreferredSize(new Dimension(400,400));
		taskOutput.setMargin(new Insets(5,5,5,5));
		taskOutput.setEditable(false);
		taskOutput.setContentType("text/html");
		outputScrollPane = new JScrollPane(taskOutput);
		taskOutput.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				scrollToBottom();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				scrollToBottom();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				scrollToBottom();
			}
		});;
		DefaultCaret caret = (DefaultCaret)taskOutput.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		taskOutput.setCaret(caret);

		buttonPanel.add(startButton);
		buttonPanel.add(instructionButton);
		buttonPanel.add(settingsButton);
		buttonPanel.add(closeButton);
		buttonPanel.setVisible(true);
		
		add(outputScrollPane, BorderLayout.NORTH);
		setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
		add(buttonPanel, BorderLayout.SOUTH);
		loadSettings();
	}
	private void loadSettings() {
		settings = new Properties();
		FileInputStream in = null;
		try {
			in = FileUtils.getFileInputStream(userSettingsPath);
			if (in == null) {
				println("default");
				in = FileUtils.getFileInputStream(defaultSettingsPath);
			}
			if (in != null) {
				settings.load(in);
				/* Save location */
				setting_save_path = settings.getProperty(
						"path_save",pwd);
				/* Input directories */
				setting_advent_input_path = settings.getProperty(
						"path_advent",FileUtils.joinPath("Z:","Newtyn","AdventOut","<DATE:YESTERDAY:MM.DD>"));
				setting_input_path = settings.getProperty(
						"path_input",FileUtils.joinPath("Z:","Newtyn","Daily Reports"));
				setting_citi_input_path = settings.getProperty(
						"path_citi",FileUtils.joinPath(setting_input_path,"CITI"));
				setting_bnp_input_path = settings.getProperty(
						"path_bnp",FileUtils.joinPath(setting_input_path,"BNP"));
				setting_gs_input_path = settings.getProperty(
						"path_gs",FileUtils.joinPath(setting_input_path,"Goldman","<DATE:TODAY:YYYYMMDD>"));
				/* File names */
				// Cash
				setting_citi_cash = settings.getProperty(
						"file_citi_cash","<NUMBER>_balancesgeneric_newtyn_<DATE:TODAY:YYYYMMDD>");
				setting_bnp_cash = settings.getProperty(
						"file_bnp_cash","bpmon.csv.<DATE:TODAY:YYMMDD><NUMBER>");
				setting_gs_cash_new = settings.getProperty(
						"file_gs_cash_new","CustodyCashBalances_<NUMBER>_NEWTYN_PRTNRS_LP");
				setting_gs_cash_nte = settings.getProperty(
						"file_gs_cash_nte","CustodyCashBalances_<NUMBER>_NEWTYN_TE_PRTNRS_LP");
				// MTM
				//setting_citi_mtm = settings.getProperty("file_citi_mtm","");
				setting_bnp_mtm = settings.getProperty(
						"file_bnp_mtm","markmkt.csv.<NUMBER>");
				setting_gs_mtm = settings.getProperty(
						"file_gs_mtm","SRPB_<NUMBER>_<NUMBER>_MTM_Report_<NUMBER>_<NUMBER>");
			} else {
				/* Save Directory */
				setting_save_path = FileUtils.joinPath("Z:","Newtyn","AdventOut","<DATE:YESTERDAY:MM.DD>");
				/* Input directories */
				setting_advent_input_path = pwd;
				File attemptToLocateInputFolder = FileUtils.locateFolder(
						"Daily Reports",(new File(pwd)).getParent());
				if (attemptToLocateInputFolder != null) {
					setting_input_path = attemptToLocateInputFolder.getAbsolutePath();
				} else {
					setting_input_path = FileUtils.joinPath("Z:","Newtyn","Daily Reports");
				}
				setting_citi_input_path = FileUtils.joinPath(setting_input_path,"Goldman");
				setting_bnp_input_path = FileUtils.joinPath(setting_input_path,"BNP");
				setting_gs_input_path = FileUtils.joinPath(setting_input_path,"CITI");
				/* File names */
				// Cash
				setting_citi_cash = "<NUMBER>_balancesgeneric_newtyn_<DATE:TODAY:YYYYMMDD>";
				setting_bnp_cash = "bpmon.csv.<DATE:TODAY:YYMMDD><NUMBER>";
				setting_gs_cash_new = "CustodyCashBalances_<NUMBER>_NEWTYN_PRTNRS_LP";
				setting_gs_cash_nte = "CustodyCashBalances_<NUMBER>_NEWTYN_TE_PRTNRS_LP";
				// MTM
				//setting_citi_mtm = "";
				setting_bnp_mtm = "markmkt.csv.<DATE:TODAY:YYMMDD><NUMBER>";
				setting_gs_mtm = "SRPB_<NUMBER>_<NUMBER>_MTM_Report_<NUMBER>_<NUMBER>";

				saveSettings(true);
			}

		} catch (IOException e) {
			if (debug) e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					if (debug) e.printStackTrace();
				}
			}
		}

	}
	private void saveSettings() {
		saveSettings(false);
	}
	private void saveSettings(boolean saveAsDefault) {
		FileOutputStream out = null;
		try {
			/* Save location */
			settings.setProperty("path_save",setting_save_path);
			/* Input directories */
			settings.setProperty("path_input",setting_input_path);
			settings.setProperty("path_bnp",setting_bnp_input_path);
			settings.setProperty("path_gs",setting_gs_input_path);
			settings.setProperty("path_citi",setting_citi_input_path);
			settings.setProperty("path_advent",setting_advent_input_path);
			/* Report file names */
			// Cash
			settings.setProperty("file_bnp_cash",setting_bnp_cash);
			settings.setProperty("file_gs_cash_new",setting_gs_cash_new);
			settings.setProperty("file_gs_cash_nte",setting_gs_cash_nte);
			settings.setProperty("file_citi_cash",setting_citi_cash);
			// MTM
			settings.setProperty("file_bnp_mtm",setting_bnp_mtm);
			settings.setProperty("file_gs_mtm",setting_gs_mtm);
			//settings.setProperty("file_citi_mtm",setting_citi_mtm);

			out = FileUtils.getFileOutputStream(userSettingsPath,true);
			settings.store(out, null);
			// Save default
			if (saveAsDefault) {
				out = FileUtils.getFileOutputStream(defaultSettingsPath,true);
				settings.store(out, null);
			}
		} catch (IOException e) {
			if (debug) e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					if (debug) e.printStackTrace();
				}
			}
		}
	}
	public void createAndShowInstructions() {
		final JDialog info = new JDialog(frame);
		info.setLayout(new BorderLayout());
		info.setMinimumSize(new Dimension(500,500));
		info.setResizable(debug);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		final JEditorPane text = new JEditorPane();
		text.setContentType("text/html");
		JScrollPane scrollPanel = new JScrollPane(text);
		scrollPanel.setPreferredSize(new Dimension(500,400));
		text.setText("<html>"
				+ "<head>"
				+ "<style>"
				+ "hr {"
				+ "margin: 0px;"
				+ "border: 0px;"
				+ "padding: 0px;"
				+ "}"
				+ "h1 {"
				+ "font-family: verdana, arial;"
				+ "}"
				+ "p {"
				+ "font-family: verdana, arial;"
				+ "}"
				+ "</style>"
				+ "</head>"
				+ "<body>"
				+ "<h1>Instructions</h1>"
				+ "<hr>"
				+ "<p>"
				+ "This program will import the appropriate files into the Newtyn Reconcile Portfolio."
				+ "</p>"
				+ "<p>"
				+ "The program may run into issues if the report needed for importing "
				+ "is currently open in Excel.  If an error arises, make sure the file is closed "
				+ "and re-run the program."
				+ "</p>"
				+ "<p>"
				+ "The program relies on the import directories listed in the settings.  These directories will be saved between usages so they only need to "
				+ "be modified on the first usage, or if there is a change to the location of the import files."
				+ "</p>"
				+ "<p>"
				+ "This program works by updating the last existing report file.  If this file is lost or damaged the most recent file should be used, by default"
				+ "this program will try to locate the most recent file."
				+ "</p>"
				+ "<p>"
				+ "There will likely be an error if any of these directories are not correct."
				+ "</p>"
				+ "<p>"
				+ "To use the program simply click the start button to begin."
				+ "<br>"
				+ "There will be an output of messages delivering status information about the import."
				+ "</p>"
				+ "<p>"
				+ "If there is a problem a warning or error message will be delivered with more information on how to fix the problem."
				+ "</p>"
				+ "</body>"
				+ "</html>");
		text.setEditable(false);
		text.setCaretPosition(0);

		JButton closeInstructions = new JButton("Close");
		closeInstructions.setActionCommand("close");
		closeInstructions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						info.dispose();
						enableButtons();
						frame.requestFocus();
					}
				});
			}
		});

		panel.add(closeInstructions,BorderLayout.SOUTH);
		panel.add(scrollPanel,BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
		info.add(panel);

		info.pack();
		info.addWindowFocusListener(new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				text.grabFocus();
				text.requestFocusInWindow();
			}
		});
		info.addWindowListener(new WindowAdapter()  {
			public void windowClosing(WindowEvent e) {
				info.dispose();
				enableButtons();
				frame.requestFocus();
			}
		});
		info.setVisible(true);
	}
	public void createAndShowSettings() {
		final JDialog settingsFrame = new JDialog(frame);
		settingsFrame.addWindowListener(new WindowAdapter()  {
			public void windowClosing(WindowEvent e) {
				settingsFrame.dispose();
				enableButtons();
				frame.requestFocus();
			}
		});
		settingsFrame.setMinimumSize(new Dimension(575,500));
		settingsFrame.setLayout(new BorderLayout());
		settingsFrame.setResizable(debug);

		JTabbedPane settingsTabs = new JTabbedPane();
		GridBagConstraints c = new GridBagConstraints();
		settingsFrame.add(settingsTabs,BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		settingsFrame.add(buttonPanel,BorderLayout.SOUTH);

		final JLabel title = new JLabel("Settings");
		title.setFont(new Font("arial",Font.BOLD,20));
		title.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
		settingsFrame.add(title,BorderLayout.NORTH);

		JPanel pathPanel = new JPanel();
		pathPanel.setLayout(new BorderLayout());
		pathPanel.setPreferredSize(new Dimension(400,400));
		JPanel pathSettingsPanel = new JPanel();
		pathSettingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		pathSettingsPanel.setLayout(new GridBagLayout());
		pathPanel.add(pathSettingsPanel,BorderLayout.NORTH);
		settingsTabs.addTab("Path Locations", pathPanel);

		JPanel filePanel = new JPanel();
		filePanel.setLayout(new BorderLayout());
		filePanel.setPreferredSize(new Dimension(400,400));
		JPanel fileSettingsPanel = new JPanel();
		fileSettingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		fileSettingsPanel.setLayout(new GridBagLayout());
		filePanel.add(fileSettingsPanel,BorderLayout.NORTH);
		settingsTabs.addTab("File Settings",filePanel);

		/* Path Settings */
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.0;
		c.weighty = 1.0;
		c.gridheight = 1;
		c.gridwidth = 1;

		JLabel saveLabel = new JLabel("<html>&nbsp;Save Location:</html>");
		c.gridwidth = 3;
		c.gridy = 0;
		c.gridx = 0;
		pathSettingsPanel.add(saveLabel,c);

		final JTextField saveField = new JTextField();
		saveField.setText(setting_save_path);
		saveField.setColumns(20);
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.gridy++;
		pathSettingsPanel.add(saveField,c);

		JButton locateSaveButton = new JButton("...");
		locateSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						File f = FileUtils.manualLocate("Choose Save Directory",true);
						if (f != null) {
							saveField.setText(f.getAbsolutePath());
						}
					}
				});
			}
		});
		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		pathSettingsPanel.add(locateSaveButton,c);


		JLabel saveDesc = new JLabel("<html>&nbsp;Path to the location this program will save reports to</html>");
		c.gridx = 0;
		c.gridwidth = 5;
		c.gridy++;
		pathSettingsPanel.add(saveDesc,c);

		c.gridwidth = 1;
		JPanel spacer = new JPanel();
		c.gridy++;
		pathSettingsPanel.add(spacer,c);

		/* Advent Path Setting */
		JLabel adventLabel = new JLabel("<html>&nbsp;Advent Output Location:</html>");
		c.gridx = 0;
		c.gridwidth = 3;
		c.gridy++;
		pathSettingsPanel.add(adventLabel,c);

		final JTextField adventField = new JTextField();
		adventField.setText(setting_advent_input_path);
		adventField.setColumns(20);
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.gridy++;
		pathSettingsPanel.add(adventField,c);

		JButton locateAdventButton = new JButton("...");
		locateAdventButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						File f = FileUtils.manualLocate("Locate AdventOut Directory",true);
						if (f != null) {
							adventField.setText(f.getAbsolutePath());
						}
					}
				});
			}
		});
		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		pathSettingsPanel.add(locateAdventButton,c);


		JLabel adventDesc = new JLabel("<html>&nbsp;Path to the location of Advent Axis output files</html>");
		c.gridx = 0;
		c.gridwidth = 5;
		c.gridy++;
		pathSettingsPanel.add(adventDesc,c);


		c.gridwidth = 1;
		spacer = new JPanel();
		c.gridy++;
		pathSettingsPanel.add(spacer,c);


		/* Location of other input files */
		JLabel inputLabel = new JLabel("<html>&nbsp;Broker Input Locations:</html>");
		inputLabel.setFont(new Font("arial",Font.PLAIN,16));
		c.gridx = 0;
		c.gridwidth = 3;
		c.gridy++;
		pathSettingsPanel.add(inputLabel,c);

		/* BNP Path Setting */
		JLabel bnpLabel = new JLabel("<html>&nbsp;BNP:</html>");
		c.gridx = 0;
		c.gridwidth = 3;
		c.gridy++;
		pathSettingsPanel.add(bnpLabel,c);

		final JTextField bnpField = new JTextField();
		bnpField.setText(setting_bnp_input_path);
		bnpField.setColumns(20);
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.gridy++;
		pathSettingsPanel.add(bnpField,c);

		JButton locateBNPButton = new JButton("...");
		locateBNPButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						File f = FileUtils.manualLocate("Locate main BNP Input Directory",true);
						if (f != null) {
							bnpField.setText(f.getAbsolutePath());
						}
					}
				});
			}
		});
		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		pathSettingsPanel.add(locateBNPButton,c);

		spacer = new JPanel();
		c.gridy++;
		pathSettingsPanel.add(spacer,c);

		/* GS Path Setting */
		JLabel gsLabel = new JLabel("<html>&nbsp;Goldman:</html>");
		c.gridx = 0;
		c.gridwidth = 5;
		c.gridy++;
		pathSettingsPanel.add(gsLabel,c);

		final JTextField gsField = new JTextField();
		gsField.setText(setting_gs_input_path);
		gsField.setColumns(20);
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.gridy++;
		pathSettingsPanel.add(gsField,c);

		JButton locateGSButton = new JButton("...");
		locateGSButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						File f = FileUtils.manualLocate("Locate main Goldman Input Directory",true);
						if (f != null) {
							gsField.setText(f.getAbsolutePath());
						}
					}
				});
			}

		});
		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		pathSettingsPanel.add(locateGSButton,c);

		spacer = new JPanel();
		c.gridy++;
		pathSettingsPanel.add(spacer,c);


		JLabel citiInput = new JLabel("<html>&nbsp;CITI:</html>");
		c.gridx = 0;
		c.gridwidth = 3;
		c.gridy++;
		pathSettingsPanel.add(citiInput,c);

		final JTextField citiField = new JTextField();
		citiField.setText(setting_citi_input_path);
		citiField.setColumns(20);
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.gridy++;
		pathSettingsPanel.add(citiField,c);

		JButton locateCitiButton = new JButton("...");
		locateCitiButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						File f = FileUtils.manualLocate("Choose Citi Directory",true);
						if (f != null) {
							citiField.setText(f.getAbsolutePath());
						}
					}
				});
			}
		});
		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		pathSettingsPanel.add(locateCitiButton,c);

		JLabel desc = new JLabel("<html>These fields support &lt;DATE&gt; tags for folder structures organized by date.<br>See the more info section under the 'File settings' tab<html>");
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 4;
		pathSettingsPanel.add(desc,c);
		

		spacer = new JPanel();
		c.gridx = 0;
		c.gridy = 1000;
		c.gridwidth  = GridBagConstraints.REMAINDER;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.weightx = 1000.0;
		c.weighty = 1000.0;
		pathSettingsPanel.add(spacer,c);


		/* 
		 * 
		 * 
		 * 
		 * File settings 
		 * 
		 * 
		 * 
		 */

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.0;
		c.weighty = 1.0;
		c.gridheight = 1;
		c.gridwidth = 1;

		desc = new JLabel("<html><span style=\"color:red;\">If this is your first time looking at this page</span>, "
				+ "its suggested to read the instructions by clicking 'More Info'.</html>");
		c.gridx = 0;
		c.gridwidth = 5;
		c.gridy = 0;
		fileSettingsPanel.add(desc,c);

		JButton moreInfo = new JButton("More Info");
		moreInfo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						createAndShowMoreInfo();
					}
				});
			}
		});
		c.gridx = 5;
		c.gridwidth = 1;
		fileSettingsPanel.add(moreInfo,c);



		JLabel citiCashLabel = new JLabel("<html>&nbsp;CITI Cash</html>");
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridy++;
		fileSettingsPanel.add(citiCashLabel,c);

		final JTextField citiCashField = new JTextField();
		citiCashField.setText(setting_citi_cash);
		citiCashField.setColumns(20);
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.gridy++;
		fileSettingsPanel.add(citiCashField,c);

		JButton locateCitiCashButton = new JButton("auto");
		locateCitiCashButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						File f = FileUtils.manualLocate("Choose Sample CITI cash report");
						if (f != null) {
							citiCashField.setText(
									FileUtils.parseExt(
											Helper.parseToRegExcel(f.getName())
											)
									);
						}
					}
				});
			}
		});
		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		fileSettingsPanel.add(locateCitiCashButton,c);

		/*
		JLabel citiMTMName = new JLabel("<html>&nbsp;CITI MTM</html>");
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridy = 3;
		fileSettingsPanel.add(citiMTMName,c);

		final JTextField citiMTMField = new JTextField();
		citiMTMField.setText(setting_citi_mtm);
		citiMTMField.setColumns(20);
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.gridy = 4;
		fileSettingsPanel.add(citiMTMField,c);

		JButton locateCitiMTMButton = new JButton("auto");
		locateCitiMTMButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File f = FileUtils.manualLocate("Choose Sample GS cash report");
				if (f != null) {
					citiMTMField.setText(f.getAbsolutePath());
				}
			}
		});
		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		fileSettingsPanel.add(locateCitiMTMButton,c);

		spacer = new JPanel();
		c.gridy++;
		fileSettingsPanel.add(spacer,c);
		 */
		JLabel bnpCashLabel = new JLabel("<html>&nbsp;BNP Cash</html>");
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridy++;
		fileSettingsPanel.add(bnpCashLabel,c);

		final JTextField bnpCashField = new JTextField();
		bnpCashField.setText(setting_bnp_cash);
		bnpCashField.setColumns(20);
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.gridy++;
		fileSettingsPanel.add(bnpCashField,c);

		JButton locateBNPCashButton = new JButton("auto");
		locateBNPCashButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						File f = FileUtils.manualLocate("Choose Sample BNP cash report");
						if (f != null) {
							bnpCashField.setText(
									FileUtils.parseExt(
											Helper.parseToRegExcel(f.getName())
											)
									);
						}
					}
				});
			}
		});
		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		fileSettingsPanel.add(locateBNPCashButton,c);

		JLabel bnpMTMLabel = new JLabel("<html>&nbsp;BNP MTM</html>");
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridy++;
		fileSettingsPanel.add(bnpMTMLabel,c);

		final JTextField bnpMTMField = new JTextField();
		bnpMTMField.setText(setting_bnp_mtm);
		bnpMTMField.setColumns(20);
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.gridy++;
		fileSettingsPanel.add(bnpMTMField,c);

		JButton locateBNPMTMButton = new JButton("auto");
		locateBNPMTMButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						File f = FileUtils.manualLocate("Choose Sample BNP MTM report");
						if (f != null) {
							bnpCashField.setText(
									FileUtils.parseExt(
											Helper.parseToRegExcel(f.getName())
											)
									);
						}
					}
				});
			}
		});
		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		fileSettingsPanel.add(locateBNPMTMButton,c);

		JLabel gsCashLabel_NEW = new JLabel("<html>&nbsp;GS Cash NEW</html>");
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridy++;
		fileSettingsPanel.add(gsCashLabel_NEW,c);

		final JTextField gsCashField_NEW = new JTextField();
		gsCashField_NEW.setText(setting_gs_cash_new);
		gsCashField_NEW.setColumns(20);
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.gridy++;
		fileSettingsPanel.add(gsCashField_NEW,c);

		JButton locateGSCash_NEW = new JButton("auto");
		locateGSCash_NEW.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						File f = FileUtils.manualLocate("Choose Sample GS NEW cash report");
						if (f != null) {
							gsCashField_NEW.setText(
									FileUtils.parseExt(
											Helper.parseToRegExcel(f.getName())
											)
									);
						}
					}
				});
			}
		});
		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		fileSettingsPanel.add(locateGSCash_NEW,c);

		JLabel gsCashLabel_NTE = new JLabel("<html>&nbsp;GS Cash NTE</html>");
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridy++;
		fileSettingsPanel.add(gsCashLabel_NTE,c);

		final JTextField gsCashField_NTE = new JTextField();
		gsCashField_NTE.setText(setting_gs_cash_nte);
		gsCashField_NTE.setColumns(20);
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.gridy++;
		fileSettingsPanel.add(gsCashField_NTE,c);

		JButton locateGSCash_NTE = new JButton("auto");
		locateGSCash_NTE.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						File f = FileUtils.manualLocate("Choose Sample GS NTE cash report");
						if (f != null) {
							gsCashField_NTE.setText(
									FileUtils.parseExt(
											Helper.parseToRegExcel(f.getName())
											)
									);
						}
					}
				});
			}
		});
		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		fileSettingsPanel.add(locateGSCash_NTE,c);

		JLabel gsMTMLabel = new JLabel("<html>&nbsp;GS MTM</html>");
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridy++;
		fileSettingsPanel.add(gsMTMLabel,c);

		final JTextField gsMTMField = new JTextField();
		gsMTMField.setText(setting_gs_mtm);
		gsMTMField.setColumns(20);
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.gridy++;
		fileSettingsPanel.add(gsMTMField,c);

		JButton locateGSMTM = new JButton("auto");
		locateGSMTM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						File f = FileUtils.manualLocate("Choose Sample GS MTM report");
						if (f != null) {
							gsMTMField.setText(
									FileUtils.parseExt(
											Helper.parseToRegExcel(f.getName())
											)
									);
						}
					}
				});
			}
		});
		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		fileSettingsPanel.add(locateGSMTM,c);

		spacer = new JPanel();
		c.gridx = 0;
		c.gridy = 1000;
		c.gridwidth  = GridBagConstraints.REMAINDER;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.weightx = 1000.0;
		c.weighty = 1000.0;
		fileSettingsPanel.add(spacer,c);



		final JButton saveButton = new JButton("Apply");
		saveButton.setEnabled(false);
		saveButton.setActionCommand("apply");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						/* Save location */
						setting_save_path         = saveField.getText();
						/* Input location */
						setting_bnp_input_path    = bnpField.getText();
						setting_gs_input_path     = gsField.getText();
						setting_citi_input_path   = citiField.getText();
						setting_advent_input_path = adventField.getText();
						/* Report file names */
						// Cash
						setting_bnp_cash          = bnpCashField.getText();
						setting_gs_cash_new       = gsCashField_NEW.getText();
						setting_gs_cash_nte       = gsCashField_NTE.getText();
						setting_citi_cash         = citiCashField.getText();
						// MTM
						setting_bnp_mtm           = bnpMTMField.getText();
						setting_gs_mtm            = gsMTMField.getText();
						//setting_citi_mtm          = citiMTMField.getText();
						saveSettings();
						saveButton.setEnabled(false);
					}
				});
			}

		});
		buttonPanel.add(saveButton);

		JTextField[] fields = { saveField , adventField, 
				gsField, bnpField, citiField, citiCashField, gsCashField_NEW,
				gsCashField_NTE,bnpCashField,gsMTMField,bnpMTMField};
		for (JTextField field : fields ) {
			field.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					warn();
				}
				public void removeUpdate(DocumentEvent e) {
					warn();
				}
				public void insertUpdate(DocumentEvent e) {
					warn();
				}

				public void warn() {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							saveButton.setEnabled(true); 
						}
					});

				}
			});
		}




		JButton closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int result = JOptionPane.YES_OPTION;
						if (saveButton.isEnabled()) {
							Object[] opts = {"Yes","No"};
							result = JOptionPane.showOptionDialog(settingsFrame, 
						    		"<html><span style='font-size: 120%;'>There are unsaved changes in the settings. Would you like to continue?</span><br>"
						    		+ "Continuing will discard those changes. If you would like to preserve the changes<br>"
						    		+ "made, click 'no' and hit apply before closing.  Otherwise, click 'yes' to continue.</html>",
						    		"Unsaved Changes",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,null,opts,opts[1]);
						}
						if (result == JOptionPane.YES_OPTION) {
							settingsFrame.dispose();
							enableButtons();
							frame.requestFocus();
						}
					}
				});
			}
		});
		buttonPanel.add(closeButton);
		settingsFrame.pack();
		settingsFrame.addWindowFocusListener(new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				saveField.grabFocus();
				saveField.requestFocusInWindow();
			}
		});

		settingsFrame.pack();
		settingsFrame.setVisible(true);

	}
	public void createAndShowMoreInfo() {
		final JDialog info = new JDialog(frame);
		info.setLayout(new BorderLayout());
		info.setMinimumSize(new Dimension(575,500));
		info.setResizable(false);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		final JEditorPane text = new JEditorPane();
		text.setContentType("text/html");
		text.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
		JScrollPane scrollPanel = new JScrollPane(text);
		scrollPanel.setPreferredSize(new Dimension(500,400));
		text.setText("<html>"
				+ "<head>"
				+ "<style>"
				+ "hr {"
				+ "margin: 0px;"
				+ "border: 0px;"
				+ "padding: 0px;"
				+ "}"
				+ "pre {"
				+ "margin-left:10px;"
				+ "margin-right:10px;"
				+ "padding: 5px;"
				+ "background: #CCCCCC;"
				+ "}"
				+ "body {"
				+ "font-family: verdana, arial;"
				+ "}"
				+ "p {"
				+ "font-family: verdana, arial;"
				+ "margin-left: 5px;"
				+ "margin-right: 5px;"
				+ "}"
				+ "</style>"
				+ "</head>"
				+ "<body>"
				+ "<h1>File Name Matching Instructions</h1>"
				+ "<hr>"
				+ "<p>"
				+ "This is an experimental attempt to create a dynamic way to match "
				+ "file names in case file names change from the time "
				+ "this program was made, to avoid users having to modify the code itself."
				+ "</p>"
				+ "<p>"
				+ "A few quick notes and a breif description of how this works:"
				+ "</p>"
				+ "<ul>"
				+ "<li>"
				+ "You are trying to provide for the program what the constant or predictable "
				+ "parts of filenames are."
				+ "</li>"
				+ "<li>"
				+ "A description of what the varying parts of the "
				+ "filename are."
				+ "</li>"
				+ "<li>"
				+ "<span style='color:red;'>"
				+ "There is no need to include the file extension, "
				+ "the program will match valid file types on its own."
				+ "</span>"
				+ "</li>"
				+ "</ul>"
				+ "<p>"
				+ "The 'auto' button next to each field will attempt to generate a pattern on its own from "
				+ "the most recent example file available.  This is not the most reliable method, but can be "
				+ "a useful place to start.  There is a some date recognition involved, but as dates can come "
				+ "in many formats there are often misses which just assume the date is a string of numbers, "
				+ "when using the auto function the generated pattern should be looked over."
				+ "</p>"
				+ "<p>"
				+ "The idea is to fill in the field for the given file with as much \"constant\""
				+ "information as possible, and then to narrow down any parts of "
				+ "the filename to specific types of characters as possible.  "
				+ "This can be done with special 'flags' as I will refer to them in this document. "
				+ "They will look like this '&lt;...&gt;' with the name of the flag in between the "
				+ "left/right angle brackets like so '&lt;FLAG_NAME&gt;'."
				+ "</p>"
				+ "<h2>Flags</h2>"
				+ "<ol>"
				+ "<li>"
				+ "&lt;NUMBER&gt; - will match any length of numbers in a row."
				+ "</li>"
				+ "<li>"
				+ "&lt;DATE:FORMAT&gt; - will look for todays date in the given format."
				+ "<br>See bottom of this information section for details about date formats"
				+ "<br>ex. &lt;DATE:MMDDYY&gt;<br>M - Month<br>D - Day<br>Y-Year"
				+ "</li>"
				+ "<li>"
				+ "&lt;DATE:TODAY:FORMAT&gt; - same as previous except it will look "
				+ "specifically for "
				+ "Today's date (which is also the default)"
				+ " in the given format."
				+ "<br>See bottom of this information section for details about date formats"
				+ "<br>ex. &lt;DATE:TODAY:MMDDYY&gt;<br>M - Month<br>D - Day<br>Y-Year"
				+ "</li>"
				+ "<li>"
				+ "&lt;DATE:YESTERDAY:FORMAT&gt; - same as previous except it will look "
				+ "specifcally for "
				+ "Yesterday's date in the given format."
				+ "<br>See bottom of this information section for details about date formats"
				+ "<br>ex. &lt;DATE:YESTERDAY:MMDDYY&gt;<br>M - Month<br>D - Day<br>Y-Year"
				+ "</li>"
				+ "<li>"
				+ "&lt;RANDOM&gt; - will look for a random or indiscribable portion of the filename."
				+ "</li>"
				+ "</ol>"
				+ "<p>"
				+ "Here are some usage examples, and following that details about the date formats."
				+ "</p>"
				+ "<h2>Usage Examples</h2>"
				+ "<p>"
				+ "When writing file names here I will use '...' as an expression for an "
				+ "omitted porition of the name unless explicitly stated otherwise."
				+ "</p>"
				+ "<p>"
				+ "Say you wish to import a file everyday and the file that is called:"
				+ "</p>"
				+ "<pre>file_to_import_01252014.xls</pre>"
				+ "<p>"
				+ "First off we can ignore the file extension '.xls' as the program will "
				+ "handle file extensions on its own.  "
				+ "You can start with the words as those will generally be constant"
				+ "</p>"
				+ "<pre>"
				+ "file_to_import_"
				+ "</pre>"
				+ "<p>"
				+ "Next we should look at the trailing numbers.  Often times these numbers can "
				+ "be part of an account number, "
				+ "a date, or something else that may be unknown.<br>If the numbers are seemingly "
				+ "random numbers:"
				+ "</p>"
				+ "<pre>"
				+ "file_to_import_&lt;NUMBER&gt;"
				+ "</pre>"
				+ "<p>"
				+ "will be sufficient, as the &lt;NUMBER&gt; flag will match any sequence of "
				+ "numbers.<br>"
				+ "Say those numbers are a date and today is January 25th, 2014, if you know "
				+ "that the date of the file is today and formatted as MMDDYYYY:"
				+ "</p>"
				+ "<pre>"
				+ "file_to_import_&lt;DATE:MMDDYYYY&gt;"
				+ "</pre>"
				+ "<p>"
				+ "or more explicitly"
				+ "</p>"
				+ "<pre>"
				+ "file_to_import_&lt;DATE:TODAY:MMDDYYYY&gt;"
				+ "</pre>"
				+ "<p>"
				+ "Both the last two statements will match the filename.  "
				+ "Lets see what overall changes we made: "
				+ "</p>"
				+ "<pre>"
				+ "file_to_import_<span style='color: green;'>01252014</span><span style='color: red;'>.xls</span><br>"
				+ "file_to_import_<span style='color: green;'>&lt;DATE:TODAY:MMDDYYYY&gt;</span>"
				+ "</pre>"
				+ "<p>"
				+ "Notice how everything in the file name matches up with something in the pattern.  It is important "
				+ "not to leave anything out or it could lead the computer in the wrong direction.<br>"
				+ "Say today is instead January 26th, 2014 and the numbers in the filename are "
				+ "yesterdays date:"
				+ "</p>"
				+ "<pre>"
				+ "file_to_import_&lt;DATE:YESTERDAY:MMDDYYYY&gt;"
				+ "</pre>"
				+ "<p>"
				+ "will match."
				+ "</p>"
				+ "<p>"
				+ "Lets start a new Example, say we have a different report that comes in as:"
				+ "</p>"
				+ "<pre>"
				+ "file_4023331412_to_import_20140125103519.csv"
				+ "</pre>"
				+ "<p>"
				+ "Seems trickier. Again we can get rid of the extension '.csv', "
				+ "as the program will take care of the file extensions, and start with the words"
				+ "<p>"
				+ "<pre>"
				+ "file_..._to_import_..."
				+ "</pre>"
				+ "<p>"
				+ "Next those first numbers do not look like a date.  If they are an account "
				+ "number it may be ok to write them directly into the name, "
				+ "but if they change from time to time it is safer to use a &lt;NUMBER&gt; tag"
				+ "</p>"
				+ "<pre>"
				+ "file_&lt;NUMBER&gt;_to_import_..."
				+ "</pre>"
				+ "<p>"
				+ "Now we can move on to the numbers on the end of the filename.  If these are another "
				+ "constant part like an account number, user id, etc. then we can place a &lt;NUMBER&gt; "
				+ "tag there and be done with it."
				+ "</p>"
				+ "<pre>"
				+ "file_&lt;NUMBER&gt;_to_import_&lt;NUMBER&gt;"
				+ "</pre>"
				+ "<p>"
				+ "But say they are a date and they all get pulled into the same folder.  "
				+ "Now it seems more important that the correct file be accessed.  If again we "
				+ "take today to be January 25th, 2014, we see that the first numbers could at "
				+ "least be the date in the format YYYYMMDD with some trailing numbers."
				+ "</p>"
				+ "<pre>"
				+ "file_&lt;NUMBER&gt;_to_import_&lt;DATE:YYYYMMDD&gt;..."
				+ "</pre>"
				+ "<p>"
				+ "There are still a few trailing numbers here '103519', these may be the hour of "
				+ "day, minute, and second of the day that the report was downloaded (10:35 AM and 19 seconds) "
				+ "or some other superfluous information, so we can replace these with a "
				+ "&lt;NUMBER&gt; tag"
				+ "</p>"
				+ "<pre>"
				+ "file_&lt;NUMBER&gt;_to_import_&lt;DATE:YYYYMMDD&gt;&lt;NUMBER&gt;"
				+ "</pre>"
				+ "<p>"
				+ "And if the date were yesterday's date"
				+ "</p>"
				+ "<pre>"
				+ "file_&lt;NUMBER&gt;_to_import_&lt;DATE:YESTERDAY:YYYYMMDD&gt;&lt;NUMBER&gt;"
				+ "</pre>"
				+ "Our overall changes here were (for the assumption that the date was today's date.)"
				+ "</p>"
				+ "<pre>"
				+ "file_<span style='color: green;'>4023331412</span>_to_import_<span style='color: #0000AA;'>20140125</span>"
				+ "<span style='color: #AA8000'>103519</span><span style='color: red;'>.csv</span><br>"
				+ "file_<span style='color: green;'>&lt;NUMBER&gt;</span>_to_import_<span style='color: #0000AA;'>&lt;DATE:YYYYMMDD&gt;</span>"
				+ "<span style='color: #AA8000'>&lt;NUMBER&gt;</span>"
				+ "</pre>"
				+ "<p>"
				+ "Again make note of the fact that every part of the original file name is accounted for apart from the file extension.<br>"
				+ "Lastly we will have an example of a file which has a "
				+ "portion of randomly generated information"
				+ "</p>"
				+ "<pre>"
				+ "file_to_import_a1231f39a3e6f89e13.xlsx"
				+ "</pre>"
				+ "<p>"
				+ "This file has an awful bit (a1231f39a3e6f89e13) attached to the end of it.  This might change "
				+ "everyday so the best choice for this would be a &lt;RANDOM&gt; tag and again removing the '.xls' "
				+ "file extension."
				+ "</p>"
				+ "<pre>"
				+ "file_to_import_&lt;RANDOM&gt;"
				+ "</pre>"
				+ "<p>"
				+ "This would probably have to be left in this state.  Now for a simple practical example, say you have a report coming "
				+ "in each day.  The report comes in marked with a timestamp of when you downloaded it, similar to the one above.  Your "
				+ "strategy then should be to use the &lt;DATE:TODAY:FORMAT&gt; tag.  Generally you will have a couple options, but the "
				+ "tags listed here should generally be enough to get the job done.  If the file comes in everyday with the exact same "
				+ "name, then maybe the strategy should be to store the reports in folders by their date, and include a <DATE> tag in "
				+ "the folder setting for that report."
				+ "</p>"
				+ "<h2>Date Formats</h2>"
				+ "<pre>"
				+ "Year:\n"
				+ "The two digit year\n"
				+ "YY -> 01, 02, 99, 87, 91, 13\n"
				+ "Complete 4 digit year\n"
				+ "YYYY -> 01, 02, 99, 87, 91, 13\n\n"
				+ "Month:\n"
				+ "M -> 1, 5, 11, 10, 12\n"
				+ "Double digit month\n"
				+ "MM -> 01, 05, 11, 10, 12\n"
				+ "Abbreviated month name\n"
				+ "MMM -> Jan, May, Nov, Oct, Dec\n"
				+ "Full month name\n"
				+ "MMMM -> January, May, November, October, December\n\n"
				+ "Day:\n"
				+ "Single/double digit day\n"
				+ "D -> 1, 2, 12, 24, 31\n"
				+ "Double digit day\n"
				+ "DD -> 01, 02, 12, 24, 31"
				+ "</pre>"
				+ "<h2>A Closing Note</h2>"
				+ "<p>"
				+ "This tag system is built upon a search language called Regular Expressions (Regex).  If you know anything about "
				+ "them then all the original search patterns are still supported and can be used in these fields.  "
				+ "If you would like a crash course in Regular Expressions refer to <a href='http://www.regular-expressions.info'>this site</a>.  "
				+ "This is just one of many resources available on the subject, and a quick internet search will certainly reveal as much."
				+ "</p>"
				+ "</body>"
				+ "</html>");
		text.setEditable(false);
		text.setCaretPosition(0);
		text.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if(Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(e.getURL().toURI());
						} catch (IOException e1) {
							if (debug) e1.printStackTrace();
						} catch (URISyntaxException e1) {
							if (debug) e1.printStackTrace();
						}
					}
				}
			}
		});

		JButton closeInstructions = new JButton("Close");
		closeInstructions.setActionCommand("close");
		closeInstructions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						enableButtons();
						info.dispose();
						frame.requestFocus();
					}
				});
			}
		});


		panel.add(closeInstructions,BorderLayout.SOUTH);
		panel.add(scrollPanel,BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
		info.add(panel);

		info.pack();
		info.addWindowFocusListener(new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				text.grabFocus();
				text.requestFocusInWindow();
			}
		});
		info.addWindowListener(new WindowAdapter()  {
			public void windowClosing(WindowEvent e) {
				info.dispose();
				enableButtons();
				frame.requestFocus();
			}
		});
		
		info.setVisible(true);
	}



	/* Quick GUI method shortcuts */
	public void enableButtons() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				settingsButton.setEnabled(true);
				instructionButton.setEnabled(true);
				startButton.setEnabled(true);
				closeButton.setEnabled(true);
			}
		});

	}
	public void disableButtons() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				settingsButton.setEnabled(false);
				instructionButton.setEnabled(false);
				startButton.setEnabled(false);
				closeButton.setEnabled(false);
			}
		});
	}
	public void scrollToBottom() {
		JScrollBar jsb = outputScrollPane.getVerticalScrollBar();
		jsb.setValue(jsb.getMaximum());
	}
	
	// Task to execute for GUI
	class ThreadedTask extends SwingWorker<Void, Void> {
		boolean closeOnCompletion = false;
		JDialog parent = null;
		String content = "";
		String header = 
				"<head>"
						+ "<style>"
						+ "body {"
						+ "color: black;"
						+ "font-family: verdana, arial;"
						+ "font-size: 18pt;"
						+ "}"
						+ ".error {"
						+ "color:#FF0000;"
						+ "}"
						+ ".warning {"
						+ "color:#FF8800;"
						+ "}"
						+ ""
						+ "</style>"
						+ "</head>";
		@Override
		public Void doInBackground() {
			if (mainMethod()) {
				closeOnCompletion = true;
			} else {
				closeOnCompletion = false;
			}
			return null;
		}

		@Override 
		public void done() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run(){
					Toolkit.getDefaultToolkit().beep();
					enableButtons();
					setCursor(null);
					if (closeOnCompletion && parent != null) {
						parent.dispose();
					}
					return;
				}
			});
			
		}
		public void setPubProgress(int p) {
			setProgress(p);
			return;
		}
		public void setParent(JDialog parent, boolean closeOnCompletion) {
			this.parent = parent;
			this.closeOnCompletion = closeOnCompletion;
		}
		private void append(final JEditorPane outputField, final String content) { 
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					outputField.setText("<html>" + header + "<body>" + content + "</body></html>");
					scrollToBottom();
				}
			});
			
		}
		public void print(Object o) {
			content += o.toString();
			append(taskOutput,content);
			return;
		}
		public void println(Object o) {
			content += o.toString() + "<br>";
			append(taskOutput,content);
			return;
		}

		public void printError(Exception e,Object o) {
			content += "<p class='error'>";
			content += "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * "
					+ "* * * * * * * *";
			content += "<br><br>";
			content += "ERROR:<br>&nbsp;&nbsp;&nbsp;&nbsp;";
			content += o.toString() + "<br>&nbsp;&nbsp;&nbsp;&nbsp;";
			content +="Error message:<br>&nbsp;&nbsp;&nbsp;&nbsp;" + e.toString() + "<br>";
			if (debug) {
				content += "Stack Trace:<br>";
				int i;
				StackTraceElement[] stack = e.getStackTrace();
				for (i = 0; i < stack.length; i++) {
					content += stack[i].toString() + "<br>";
				}
			}
			content +="<br>";
			content += "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * "
					+ "* * * * * * * *";
			content += "</p><br>";
			append(taskOutput,content);
			return;
		}

		public void printWarning(Object o) {
			content += "<br><p class='warning'> Warning:";
			content += o.toString();
			content += "</p><br><br>";
			append(taskOutput,content);
			return;
		}
	}
	public static void createAndShowGUI() {
		//Create and set up the window.
		frame = new JFrame("Swap Unwind");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		JComponent newContentPane = new ReconcileGUI(frame);
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);

		//Display the window.
		frame.pack();
		if (!debug) frame.setResizable(false);
		frame.setVisible(true);
		return;
	}
	public void createAndShowGUI(Task parent) {
		//Create and set up the window.
		frame = new JFrame("Swap Unwind");

		//Create and set up the content pane.
		JComponent newContentPane = new ReconcileGUI(frame,parent);
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		//Display the window.
		frame.pack();
		if (!debug) frame.setResizable(false);
		frame.setVisible(true);
		return;
	}

	public void refresh() {
		this.revalidate();
		frame.repaint();
	}
	/* END GUI METHODS */



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 
	 *  Task: Import report files into Reconcile files
	 *  
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 
	 *  TO DO:
	 * 		[+] Edit method to select latest file
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	/* BEGIN PROCESSING METHODS */

	/* Constants */ 
	private static String pwd = FileUtils.getPWD();

	/* Excel Processing Methods */
	private Workbook getLatestWorkbook() {
		// Assert that the output directory exists
		// Create a better archive
		String recDirPath = parseSettingToRegex(setting_save_path,false);
		File recDir = new File(recDirPath), latestWorkbook = null;
		ArrayList<File> matches = new ArrayList<File>();
		if (!recDir.exists()) {
			recDir.mkdirs();
			latestWorkbook = FileUtils.manualLocate("Open latest Newtyn Reconcile Report");
		} else {
			File[] files = recDir.listFiles();
			if (files.length == 0) {
				latestWorkbook = FileUtils.manualLocate("Open latest Newtyn Reconcile report");
			}
			for (File file: files) {
				if (file.getName().contains("Newtyn Reconcile")) {
					matches.add(file);
				}
			}
		}
		String date, cur_max = "";
		for (File match : matches) {
			date = Helper.parseDate(match.getName());
			if (cur_max.length() == 0) {
				cur_max = date;
				latestWorkbook = match;
			} else {
				if (Helper.compareDates(date, cur_max, "mm-dd-yy") > 0) {
					cur_max = date;
					latestWorkbook = match;
				}
			}
		}
		if (latestWorkbook != null) {
			try {
				return ExcelUtils.openWorkbook(latestWorkbook);
			} catch (Exception e) {
				if (debug) e.printStackTrace();
				else task.printError(e, "Error opening workbook, file may be damaged.");
			}
		}
		return null;
	}
	private static String createSaveFileName() {
		String nameTemplate = "Newtyn Reconcile <DATE>", savename = "";
		savename = nameTemplate.replace("<DATE>", Helper.formatYesterday("MM-dd-yy"));
		return savename + ".xls";
	}
	private String parseSettingToRegex(String setting,boolean addExt) {
		println("SETTING: " + setting);
		String regex = Helper.parseToRegex(setting) + (addExt ? "\\.(csv|tab|txt|xlsx?)" : "");
		println("REGEX:   " + regex);
		return regex;
	}
	/* Main Method for Task method */
	private boolean mainMethod() {
		Workbook wb, tb; Sheet ws, ts;
		int importCount = 0, attemptedImports = 0;
		int canary = 0;
		String regex;
		try{
			task.print((debug ? "DEBUG MODE IS ON<br>" : ""));
			task.println("Beginning Import...<br>");
			task.print("Opening Workbook...");
			wb = getLatestWorkbook();
			if (wb == null) {
				task.println("Unable to open Workbook.");
				task.printWarning("Unable to open Workbook.<br>Please check the directory and locate the correct file.");
				return false;
			}
			task.println("Workbook Opened.<br>");
			String adventDirPath = parseSettingToRegex(setting_advent_input_path,false);
			File adventDir = new File(adventDirPath), temp;
			if (!adventDir.exists()) {
				adventDir = FileUtils.manualLocate(adventDirPath,"Locate today's AdventOut directory",true);
				if (adventDir != null) {
					adventDirPath = adventDir.getAbsolutePath();
				}
			}


			try {
				/* Import CASHNEW */
				try {
					attemptedImports++;
					temp = FileUtils.locateAndOpenFile("cashnew",adventDirPath,true,"Locate correct 'cashnew' file");
					if (temp != null) {
						task.println("Located " + temp.getName());
						tb = ExcelUtils.openWorkbook(temp);
						ts = tb.getSheetAt(0);
						ws = wb.getSheet("UG-NEW");
						try {
							task.print("Beginning Import...");
							if (ExcelUtils.copySheetSection(ts,-1,-1,0,7,ws,true) == null) {
								task.println("failed");
								task.printWarning("Could not open '" + temp.getName() + "', continuing...");
							}
							importCount++;
							task.println("complete.");
						} catch (Exception e) {
							if (debug) e.printStackTrace();
							else task.printError(e, "Error importing '"  + temp.getName() + "', continuing...");
						}
						task.println("");
					} else {
						task.printWarning("Could not locate cashnew, skipping import and continuing...");
					}
				} catch (Exception e) {
					if (debug) e.printStackTrace();
					else task.printError(e, "Error reached while importing Advent NEW Cash report.");
				}


				/* Import CASHNTE */
				try {
					attemptedImports++;
					temp = FileUtils.locateAndOpenFile("cashnte",adventDirPath,true,"Locate correct 'cashnte' file");
					if (temp != null) {
						task.println("Located " + temp.getName());
						tb = ExcelUtils.openWorkbook(temp);
						ts = tb.getSheetAt(0);
						ws = wb.getSheet("UG-NTE");
						try {
							task.print("Beginning Import...");
							if (ExcelUtils.copySheetSection(ts,-1,-1,0,7,ws,true) == null) {
								task.println("failed");
								task.printWarning("Could not open '" + temp.getName() + "', continuing...");
							}
							importCount++;
							task.println("complete.");
						} catch (Exception e) {
							if (debug) e.printStackTrace();
							else task.printError(e, "Error importing '"  + temp.getName() + "', continuing...");
						}
						task.println("");
					} else {
						task.printWarning("Could not locate cashnte, skipping import and continuing...");
					}
				} catch (Exception e) {
					if (debug) e.printStackTrace();
					else task.printError(e, "Error reached while importing Advent NTE Cash report.");
				}


				/* Import and Edit UGSPX */
				try {
					attemptedImports++;
					temp = FileUtils.locateAndOpenFile("ugspx",adventDirPath,true,"Locate correct 'ugspx' file");
					if (temp != null) {
						task.println("Located " + temp.getName());
						tb = ExcelUtils.openWorkbook(temp);
						ts = tb.getSheetAt(0);
						ws = wb.getSheet("NEWSPX");
						try {
							task.print("Beginning Import...");
							if (ExcelUtils.copySheetSection(ts,-1,-1,-1,-1,ws,true) == null) {
								task.println("failed");
								task.printWarning("Could not open '" + temp.getName() + "', continuing...");
							}
							importCount++;
							task.println("complete.");
							task.print("Adding currency flags to NEWSPX Sheet...");
							ArrayList<Cell> matchedCells = ExcelUtils.searchSheetAll(ws,"ADDED TO GRAND TOTAL",true);
							if (matchedCells.size() > 0) {
								int lastIndex = 0; Cell c; Row r; String euroExtString = "";
								for (Cell matchedCell : matchedCells) {
									c = ExcelUtils.searchSheetBySection(ws,lastIndex,matchedCell.getRowIndex(),0,0,"(FX =",true);
									r = ExcelUtils.getRow(ws,matchedCell.getRowIndex());

									if (ExcelUtils.checkCellValue(c, true, "Euro", true) ) {
										euroExtString = " EURO";
									} else {
										euroExtString = "";
									}
									c = ExcelUtils.getCell(r,13,ExcelUtils.CELL_CREATE_NULL_AS_BLANK);
									c.setCellType(Cell.CELL_TYPE_STRING);
									c.setCellValue("COMMNEW - OPEN TRADE EQUITY" + euroExtString);

									c = ExcelUtils.getCell(ws,r.getRowNum()+4,13,ExcelUtils.CELL_CREATE_NULL_AS_BLANK);
									c.setCellType(Cell.CELL_TYPE_STRING);
									c.setCellValue("COMMNEW -" + (euroExtString.equals("") ? " GSCMDY" : euroExtString ));

									lastIndex = matchedCell.getRowIndex();
								}
								task.println("complete");
							} else {
								task.println("failed.");
								task.printWarning("There was a problem while adding flags to the NEWSPX sheet.");
							}
						} catch (Exception e) {
							if (debug) e.printStackTrace();
							else task.printError(e, "Error importing '"  + 
									temp.getName() + "', continuing...");
						}
						task.println("");
					} else {
						task.printWarning("Could not locate newspx file 'ugspx', "
								+ "skipping import and continuing...");
					}
				} catch (Exception e) {
					if (debug) e.printStackTrace();
					else task.printError(e, "Error reached while importing Advent ugspx report.");
				}
				String path = setting_gs_input_path;
				println(canary++);
				try {
					/* GS Cash NEW */
					attemptedImports++;
					regex = parseSettingToRegex(setting_gs_cash_new,true);
					path = parseSettingToRegex(setting_gs_input_path,false);
					temp = FileUtils.locateAndOpenFileRegex(regex,path,
							true,"Locate correct NEW Goldman cash file");
					println(canary++);
					if (temp != null) {
						println(canary++);
						task.println("Located " + temp.getName());
						tb = ExcelUtils.openWorkbook(temp);
						ts = tb.getSheetAt(0);
						ws = wb.getSheet("GS Cash NEW");
						try {
							task.print("Beginning Import...");
							if (ExcelUtils.copySheetSection(ts,-1,-1,0,8,ws,true) == null) {
								task.println("failed");
								task.printWarning("Could not open '" 
										+ temp.getName() + "', continuing...");
							}
							importCount++;
							task.println("complete.");
						} catch (Exception e) {
							if (debug) e.printStackTrace();
							else task.printError(e, "Error importing '"  + temp.getName() 
									+ "', continuing...");
						}
						task.println("");
					} else {
						println(canary++);
						task.printWarning("Could not locate Goldman NEW Cash file, skipping import and continuing...");
					}
				} catch (Exception e) {
					if (debug) e.printStackTrace();
					else task.printError(e, "Error reached while importing GS Cash NEW report.");
				}


				/* GS Cash NTE */
				try {
					attemptedImports++;
					regex = parseSettingToRegex(setting_gs_cash_nte,true);
					temp = FileUtils.locateAndOpenFileRegex(regex,path,
							true,"Locate correct NTE Goldman cash file");
					if (temp != null){
						task.println("Located " + temp.getName());
						tb = ExcelUtils.openWorkbook(temp);
						ts = tb.getSheetAt(0);
						ws = wb.getSheet("GS Cash NTE");
						try {
							task.print("Beginning Import...");
							if (ExcelUtils.copySheetSection(ts,-1,-1,0,8,ws,true) == null) {
								task.println("failed");
								task.printWarning("Could not open '" + temp.getName() + "', continuing...");
							}
							importCount++;
							task.println("complete.");
						} catch (Exception e) {
							if (debug) e.printStackTrace();
							else task.printError(e, "Error importing '"  + temp.getName() + "', continuing...");
						}
						task.println("");
					} else {
						task.printWarning("Could not locate Goldman NTE Cash file, skipping import and continuing...");
					}
				} catch (Exception e) {
					if (debug) e.printStackTrace();
					else task.printError(e, "Error reached while importing GS Cash NTE report.");
				}

				/* GS MTM */

				try {
					regex = parseSettingToRegex(setting_gs_mtm,true);
					attemptedImports++;
					temp = FileUtils.locateAndOpenFileRegex(regex,path,true,"Locate correct Goldman MTM file");
					if (temp != null) {
						task.println("Located " + temp.getName());
						tb = ExcelUtils.openWorkbook(temp);
						ts = tb.getSheetAt(0);
						ws = wb.getSheet("GS MTM");
						try {
							task.print("Beginning Import...");
							if (ExcelUtils.copySheetSection(ts,-1,-1,0,8,ws,true) == null) {
								task.println("failed");
								task.printWarning("Could not open '" + temp.getName() + "', continuing...");
							}
							importCount++;
							task.println("complete.");
						} catch (Exception e) {
							if (debug) e.printStackTrace();
							else task.printError(e, "Error importing '"  + temp.getName() + "', continuing...");
						}
						task.println("");
					} else {
						task.printWarning("Could not locate Goldman MTM file, skipping import and continuing...");
					}
				} catch (Exception e) {
					if (debug) e.printStackTrace();
					else task.printError(e, "Error reached while importing GS MTM report.");
				}

				/* BNP Cash */
				try {
					attemptedImports++;
					regex = parseSettingToRegex(setting_bnp_cash,true);
					path = parseSettingToRegex(setting_bnp_input_path,false);
					temp = FileUtils.locateAndOpenFileRegex(regex,path,true,"Locate correct BNP Cash file");
					if (temp != null) {
						task.println("Located " + temp.getName());
						tb = ExcelUtils.openWorkbook(temp);
						ts = tb.getSheetAt(0);
						ws = wb.getSheet("BNP Cash");
						try {
							task.print("Beginning Import...");
							if (ExcelUtils.copySheetSection(ts,-1,-1,0,ExcelUtils.convertColToInt("N"),ws,true) == null) {
								task.println("failed");
								task.printWarning("Could not open '" + temp.getName() + "', continuing...");
							}
							importCount++;
							task.println("complete.");
						} catch (Exception e) {
							if (debug) e.printStackTrace();
							else task.printError(e, "Error importing '"  + temp.getName() + "', continuing...");
						}
						task.println("");
					} else {
						task.printWarning("Could not locate BNP Cash file, skipping import and continuing...");
					}
				} catch (Exception e) {
					if (debug) e.printStackTrace();
					else task.printError(e, "Error reached while importing BNP Cash report.");
				}

				/* BNP MTM */
				try {
					attemptedImports++;
					regex = parseSettingToRegex(setting_bnp_mtm,true);
					path = parseSettingToRegex(setting_bnp_input_path,false);
					temp = FileUtils.locateAndOpenFileRegex(regex,path,true,"Locate correct BNP MTM file");
					System.out.println(canary++);
					if (temp != null) {
						task.println("Located " + temp.getName());
						tb = ExcelUtils.openWorkbook(temp);
						ts = tb.getSheetAt(0);
						ws = wb.getSheet("BNP MTM");
						try {
							task.print("Beginning Import...");

							if (ExcelUtils.copySheetSection(ts,-1,-1,0,ExcelUtils.convertColToInt("N"),ws,true) == null) {
								task.println("failed");
								task.printWarning("Could not open '" + temp.getName() + "', continuing...");
							}
							importCount++;
							task.println("complete.");
						} catch (Exception e) {
							if (debug) e.printStackTrace();
							else task.printError(e, "Error importing '"  + temp.getName() + "', continuing...");
						}
						task.println("");
					} else {
						task.printWarning("Could not locate BNP MTM file, skipping import and continuing...");
					}
				} catch (Exception e) {
					if (debug) e.printStackTrace();
					else task.printError(e, "Error reached while importing BNP MTM report.");
				}


				/* CITI Cash */
				try {
					regex = parseSettingToRegex(setting_citi_cash,true);
					path = parseSettingToRegex(setting_citi_input_path,false);
					temp = FileUtils.locateAndOpenFileRegex(regex,path,true,"Locate correct CITI Cash file");
					if (temp != null) {
						task.println("Located " + temp.getName());
						tb = ExcelUtils.openWorkbook(temp);
						ts = tb.getSheetAt(0);
						ws = wb.getSheet("Citi Cash");
						try {
							task.print("Beginning Import...");
							attemptedImports++;
							if (ExcelUtils.copySheetSection(ts,-1,-1,0,ExcelUtils.convertColToInt("N"),ws,true) == null) {
								task.println("failed");
								task.printWarning("Could not open '" + temp.getName() + "', continuing...");
							}
							importCount++;
							task.println("complete.");
						} catch (Exception e) {
							if (debug) e.printStackTrace();
							else task.printError(e, "Error importing '"  + temp.getName() + "', continuing...");
						}
						task.println("");
					} else {
						task.printWarning("Could not locate CITI Cash file, skipping import and continuing...");
					}
				} catch (Exception e) {
					if (debug) e.printStackTrace();
					else task.printError(e, "Error reached while importing Citi Cash report.");
				}

				// END OF IMPORTS
			} catch (Exception e) {
				if (debug) e.printStackTrace();
				else task.printError(e, "Error reached while compiling reports.");
			}


			if (importCount < attemptedImports) {
				task.printWarning("Only " + importCount + "/" + 
						attemptedImports + " imports completed.");
			} else {
				task.println("All imports completed successfully.<br>");
			}

			task.print("Saving Workbook...");
			String savename = createSaveFileName();
			try {
				if (!ExcelUtils.saveWorkbook(wb, savename, parseSettingToRegex(setting_save_path,false), true, true)) {
					throw new ExcelException();
				}
				task.println("complete.<br>Workbook saved to:<br>"+
						FileUtils.joinPath(FileUtils.shortenPath(setting_save_path),savename)+"<br>");
			} catch (Exception e) {
				if (debug) e.printStackTrace();
				else task.printError(e, "Error reached while compiling reports.");
			}
			return true;
		} catch (Exception e) {
			if (debug) e.printStackTrace();
			else task.printError(e, "Error reached while compiling reports.");
			return false;
		} finally {
			task.println("<br>Program has reached completion.<br>");
			scrollToBottom();
		}
	}

	/* Inherited class methods to allow Task Management */
	@Override
	public void launch(final Task parentTask) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// pass
		}
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(parentTask);
			}
		});
	}
	@Override
	public void close() {	
		if (parent != null) {
			parent.signalClose();
		}
		frame.dispose();
	}
	@Override
	public String getFunctionName() {
		return "Newtyn Reconcile";
	}

	/* Main method */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// pass
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	} // main
}
