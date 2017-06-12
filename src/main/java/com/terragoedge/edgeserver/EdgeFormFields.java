package com.terragoedge.edgeserver;

import java.util.ArrayList;
import java.util.List;

public class EdgeFormFields {
	
	private int id;
    private ComponentType component;
    private boolean editable;
    private int index;
    private String label;
    private String description;
    private String placeHolder;
    private String placeholder;
    private boolean required;
    private boolean repeatable;
    private String validation;
    private String currency;
    private Occurences  occurrences;
    private CalculationDefinition calculationDefinition;
    private ConditionCollection conditionCollection;
    private List<String> options = new ArrayList<>();
    private String capturedValue;
    
    
    private NotesType type;

    private List<EdgeFormFields> components = new ArrayList<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ComponentType getComponent() {
		return component;
	}

	public void setComponent(ComponentType component) {
		this.component = component;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPlaceHolder() {
		return placeHolder;
	}

	public void setPlaceHolder(String placeHolder) {
		this.placeHolder = placeHolder;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isRepeatable() {
		return repeatable;
	}

	public void setRepeatable(boolean repeatable) {
		this.repeatable = repeatable;
	}

	public String getValidation() {
		return validation;
	}

	public void setValidation(String validation) {
		this.validation = validation;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Occurences getOccurrences() {
		return occurrences;
	}

	public void setOccurrences(Occurences occurrences) {
		this.occurrences = occurrences;
	}

	public CalculationDefinition getCalculationDefinition() {
		return calculationDefinition;
	}

	public void setCalculationDefinition(CalculationDefinition calculationDefinition) {
		this.calculationDefinition = calculationDefinition;
	}

	public ConditionCollection getConditionCollection() {
		return conditionCollection;
	}

	public void setConditionCollection(ConditionCollection conditionCollection) {
		this.conditionCollection = conditionCollection;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	public String getCapturedValue() {
		return capturedValue;
	}

	public void setCapturedValue(String capturedValue) {
		this.capturedValue = capturedValue;
	}

	public NotesType getType() {
		return type;
	}

	public void setType(NotesType type) {
		this.type = type;
	}

	public List<EdgeFormFields> getComponents() {
		return components;
	}

	public void setComponents(List<EdgeFormFields> components) {
		this.components = components;
	}

	@Override
	public String toString() {
		return "EdgeFormFields [id=" + id + ", component=" + component + ", editable=" + editable + ", index=" + index
				+ ", label=" + label + ", description=" + description + ", placeHolder=" + placeHolder
				+ ", placeholder=" + placeholder + ", required=" + required + ", repeatable=" + repeatable
				+ ", validation=" + validation + ", currency=" + currency + ", occurrences=" + occurrences
				+ ", calculationDefinition=" + calculationDefinition + ", conditionCollection=" + conditionCollection
				+ ", options=" + options + ", capturedValue=" + capturedValue + ", type=" + type + ", components="
				+ components + "]";
	}
    
    

}
