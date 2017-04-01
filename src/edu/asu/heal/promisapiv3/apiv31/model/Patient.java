package edu.asu.heal.promisapiv3.apiv31.model;

import java.util.ArrayList;

/**
 * Incomplete POJO describing a patient
 * @author kevinagary
 *
 */
public class Patient {

	public enum Trial{
		SICKLE_CELL,ORTHO_POST_OP,PAIN_POST_OP
	}

	public enum PatientType{
	child,parent_proxy,Adult
	}

	private String patientPin = null;
	private String createdByUserId = null;
	private String tsCreated = null;		// when the Patient was created
	private String trialId = null;			// clinical trial the Patient is in
	private String stageId = null;			// stage of the protocol
	private String childPIN = null;
	private String type = null;
	private Boolean enhancedContent = null;

	/**
	 * Default Constructor
	 */
	public Patient(){}
	/**
	 * Constructor that uses the DAO to populate
	 * @param pin
	 * @return
	 * @throws Exception
	 */
	public Patient(String pin,String sid,String chldPIN,String type, Boolean enhancedContent) throws Exception {
		// delegate to the DAO to get a PatientVO object, Exception on error or no such Patient
		// something like PatientVO pvo = theDAO.getPatient(pin); then invoke constructor?
		this.patientPin = pin;
		this.stageId = sid;
		this.childPIN = chldPIN;
		this.type = type;
		this.enhancedContent = enhancedContent;
	}

	/*public Patient(String cid, String tid, String sid) throws Exception {
		// invoke the DAO to get a PatientVO
		createdByUserId = cid;
		trialId = tid;
		stageId = sid;
		PatientVO pvo = new PatientVO();
		// By using an inner class we control who gets a PatientVO
		//theDAO.createPatient(pvo, createdById, tid, sid);
		//patientPin = pvo.pin;
		//tsCreated = pvo.tsCreated;
	}*/

	public String getChildPIN() {
		return childPIN;
	}

	public String getType() {
		return type;
	}

	public class PatientVO {
		public String pin;
		public String tsCreated;
		private PatientVO() {}
	}

	public String getPatientPin() {
		return patientPin;
	}

	public String getCreatedByUserId() {
		return createdByUserId;
	}

	public String getTsCreated() {
		return tsCreated;
	}

	public String getTrialId() {
		return trialId;
	}

	public String getStageId() {
		return stageId;
	}

	public Boolean getEnhancedContent(){
		return enhancedContent;
	}
	//The inner class for enrolling patients.
	public class PatientEnroll
	{
		public PatientEnroll(String patientType,String childPIN, String deviceType, String deviceVersion
							,String hydroxyUrea,ArrayList<medicationInfo> patientMedication,Trial trialType)
		{
			_patientType = patientType;
			_childPIN = childPIN;
			_deviceType = deviceType;
			_deviceVersion = deviceVersion;
			_isHydroxyUreaPrescribed = hydroxyUrea;
			_patientMedication = patientMedication;
			_trialType = trialType;
		}
		private String _patientType;
		private String _childPIN;
		private String _deviceType;
		private String _deviceVersion;
		private String _isHydroxyUreaPrescribed;
		private ArrayList<medicationInfo> _patientMedication;
		private Trial _trialType;

		public Trial get_trialType() {
			return _trialType;
		}
		public void set_trialType(Trial _trialType) {
			this._trialType = _trialType;
		}
		public String getPatientType() {
			return _patientType;
		}
		public void setPatientType(String patientType) {
			this._patientType = patientType;
		}
		public String getChildPIN() {
			return _childPIN;
		}
		public void setChildPIN(String childPIN) {
			this._childPIN = childPIN;
		}
		public String getDeviceType() {
			return _deviceType;
		}
		public void setDeviceType(String deviceType) {
			this._deviceType = deviceType;
		}
		public String getDeviceVersion() {
			return _deviceVersion;
		}
		public void setDeviceVersion(String deviceVersion) {
			this._deviceVersion = deviceVersion;
		}
		public String isHydroxyUreaPrescribed() {
			return _isHydroxyUreaPrescribed;
		}
		public void setHydroxyUreaPrescribed(String isHydroxyUreaPrescribed) {
			this._isHydroxyUreaPrescribed = isHydroxyUreaPrescribed;
		}
		public ArrayList<medicationInfo> getPatientMedication() {
			return _patientMedication;
		}
		public void setPatientMedication(ArrayList<medicationInfo> patientMedication) {
			this._patientMedication = patientMedication;
		}

	}
	//This is the class for containing the medication info.
	public class medicationInfo
	{
		private String _mediceName;
		private String _units;
		private int _prescribedDosage; //Prescribed no of tablets.
		private int _noOfTablets;

		public medicationInfo(String medicationName,String units,int prescribedDosage,int noOfTablets)
		{
			_mediceName = medicationName;
			_units = units;
			_prescribedDosage = prescribedDosage;
			_noOfTablets = noOfTablets;
		}

		public String getMediceName() {
			return _mediceName;
		}
		public void setMediceName(String mediceName) {
			this._mediceName = mediceName;
		}
		public String getUnits() {
			return _units;
		}
		public void setUnits(String units) {
			this._units = units;
		}
		public int getPrescribedDosage() {
			return _prescribedDosage;
		}
		public void setPrescribedDosage(int prescribedDosage) {
			this._prescribedDosage = prescribedDosage;
		}
		public int getNoOfTablets() {
			return _noOfTablets;
		}
		public void setNoOfTablets(int noOfTablets) {
			this._noOfTablets = noOfTablets;
		}
	}
}
