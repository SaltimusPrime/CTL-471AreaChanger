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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class for storing backups and editing them.
 * 
 * @author SaltimusPrime
 *
 */
public class WacomBackup {

	protected Document backupXML;
	protected Node eX;
	protected Node eY;

	protected Node oX;
	protected Node oY;

	/**
	 * Load a backup for the CTL-471 from a file.
	 * 
	 * @param f
	 *            The file the backup is in.
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws TransformerException
	 */
	public WacomBackup(File f) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		backupXML = dBuilder.parse(f);
		
		// Process the loaded backup File.
		processXML();
	}

	/**
	 * Processes the selected backup file.
	 */
	protected void processXML() {
		backupXML.getDocumentElement().normalize();

		// The data we're looking for is stored in the last TabletInputArea element.
		NodeList areaNodes = backupXML.getElementsByTagName("TabletInputArea");
		Node areaSelectionNode = areaNodes.item(areaNodes.getLength() - 1);

		NodeList areaSelectNodeList = areaSelectionNode.getChildNodes();

		// Loop over the nodes and save the ones used to determine the active area for editing.
		for (int i = 0; i < areaSelectNodeList.getLength(); i++) {
			Node curNode = areaSelectNodeList.item(i);
			if (curNode.getNodeName().equals("Extent")) {
				for (int j = 0; j < curNode.getChildNodes().getLength(); j++) {
					Node valueNode = curNode.getChildNodes().item(j);
					if (valueNode.getNodeName().equals("X")) {
						eX = valueNode;
					}
					if (valueNode.getNodeName().equals("Y")) {
						eY = valueNode;
					}
				}
			} else if (curNode.getNodeName().equals("Origin")) {
				for (int j = 0; j < curNode.getChildNodes().getLength(); j++) {
					Node valueNode = curNode.getChildNodes().item(j);
					if (valueNode.getNodeName().equals("X")) {
						oX = valueNode;
					} else if (valueNode.getNodeName().equals("Y")) {
						oY = valueNode;
					}
				}
			}
		}
	}

	/**
	 * @return The top edge of the active area in counts.
	 */
	public int getTopValue() {
		int val = 0;
		try {
			val = Integer.parseInt(oY.getTextContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	/**
	 * Set the top edge of the active area in counts.
	 * 
	 * @param topVal
	 *            The new top edge value.
	 */
	public void setTopValue(final int topVal) {
		int curTop = Integer.parseInt(oY.getTextContent());
		int yExt = Integer.parseInt(eY.getTextContent());
		yExt -= topVal - curTop;
		oY.setTextContent(Integer.toString(topVal));
		eY.setTextContent(Integer.toString(yExt));
	}

	/**
	 * @return The bottom edge of the active area in counts.
	 */
	public int getBottomValue() {
		int val = 0;
		try {
			val = Integer.parseInt(oY.getTextContent()) + Integer.parseInt(eY.getTextContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	/**
	 * Set the bottom edge of the active area in counts.
	 * 
	 * @param bottomVal
	 *            The new bottom edge value.
	 */
	public void setBottomValue(final int bottomVal) {
		int yExt = bottomVal - Integer.parseInt(oY.getTextContent());
		eY.setTextContent(Integer.toString(yExt));
	}

	/**
	 * @return The left edge of the active area in counts.
	 */
	public int getLeftValue() {
		int val = 0;
		try {
			val = Integer.parseInt(oX.getTextContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	/**
	 * Set the left edge of the active area in counts.
	 * 
	 * @param leftVal
	 *            The new left edge value.
	 */
	public void setLeftValue(final int leftVal) {
		int curLeft = Integer.parseInt(oX.getTextContent());
		int xExt = Integer.parseInt(eX.getTextContent());
		xExt -= leftVal - curLeft;
		oX.setTextContent(Integer.toString(leftVal));
		eX.setTextContent(Integer.toString(xExt));
	}

	/**
	 * @return The right edge of the active area in counts.
	 */
	public int getRightValue() {
		int val = 0;
		try {
			val = Integer.parseInt(oX.getTextContent()) + Integer.parseInt(eX.getTextContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	/**
	 * Set the right edge of the active area in counts.
	 * 
	 * @param rightVal
	 *            The new right edge value.
	 */
	public void setRightValue(final int rightVal) {
		int xExt = rightVal - Integer.parseInt(oX.getTextContent());
		eX.setTextContent(Integer.toString(xExt));
	}

	/**
	 * Save this backup to a file.
	 * 
	 * @param saveFile
	 *            The file to save the backup to.
	 * @return true if the file was saved successfully.
	 */
	public boolean save(File saveFile) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(backupXML);
			StreamResult result = new StreamResult(saveFile);
			transformer.transform(source, result);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
