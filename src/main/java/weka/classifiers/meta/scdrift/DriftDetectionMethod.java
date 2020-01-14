package weka.classifiers.meta.scdrift;

import java.io.Serializable;

import weka.core.Utils;

public abstract class DriftDetectionMethod implements Serializable {

	private static final long serialVersionUID = -5038677174655119663L;
	
	public static final int DDM_INCONTROL_LEVEL = 0;
	public static final int DDM_WARNING_LEVEL = 1;
	public static final int DDM_OUTCONTROL_LEVEL = 2;

	public abstract int computeNextVal(boolean prediction, double probability);

	/**
	 * Creates a new instance of a drift detection method given it's class name and
	 * (optional) arguments to pass to it's setOptions method.
	 *
	 * @param driftDMName the fully qualified class name of the drift detection
	 *                    method
	 * @param options     an array of options suitable for passing to setOptions.
	 *                    May be null.
	 * @return the newly created drift detection method, ready for use.
	 * @exception Exception if the drift detection method name is invalid, or the
	 *                      options supplied are not acceptable to the method
	 */
	public static DriftDetectionMethod forName(String driftDMName, String[] options) throws Exception {

		return (DriftDetectionMethod) Utils.forName(DriftDetectionMethod.class, driftDMName, options);
	}

}
