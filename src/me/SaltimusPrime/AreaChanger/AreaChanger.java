package me.SaltimusPrime.AreaChanger;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.PlainDocument;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.Toolkit;

/**
 * Simple GUI for changing the active area of a wacom tablet. Works on both Windows
 * and Mac OSX.
 * 
 * @author SaltimusPrime
 *
 */
public class AreaChanger extends JFrame {

	// Backup loading and saving is slightly different on OSX so this variable
	// is used to check if we're on a mac.
	public static boolean isMac = false;

	private static final long serialVersionUID = 4015592421303880390L;

	// Declare all components of the GUI.
	private JPanel contentPane;

	private JTextField tfTop;
	private JTextField tfLeft;
	private JTextField tfBottom;
	private JTextField tfRight;
	
	private IntInputFilter filterMaxX;
	private IntInputFilter filterMaxY;

	private JLabel lblEnterCordinates;
	private JLabel lblTop;
	private JLabel lblLeft;
	private JLabel lblBottom;
	private JLabel lblRight;

	private JButton btnLoadBackup;
	private JButton btnApply;

	// The wacom backup currently loaded.
	private WacomBackup backup;
	
	// Store the file extension, this way we can allow users to try if the program works on different wacom tablets.
	private String fileExt;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// Check if the program is running on OSX.
		isMac = System.getProperty("os.name").startsWith("Mac");

		// Set the look and feel to the default one for the OS.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Launch the GUI.
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AreaChanger frame = new AreaChanger();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public AreaChanger() {
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(AreaChanger.class.getResource("/me/SaltimusPrime/AreaChanger/Images/icon.png")));
		setTitle("Wacom CTL Area Changer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 420, 300);
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		lblEnterCordinates = new JLabel("Enter Coordinates (in Counts)");
		lblEnterCordinates.setBounds(10, 11, 233, 14);
		contentPane.add(lblEnterCordinates);

		lblTop = new JLabel("Top");
		lblTop.setBounds(66, 68, 46, 14);
		contentPane.add(lblTop);

		lblLeft = new JLabel("Left");
		lblLeft.setBounds(66, 129, 46, 14);
		contentPane.add(lblLeft);

		lblBottom = new JLabel("Bottom");
		lblBottom.setBounds(231, 68, 46, 14);
		contentPane.add(lblBottom);

		lblRight = new JLabel("Right");
		lblRight.setBounds(231, 129, 46, 14);
		contentPane.add(lblRight);

		tfTop = new JTextField();
		tfTop.setColumns(10);
		tfTop.setBounds(96, 65, 86, 20);
		tfTop.setEditable(false);
		filterMaxY = new IntInputFilter(0); // Set max value for filter to 0 for now since we don't know the tablet area yet.
		((PlainDocument) tfTop.getDocument()).setDocumentFilter(filterMaxY);
		contentPane.add(tfTop);

		tfLeft = new JTextField();
		tfLeft.setColumns(10);
		tfLeft.setBounds(96, 126, 86, 20);
		tfLeft.setEditable(false);
		filterMaxX = new IntInputFilter(0); // Set max value for filter to 0 for now since we don't know the tablet area yet.
		((PlainDocument) tfLeft.getDocument()).setDocumentFilter(filterMaxX);
		contentPane.add(tfLeft);

		tfBottom = new JTextField();
		tfBottom.setEditable(false);
		tfBottom.setColumns(10);
		tfBottom.setBounds(277, 65, 86, 20);
		((PlainDocument) tfBottom.getDocument()).setDocumentFilter(filterMaxY);
		contentPane.add(tfBottom);

		tfRight = new JTextField();
		tfRight.setEditable(false);
		tfRight.setColumns(10);
		tfRight.setBounds(277, 126, 86, 20);
		((PlainDocument) tfRight.getDocument()).setDocumentFilter(filterMaxX);
		contentPane.add(tfRight);

		btnLoadBackup = new JButton("Load backup");
		btnLoadBackup.setBounds(66, 197, 116, 23);
		btnLoadBackup.addActionListener(makeLoadButtonActionListener());
		contentPane.add(btnLoadBackup);

		btnApply = new JButton("Apply");
		btnApply.setEnabled(false);
		btnApply.setBounds(231, 197, 116, 23);
		btnApply.addActionListener(makeApplyButtonActionListener());
		contentPane.add(btnApply);
	}

	/**
	 * @return The action listener used to allow the user to select a backup
	 *         file.
	 */
	private ActionListener makeLoadButtonActionListener() {
		ActionListener btAct = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser selector = new JFileChooser();
				//selector.setAcceptAllFileFilterUsed(false);
				FileFilter filter = new FileNameExtensionFilter("Wacom Backup", "tabletprefs", "wacomprefs");
				selector.setFileFilter(filter);
				selector.setDialogTitle("Select a Wacom tablet backup");
				int result = selector.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					File f = selector.getSelectedFile();
					try {
						// Backups are loaded differently depending on OS, since
						// the OSX version extends the windows one, We don't
						// need to make any further distinctions based on OS
						// here.
						fileExt = getFileExtension(f);
						if (isMac) {
							backup = new WacomBackupOSX(f);
						} else {
							backup = new WacomBackup(f);
						}

						// Read the active area values from the loaded backup
						// and display them. From this point onward the user can
						// edit the values.
						filterMaxX.setMaxValue(backup.getMaxX());
						filterMaxY.setMaxValue(backup.getMaxY());
						tfTop.setText(Integer.toString(backup.getTopValue()));
						tfTop.setEditable(true);
						tfLeft.setText(Integer.toString(backup.getLeftValue()));
						tfLeft.setEditable(true);
						tfBottom.setText(Integer.toString(backup.getBottomValue()));
						tfBottom.setEditable(true);
						tfRight.setText(Integer.toString(backup.getRightValue()));
						tfRight.setEditable(true);
						btnApply.setEnabled(true);
					} catch (Exception exc) {
						JOptionPane.showMessageDialog(null, "Could not load selected backup file.",
								"Failed to load backup", JOptionPane.ERROR_MESSAGE);
						exc.printStackTrace();
					}
				}

			}

		};

		return btAct;
	}

	/**
	 * @return Action listener used when the user hits apply. Handles saving the
	 *         new backup and opens it so the backup utility can apply it.
	 */
	private ActionListener makeApplyButtonActionListener() {
		ActionListener btAct = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// This should never happen since the button is disabled before
				// loading a backup but let's check anyway.
				if (backup == null) {
					JOptionPane.showMessageDialog(null, "No backup loaded.", "Could not apply changes",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				try {
					int top = Integer.parseInt(tfTop.getText());
					int bottom = Integer.parseInt(tfBottom.getText());
					int left = Integer.parseInt(tfLeft.getText());
					int right = Integer.parseInt(tfRight.getText());

					// Cancel and show an error message if the data entered is invalid.
					if (bottom <= top) {
						JOptionPane.showMessageDialog(null, "Bottom value must be higher than top value.",
								"Could not apply changes", JOptionPane.ERROR_MESSAGE);
						return;
					} else if (right <= left) {
						JOptionPane.showMessageDialog(null, "Right value must be higher than left value.",
								"Could not apply changes", JOptionPane.ERROR_MESSAGE);
						return;
					}

					// Apply the new values to the loaded backup.
					backup.setTopValue(top);
					backup.setBottomValue(bottom);
					backup.setLeftValue(left);
					backup.setRightValue(right);
				} catch (NumberFormatException nE) {
					// This is called if the user left one of the fields empty.
					JOptionPane.showMessageDialog(null, "Values could not be set, make sure all fields are filled out.",
							"Could not apply changes", JOptionPane.ERROR_MESSAGE);
					return;
				}
				URL url;
				File mbFile = null;
				try {
					url = new URL("file:" + ClassLoader.getSystemClassLoader().getResource(".").getPath()
							+ "modBackup." + fileExt);
					mbFile = new File(url.toURI().getSchemeSpecificPart());
					
					// Save the modified backup to modBackup.tabletprefs in the same folder the application was launched from.
					if (!backup.save(mbFile))
					{
						JOptionPane.showMessageDialog(null, "Could not save backup.", "Could not apply changes",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Could not save backup.", "Could not apply changes",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();

					return;
				}

				// Open the modified backup.
				try {
					Desktop.getDesktop().open(mbFile);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Could not open modified backup.", "Could not apply changes",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}

			}

		};

		return btAct;
	}
	
	/**
	 * Gets the extension of a file.
	 * Since the backup folder on mac also has a . in it, it should also sorta work on the backups there.
	 * @param file
	 * @return
	 */
	private String getFileExtension(File file) {
	    String name = file.getName();
	    try {
	        return name.substring(name.lastIndexOf(".") + 1);
	    } catch (Exception e) {
	        return "";
	    }
	}
}
