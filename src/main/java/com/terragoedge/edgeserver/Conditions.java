package com.terragoedge.edgeserver;

public class Conditions {
	
	 private String sourceComponent;
	    private ArithmeticOperator equalityType;
	    private String targetValue;
		public String getSourceComponent() {
			return sourceComponent;
		}
		public void setSourceComponent(String sourceComponent) {
			this.sourceComponent = sourceComponent;
		}
		public ArithmeticOperator getEqualityType() {
			return equalityType;
		}
		public void setEqualityType(ArithmeticOperator equalityType) {
			this.equalityType = equalityType;
		}
		public String getTargetValue() {
			return targetValue;
		}
		public void setTargetValue(String targetValue) {
			this.targetValue = targetValue;
		}
		@Override
		public String toString() {
			return "Conditions [sourceComponent=" + sourceComponent + ", equalityType=" + equalityType
					+ ", targetValue=" + targetValue + "]";
		}
	    
	    

}
