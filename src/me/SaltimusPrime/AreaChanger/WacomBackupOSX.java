package me.SaltimusPrime.AreaChanger;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class for editing backups created in OSX.
 * 
 * @author SaltimusPrime
 *
 */
public class WacomBackupOSX extends WacomBackup {
	/**
	 * Gets the file inside the backup folder that contains the actual backup data.
	 * @param directory	The directory the backup is contained in.
	 * @param type	The backup type (pen or touch)
	 * @return	The pentablet backup file
	 * @throws InvalidBackupException	Thrown if the folder doesn't contain a pentablet or touch backup file.
	 */
	
	private static File getPrefsFile(File directory, String type) throws InvalidBackupException
	{
		if (!directory.isDirectory())
		{
			throw new InvalidBackupException();
		}
		
		File[] dirFiles = directory.listFiles();
		for (File f : dirFiles)
		{
			String filename = f.getName().toLowerCase();
			if ((type.contains("pentablet") && filename.contains("pentablet")) || (type.contains("touch") && filename.contains("touch")))
				return f;
		}
		throw new InvalidBackupException();
	}

	private Document touchPrefs;
	private Document version;
	
	private String backupName;
	private String touchName;

	/**
	 * Loads an OSX backup for the tablet.
	 * 
	 * @param bFile
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws InvalidBackupException 
	 */
	public WacomBackupOSX(File bFile)
			throws ParserConfigurationException, SAXException, IOException, TransformerException, InvalidBackupException {
		// The selected file is a folder that contains the actual backup, we
		// pass the file containing the tablet area data to the parent class for
		// processing.
		super(getPrefsFile(bFile, "pentablet"));
		backupName = getPrefsFile(bFile, "pentablet").getName();

		// The other files in the folder we simply store so we can create an edited backup later.
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		File touchPrefsFile = getPrefsFile(bFile, "touch");
		touchPrefs = dBuilder.parse(touchPrefsFile/*new File(bFile, "com.wacom.touch.prefs")*/);
		touchName = touchPrefsFile.getName();
		version = dBuilder.parse(new File(bFile, "version.plist"));
	}

	/**
	 * Save this backup to a "file" (folder).
	 * 
	 * @param saveFile
	 *            The file to save the backup to.
	 */
	@Override
	public boolean save(File saveDir) {
		saveDir.mkdir();
		try {
			// write the content into xml file

			File backupDataFile = new File(saveDir, backupName);
			File touchFile = new File(saveDir, touchName);
			File versionFile = new File(saveDir, "version.plist");

			super.save(backupDataFile);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			DOMSource source = new DOMSource(touchPrefs);
			StreamResult result = new StreamResult(touchFile);
			transformer.transform(source, result);

			source = new DOMSource(version);
			result = new StreamResult(versionFile);
			transformer.transform(source, result);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
