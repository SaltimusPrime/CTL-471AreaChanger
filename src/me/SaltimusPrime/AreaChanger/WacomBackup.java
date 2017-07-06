package me.SaltimusPrime.AreaChanger;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

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
import org.xml.sax.InputSource;
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

	protected Node maxX;
	protected Node maxY;

	protected boolean isContained;
	protected Document containedXML;
	protected Node containedNode;

	/**
	 * Load a backup for the tablet from a file.
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
	 * Process the nodes in the backup to get the values we want to edit.
	 * 
	 * @param areaNodes
	 *            The nodelist containing the area data.
	 */
	protected void processNodes(NodeList areaNodes) {
		Node maxAreaNode = areaNodes.item(0);

		// First determine the max area for the tablet.
		NodeList maxAreaNodeList = maxAreaNode.getChildNodes();

		// Loop over the nodes and save the ones used to determine the max area
		// for editing.
		for (int i = 0; i < maxAreaNodeList.getLength(); i++) {
			Node curNode = maxAreaNodeList.item(i);
			if (curNode.getNodeName().equals("Extent")) {
				for (int j = 0; j < curNode.getChildNodes().getLength(); j++) {
					Node valueNode = curNode.getChildNodes().item(j);
					if (valueNode.getNodeName().equals("X")) {
						maxX = valueNode;
					}
					if (valueNode.getNodeName().equals("Y")) {
						maxY = valueNode;
					}
				}
			}
		}

		Node areaSelectionNode = areaNodes.item(areaNodes.getLength() - 1);

		NodeList areaSelectNodeList = areaSelectionNode.getChildNodes();

		// Loop over the nodes and save the ones used to determine the active
		// area for editing.
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
	 * Processes the selected backup file.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	protected void processXML() throws ParserConfigurationException, SAXException, IOException {
		backupXML.getDocumentElement().normalize();

		// The data we're looking for is stored in the last TabletInputArea
		// element.
		NodeList areaNodes = backupXML.getElementsByTagName("TabletInputArea");
		if (areaNodes.getLength() == 0) {
			// The backup uses the contained format so it needs to be processed
			// differently.
			isContained = true;
			processContainedXML();
			return;
		} else {
			isContained = false;
		}

		processNodes(areaNodes);
	}

	/**
	 * Used for processing backups using the second backup format, this format
	 * stores the backup file as text in a containedfile field in the backupXML
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void processContainedXML() throws ParserConfigurationException, SAXException, IOException {
		NodeList containedNodes = backupXML.getElementsByTagName("ContainedFile");
		containedNode = containedNodes.item(0);
		String strBackup = containedNode.getTextContent();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		builder = factory.newDocumentBuilder();
		containedXML = builder.parse(new InputSource(new StringReader(strBackup)));

		// The data we're looking for is stored in the last TabletInputArea
		// element.
		NodeList areaNodes = containedXML.getElementsByTagName("TabletInputArea");

		processNodes(areaNodes);
	}

	/**
	 * Get the tablet area in the X direction.
	 * @return	The tablet X size in counts.
	 */
	public int getMaxX() {
		int val = 0;
		try {
			val = Integer.parseInt(maxX.getTextContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	/**
	 * Get the tablet area in the Y direction.
	 * @return	The tablet Y size in counts.
	 */
	public int getMaxY() {
		int val = 0;
		try {
			val = Integer.parseInt(maxY.getTextContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
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
		if (isContained) {
			return saveContained(saveFile);
		}

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

	/**
	 * Saves the contained backup file
	 * @param saveFile	The file to save the backup to.
	 * @return true if saved successfully.
	 */
	private boolean saveContained(File saveFile) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(containedXML);
			StringWriter writer = new StringWriter();
			transformer.transform(source, new StreamResult(writer));
			String strBackup = writer.getBuffer().toString();
			containedNode.setTextContent(strBackup);

			source = new DOMSource(backupXML);
			StreamResult result = new StreamResult(saveFile);
			transformer.transform(source, result);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
