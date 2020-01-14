/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    scdrift.java
 *    Author 2006 Manuel Baena García
 *    Copyright (C) 2006 Universidad de Málaga
 *
 */
package weka.classifiers.meta;

import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.meta.scdrift.DriftDetectionMethod;
import weka.classifiers.meta.scdrift.EDDM;
import weka.classifiers.meta.scdrift.JGamaMethod;
import weka.classifiers.meta.scdrift.NULLDM;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.Utils;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;

/**
 * Class for handling concept drift datasets with a wrapper on a classifier.
 * <p>
 * data
 *
 * Valid options are:
 * <p>
 *
 * -W classname <br>
 * Specify the full class name of a classifier as the basis for the concept
 * drift classifier (required).
 * <p>
 *
 *
 * @author Manuel Baena (mbaena@uma.es)
 * @version 1.1
 */
public class SCDrift extends SingleClassifierEnhancer implements TechnicalInformationHandler, OptionHandler {

	private static final long serialVersionUID = -3502061160181118975L;

	/**
	 * Returns an instance of a TechnicalInformation object, containing detailed
	 * information about the technical background of this class, e.g., paper
	 * reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;

		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Manuel Baena-Garcia, Jose Del Campo-Avila, Raúl Fidalgo, Albert Bifet, Ricard Gavalda, Rafael Morales-Bueno");
		result.setValue(Field.YEAR, "2006");
		result.setValue(Field.TITLE, "Early Drift Detection Method");
		result.setValue(Field.JOURNAL, "In Fourth International Workshop on Knowledge Discovery from Data Streams");

		return result;
	}

	/* Define possible drift detection methods */
	public static final int DDM_NULL = 0;
	public static final int DDM_JGAMA = 1;
	public static final int DDM_EDDM = 2;
	public static final Tag[] TAGS_DRIFTDETECTION = { new Tag(DDM_JGAMA, "JGama Drift Detection Method"),
			new Tag(DDM_EDDM, "EDDM - Early Drift Detection Method"), new Tag(DDM_NULL, "Without Detection Method") };

	protected int m_ddmTag;
	protected DriftDetectionMethod m_ddm = new JGamaMethod();

	public SCDrift() {
		init();
	}

	/**
	 * Initialise scheme variables.
	 */
	protected void init() {

		m_ddmTag = DDM_JGAMA;
	}

	/**
	 * String describing default classifier.
	 */
	protected String defaultDriftDetectionMethodString() {
		//return JGamaMethod.class.getPackage() + "." + JGamaMethod.class.getName();
		return JGamaMethod.class.getName();
	}

	/**
	 * Returns an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options.
	 */
	public Enumeration<Option> listOptions() {

		Vector<Option> newVector = new Vector<>(4);

		Enumeration<Option> enu = super.listOptions();
		while (enu.hasMoreElements()) {
			newVector.addElement(enu.nextElement());
		}

		newVector.addElement(new Option("\tFull name of base drift detection method.\n" + "\t(default: "
				+ defaultDriftDetectionMethodString() + ")", "K", 1, "-K 'drift detection method class'"));

		return newVector.elements();
	}

	/**
	 * Parses a given list of options. Valid options are:
	 * <p>
	 *
	 * -W classname <br>
	 * Specify the full class name of the base learner.
	 * <p>
	 *
	 * Options after -- are passed to the designated classifier.
	 * <p>
	 *
	 * @param options the list of options as an array of strings
	 * @exception Exception if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {

		String driftDMName = Utils.getOption('K', options);

		super.setOptions(options);

		if (driftDMName.length() > 0) {

			// This is just to set the classifier in case the option
			// parsing fails.
			setDdMethod(DriftDetectionMethod.forName(driftDMName, null));
			setDdMethod(DriftDetectionMethod.forName(driftDMName, Utils.partitionOptions(options)));

		} else {

			// This is just to set the classifier in case the option
			// parsing fails.

			setDdMethod(DriftDetectionMethod.forName(defaultDriftDetectionMethodString(), null));
			setDdMethod(
					DriftDetectionMethod.forName(defaultDriftDetectionMethodString(), Utils.partitionOptions(options)));

		}
	}

	public void setDdm(SelectedTag newMethod) {

		if (newMethod.getTags() == TAGS_DRIFTDETECTION) {
			m_ddmTag = newMethod.getSelectedTag().getID();
		}
		switch (m_ddmTag) {
		case DDM_JGAMA:
			setDdMethod(new JGamaMethod());
			break;
		case DDM_EDDM:
			setDdMethod(new EDDM());
			break;
		case DDM_NULL:
			setDdMethod(new NULLDM());
			break;
		default:
			break;
		}

	}

	public SelectedTag getDdm() {
		return new SelectedTag(m_ddmTag, TAGS_DRIFTDETECTION);
	}

	private void setDdMethod(DriftDetectionMethod newMethod) {
		m_ddm = newMethod;
	}

	private DriftDetectionMethod getDdMethod() {
		return m_ddm;
	}

	/**
	 * Gets the current settings of the Classifier.
	 *
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String[] getOptions() {

		String[] superOptions = super.getOptions();
		String[] options = new String[superOptions.length + 2];

		int current = 0;
		options[current++] = "-K";
		options[current++] = getDdMethod().getClass().getName();

		System.arraycopy(superOptions, 0, options, current, superOptions.length);
		current += superOptions.length;

		return options;
	}

	/**
	 * Returns a string describing the classifier.
	 * 
	 * @return a description suitable for the GUI.
	 */
	public String globalInfo() {

		return "Class for constructing a metamodel based on a simple concept drift"
				+ "detection method. For more information see: \n\n"
				+ " J. Gama, P. Medas, G. Castillo, P. Rodrigues. \"Learning with Drift" + " Detection\". ";
	}

	/**
	 * Builds SCDrift classifier.
	 *
	 * @param data the training data
	 * @exception Exception if classifier can't be built successfully
	 */
	public void buildClassifier(Instances data) throws Exception {

		Instances currentData, nextData;
		Instance inst;
		Classifier classifier;
		boolean prediction;
		int n = 1;
		int localn = 1;
		double precuential = 1.0;
		double localprecuential = 1.0;

		if (m_Classifier == null) {
			throw new Exception("No base classifier has been set!");
		}

		Enumeration<Instance> enu = data.enumerateInstances();

		if (!enu.hasMoreElements()) {
			return;
		}
		currentData = new Instances(data, 0);
		nextData = new Instances(data, 0);
		currentData.add((Instance) enu.nextElement());
		classifier = AbstractClassifier.makeCopy(m_Classifier);
		classifier.buildClassifier(currentData);

		while (enu.hasMoreElements()) {
			n++;
			localn++;
			inst = (Instance) enu.nextElement();

			if (inst.classValue() == classifier.classifyInstance(inst)) {
				prediction = true;
				precuential = precuential - (precuential) / (double) n;
				localprecuential = localprecuential - (localprecuential) / (double) localn;
			} else {
				prediction = false;
				precuential = precuential + (1 - precuential) / (double) n;
				localprecuential = localprecuential + (1 - localprecuential) / (double) localn;
			}

			System.out.print(precuential + " " + localprecuential + " ");
			switch (m_ddm.computeNextVal(prediction,
					classifier.distributionForInstance(inst)[(int) inst.classValue()])) {
			case DriftDetectionMethod.DDM_WARNING_LEVEL:
				System.out.println("1 0 W");
				nextData.add(inst);
				break;

			case DriftDetectionMethod.DDM_OUTCONTROL_LEVEL:
				System.out.println("0 1 O");
				localprecuential = 1.0;
				localn = 1;
				currentData = new Instances(nextData);
				if (classifier instanceof UpdateableClassifier) {
					classifier = AbstractClassifier.makeCopy(m_Classifier);
					classifier.buildClassifier(currentData);
				}
				break;

			case DriftDetectionMethod.DDM_INCONTROL_LEVEL:
				System.out.println("0 0 I");
				nextData.delete();
				break;
			default:
				System.out.println("ERROR!");

			}

			if (classifier instanceof UpdateableClassifier) {
				((UpdateableClassifier) classifier).updateClassifier(inst);
			} else {
				currentData.add(inst);
				classifier = AbstractClassifier.makeCopy(m_Classifier);
				classifier.buildClassifier(currentData);
			}
		}

		m_Classifier = classifier;
	}

	/**
	 * Classifies a given test instance using the model.
	 *
	 * @param instance the instance to be classified
	 * @return the classification
	 * @throws Exception a classifier exception
	 */
	public double classifyInstance(Instance instance) throws Exception {
		return m_Classifier.classifyInstance(instance);
	}

	/**
	 * Computes class distribution for instance using model.
	 *
	 * @param instance the instance for which distribution is to be computed
	 * @return the class distribution for the given instance
	 * @throws Exception a classifier exception
	 */
	public double[] distributionForInstance(Instance instance) throws Exception {

		return m_Classifier.distributionForInstance(instance);

	}

	/**
	 * Prints the model using the private classifier toString method.
	 *
	 * @return a textual description of the classifier
	 */
	public String toString() {

		if (m_Classifier == null) {
			return "SCDrift: No model built yet.";
		}
		return "SCDrift\n\n" + m_Classifier.toString();
	}

	  /**
	   * Main method for testing this class
	   * 
	   * @param argv the commandline options
	   */
	  public static void main(String[] argv) {
	    runClassifier(new SCDrift(), argv);
	  }
}
