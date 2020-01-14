package weka.classifiers.meta.scdrift;

public class NULLDM extends DriftDetectionMethod {

	private static final long serialVersionUID = 8829950282577141366L;

	public int computeNextVal(boolean prediction, double probability) {
		return DDM_INCONTROL_LEVEL;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
