package com.terragoedge.edgeserver;

import java.util.ArrayList;
import java.util.List;

public class ConditionCollection {
	
	private LogicalOperator conjunctionType;
    private List<Conditions> conditions = new ArrayList<>();
	public LogicalOperator getConjunctionType() {
		return conjunctionType;
	}
	public void setConjunctionType(LogicalOperator conjunctionType) {
		this.conjunctionType = conjunctionType;
	}
	public List<Conditions> getConditions() {
		return conditions;
	}
	public void setConditions(List<Conditions> conditions) {
		this.conditions = conditions;
	}
	@Override
	public String toString() {
		return "ConditionCollection [conjunctionType=" + conjunctionType + ", conditions=" + conditions + "]";
	}
    
    

}
