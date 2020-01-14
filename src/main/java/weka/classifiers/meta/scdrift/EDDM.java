package weka.classifiers.meta.scdrift;

public class EDDM extends DriftDetectionMethod {

	private static final long serialVersionUID = 4717445836720558690L;

	private static final double FDDM_OUTCONTROL = 0.9;
	private static final double FDDM_WARNING = 0.95;

	private static final double FDDM_MINNUMINSTANCES = 30;

	private double m_numErrors;
	private int m_minNumErrors = 30;
	private int m_n;
	private int m_d;
	private int m_lastd;

	private double m_mean;
	private double m_stdTemp;
	private double m_m2smax;
	private int m_lastLevel;

	public EDDM() {
		initialize();
	}

	private void initialize() {
		m_n = 1;
		m_numErrors = 0;
		m_d = 0;
		m_lastd = 0;
		m_mean = 0.0;
		m_stdTemp = 0.0;
		m_m2smax = 0.0;
		m_lastLevel = DDM_INCONTROL_LEVEL;
	}

	public int computeNextVal(boolean prediction, double probability) {
		System.out.print(prediction + " " + m_n + " " + probability + " ");
		m_n++;
		if (prediction == false) {
			m_numErrors += 1;
			m_lastd = m_d;
			m_d = m_n - 1;
			int distance = m_d - m_lastd;
			double oldmean = m_mean;
			m_mean = m_mean + ((double) distance - m_mean) / m_numErrors;
			m_stdTemp = m_stdTemp + (distance - m_mean) * (distance - oldmean);
			double std = Math.sqrt(m_stdTemp / m_numErrors);
			double m2s = m_mean + 2 * std;

			System.out.print(m_numErrors + " " + m_mean + " " + std + " " + m2s + " " + m_m2smax + " ");

			if (m2s > m_m2smax) {
				if (m_n > FDDM_MINNUMINSTANCES) {
					m_m2smax = m2s;
				}
				m_lastLevel = DDM_INCONTROL_LEVEL;
				System.out.print(1 + " ");
			} else {
				double p = m2s / m_m2smax;
				System.out.print(p + " ");
				if (m_n > FDDM_MINNUMINSTANCES && m_numErrors > m_minNumErrors && p < FDDM_OUTCONTROL) {
					initialize();
					return DDM_OUTCONTROL_LEVEL;
				} else if (m_n > FDDM_MINNUMINSTANCES && m_numErrors > m_minNumErrors && p < FDDM_WARNING) {
					m_lastLevel = DDM_WARNING_LEVEL;
					return DDM_WARNING_LEVEL;
				} else {
					m_lastLevel = DDM_INCONTROL_LEVEL;
					return DDM_INCONTROL_LEVEL;
				}
			}
		} else {
			System.out.print(m_numErrors + " " + m_mean + " " + Math.sqrt(m_stdTemp / m_numErrors) + " "
					+ (m_mean + 2 * Math.sqrt(m_stdTemp / m_numErrors)) + " " + m_m2smax + " ");
			System.out.print(((m_mean + 2 * Math.sqrt(m_stdTemp / m_numErrors)) / m_m2smax) + " ");
		}
		return m_lastLevel;
	}

}
