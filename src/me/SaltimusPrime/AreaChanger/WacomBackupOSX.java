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

	private Document touchPrefs;
	private Document version;

	/**
	 * Loads an OSX backup for the CTL-471.
	 * 
	 * @param bFile
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public WacomBackupOSX(File bFile)
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		// The selected file is a folder that contains the actual backup, we
		// pass the file containing the tablet area data to the parent class for
		// processing.
		super(new File(bFile, "com.wacom.pentablet.prefs"));

		// The other files in the folder we simply store so we can create an edited backup later.
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		touchPrefs = dBuilder.parse(new File(bFile, "com.wacom.touch.prefs"));
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

			File backupDataFile = new File(saveDir, "com.wacom.pentablet.prefs");
			File touchFile = new File(saveDir, "com.wacom.touch.prefs");
			File versionFile = new File(saveDir, "version.plist");

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(backupXML);
			StreamResult result = new StreamResult(backupDataFile);
			transformer.transform(source, result);

			source = new DOMSource(touchPrefs);
			result = new StreamResult(touchFile);
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
