package me.SaltimusPrime.AreaChanger;

import javax.swing.JOptionPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * InputFilter used to make sure the user cannot enter text or values that fall outside of the tablet area.
 * 
 * @author SaltimusPrime
 *
 */
public class IntInputFilter extends DocumentFilter {

	private int maxValue;

	/**
	 * Input filter used to prevent invalid input.
	 * @param maxValue	The highest acceptable input value.
	 */
	public IntInputFilter(int maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public void insertString(FilterBypass bypass, int offset, String str, AttributeSet attr)
			throws BadLocationException {
		// Get the new string the textfield would contain.
		Document doc = bypass.getDocument();
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(doc.getText(0, doc.getLength()));
		strBuilder.insert(offset, str);

		// Test if the input is still a number and doesn't fall outside the
		// active area.
		if (isValid(strBuilder.toString())) {
			super.insertString(bypass, offset, str, attr);
		} else {
			JOptionPane.showMessageDialog(null, "Enter a number between 0 and " + maxValue, "Invalid input", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void replace(FilterBypass bypass, int offset, int length, String str, AttributeSet attrs)
			throws BadLocationException {
		// Get the new string the textfield would contain.
		Document doc = bypass.getDocument();
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(doc.getText(0, doc.getLength()));
		strBuilder.replace(offset, offset + length, str);

		// Test if the input is still a number and doesn't fall outside the
		// active area.
		if (isValid(strBuilder.toString())) {
			super.replace(bypass, offset, length, str, attrs);
		} else {
			JOptionPane.showMessageDialog(null, "Enter a number between 0 and " + maxValue, "Invalid input", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void remove(FilterBypass bypass, int offset, int length) throws BadLocationException {
		// Get the new string the textfield would contain.
		Document doc = bypass.getDocument();
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(doc.getText(0, doc.getLength()));
		strBuilder.delete(offset, offset + length);

		// Test if the input is still a number and doesn't fall outside the
		// active area.
		if (isValid(strBuilder.toString())) {
			super.remove(bypass, offset, length);
		} else {
			JOptionPane.showMessageDialog(null, "Enter a number between 0 and " + maxValue, "Invalid input", JOptionPane.ERROR_MESSAGE);
		}

	}

	/**
	 * Checks if the input is a number and is between 0 and maxValue
	 * @param input	The input string to test.
	 * @return	True if valid.
	 */
	private boolean isValid(final String input) {
		if (input.length() == 0)
		{
			return true;
		}
		try {
			int res = Integer.parseInt(input);
			if (res <= maxValue && res >= 0) {
				return true;
			}
		} catch (NumberFormatException e) {
			// Do nothing, input was invalid.
		}
		return false;
	}

	/**
	 * Getter for the maxValue
	 * @return	maxValue
	 */
	public int getMaxValue() {
		return maxValue;
	}

	/**
	 * Setter for the maxValue
	 * @param maxValue	the new maxValue.
	 */
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

}
