package com.terragoedge.install;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.install.exception.InValidFormException;
import com.terragoedge.install.exception.SLNumberException;

public class StreetLightInstallService {

	private StreetLightInstallDAO streetLightInstallDAO;
	private Properties properties = null;
	private Gson gson = null;
	
	
	private Map<String, String> deviceList = new ConcurrentHashMap<>();
	private Map<String, String> macAddressList = new ConcurrentHashMap<>();

	public StreetLightInstallService() {

	}
	

	public void process() {
		List<NoteDetails> noteDetailsList = streetLightInstallDAO.getUnSyncedNoteIds();
		for (NoteDetails noteDetails : noteDetailsList) {
			LoggingDetails loggingDetails = new LoggingDetails();
			SLVDataEntity slvDataEntity = new SLVDataEntity();
			loadLoggingDetails(noteDetails, loggingDetails);
			process(noteDetails, loggingDetails);
		}
	}
	

	private void loadLoggingDetails(NoteDetails noteDetails, LoggingDetails loggingDetails) {
		loggingDetails.setNoteId(noteDetails.getNoteid());
		loggingDetails.setNoteGuid(noteDetails.getNoteGuid());
		loggingDetails.setTitle(noteDetails.getTitle());
	}
	

	private void process(NoteDetails noteDetails, LoggingDetails loggingDetails,SLVDataEntity slvDataEntity) {
		try {
			streetLightInstallDAO.getFormDetails(noteDetails);
			int totalSize = noteDetails.getFormDetails().size();
			loggingDetails.setTotalForms(String.valueOf(totalSize));
			if (totalSize > 0) {
				validateFormTemplate(noteDetails);
			} else {
				loggingDetails.setStatus(Constants.FAILURE);
				loggingDetails.setDescription("No Form is present in this note.");
			}
		} catch (InValidFormException e1) {
			loggingDetails.setStatus(Constants.FAILURE);
			loggingDetails.setDescription(e1.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void processEdgeForms(NoteDetails noteDetails, SLVDataEntity slvDataEntity) throws SLNumberException{
		getSLNumber(noteDetails, slvDataEntity);
		List<FormDetails> formDetailsList = noteDetails.getFormDetails();
		for (FormDetails formDetails : formDetailsList) {
			
		}
	}
	
	
	private void processEdgeForm(NoteDetails noteDetails, SLVDataEntity slvDataEntity,FormDetails formDetails ){
		List<FormValues> edgeFormValuesList = gson.fromJson(formDetails.getFormDef(),
				new TypeToken<List<FormValues>>() {
				}.getType());
		FormValues qrCodeFormValues = getFormValues(edgeFormValuesList, "Action");
		switch (qrCodeFormValues.getValue()) {
		case "New Streetlight":
			break;
			
		case "Update Streetlight":
			break;
			
		case "Remove Streetlight":
			break;

		default:
			break;
		}
	}
	
	
	private void getSLNumber(NoteDetails noteDetails, SLVDataEntity slvDataEntity) throws SLNumberException{
		List<FormDetails> formDetailsList = noteDetails.getFormDetails();
		Set<String> slNumbers = new HashSet<>();
		for (FormDetails formDetails : formDetailsList) {
			List<FormValues> edgeFormValuesList = gson.fromJson(formDetails.getFormDef(),
					new TypeToken<List<FormValues>>() {
					}.getType());
			FormValues qrCodeFormValues = getFormValues(edgeFormValuesList, "SELC QR Code");
			String values = qrCodeFormValues.getValue();
			if (values != null) {
				slNumbers.add(values);
				slvDataEntity.setIdOnController(values);
				
				FormValues controllerSrtValues = getFormValues(edgeFormValuesList, "Controller Str ID");
				String controllerSrtValue = controllerSrtValues.getValue();
				slvDataEntity.setControllerStrId(controllerSrtValue);
			}
		}
		int size = slNumbers.size();
		if(size == 0){
			throw new SLNumberException("ID on Controller is not present.");
			// throw no mac address
		}else if(size == 1){
			// no process
		}else{
			throw new SLNumberException("More than one ID on Controller is present.");
		}
	}
	
	
	private void validateNewStreetMacAddress(NoteDetails noteDetails, SLVDataEntity slvDataEntity) {
		List<FormDetails> formDetailsList = noteDetails.getFormDetails();
		Set<String> qrCodes = new HashSet<>();
		for (FormDetails formDetails : formDetailsList) {
			List<FormValues> edgeFormValuesList = gson.fromJson(formDetails.getFormDef(),
					new TypeToken<List<FormValues>>() {
					}.getType());
			FormValues qrCodeFormValues = getFormValues(edgeFormValuesList, "SELC QR Code");
			String values = qrCodeFormValues.getValue();
			if (values != null) {
				qrCodes.add(values);
				slvDataEntity.setMacAddress(values);
			}
			FormValues luminareScanValues = getFormValues(edgeFormValuesList, "Luminaire Scan");
			String luminareScanValue = luminareScanValues.getValue();
			slvDataEntity.setLuminareCode(luminareScanValue);
		}
		int size = qrCodes.size();
		if(size == 0){
			// throw no mac address
		}else if(size == 1){
			// no process
		}else{
			// throw different types
		}
	}
	
	
	private FormValues getFormValues(List<FormValues> edgeFormValuesList,String lab){
		FormValues formValues = new FormValues();
		formValues.setLabel(lab);
		int pos = edgeFormValuesList.indexOf(formValues);
		if(pos != -1){
			return edgeFormValuesList.get(pos);
		}
		return null;
	}
	
	/***
	 * Check this note contains Streetlight install form or not
	 * @param noteDetails
	 * @throws InValidFormException 
	 */
	private void validateFormTemplate(NoteDetails noteDetails) throws InValidFormException{
		List<FormDetails> formDetailsList = noteDetails.getFormDetails();
		boolean isFormPresent = false;
		for(FormDetails formDetails : formDetailsList){
			if(formDetails.getFormTemplateGuid().equals("")){ // -- TODO
				isFormPresent = true;
			}
		}
		
		if(!isFormPresent){
			throw new InValidFormException("Streetlight Installation Form is not present in this note.");
		}
	}

}
