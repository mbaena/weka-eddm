package weka.classifiers.meta.scdrift;

public class JGamaMethod extends DriftDetectionMethod {

	private static final long serialVersionUID = -5342393143097617935L;

	private static final int JGAMAMETHOD_MINNUMINST = 30;
	private int m_n;
	private double m_p;
	private double m_s;
	private double m_psmin;
	private double m_pmin;
	private double m_smin;

	public JGamaMethod() {
		initialize();
	}

	private void initialize() {
		m_n = 1;
		m_p = 1;
		m_s = 0;
		m_psmin = Double.MAX_VALUE;
		m_pmin = Double.MAX_VALUE;
		m_smin = Double.MAX_VALUE;
	}

	public int computeNextVal(boolean prediction, double probability) {
		if (prediction == false) {
			m_p = m_p + (1.0 - m_p) / (double) m_n;
		} else {
			m_p = m_p - (m_p) / (double) m_n;
		}
		m_s = Math.sqrt(m_p * (1 - m_p) / (double) m_n);

		m_n++;

		System.out.print(prediction + " " + m_n + " " + (m_p + m_s) + " ");

		if (m_n < JGAMAMETHOD_MINNUMINST) {
			return DDM_INCONTROL_LEVEL;
		}

		if (m_p + m_s <= m_psmin) {
			m_pmin = m_p;
			m_smin = m_s;
			m_psmin = m_p + m_s;
		}

		if (m_n > JGAMAMETHOD_MINNUMINST && m_p + m_s > m_pmin + 3 * m_smin) {
			initialize();
			return DDM_OUTCONTROL_LEVEL;
		} else if (m_p + m_s > m_pmin + 2 * m_smin) {
			return DDM_WARNING_LEVEL;
		} else {
			return DDM_INCONTROL_LEVEL;
		}
	}

}
