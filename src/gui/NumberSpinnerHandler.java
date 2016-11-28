package gui;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class NumberSpinnerHandler extends JSpinner {
	int currValue, min, max, interval;
	SpinnerNumberModel model;

	public NumberSpinnerHandler(int value, int min, int max, int interval) {
		this.currValue = value;
		this.min = min;
		this.max = max;
		this.interval = interval;

		// Set editor to add leading zeros
		setEditor(new JSpinner.NumberEditor(this, "00"));
		model = new SpinnerNumberModel(value, min, max, interval);
		setValue(value);

		// Add listeners for wrapping
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int newValue = (int) getValue();

				if (newValue > currValue) { // increased
					currValue += interval;
					if (currValue > max)
						currValue = min; // wrap

				} else if (newValue < currValue) { // decreased
					currValue -= interval;
					if (currValue < min)
						currValue = max - (interval - 1); // wrap
				}
				setValue(currValue);
			}
		});
	}

	public int getCurrentValue() {
		return (int) getValue();
	}
}
