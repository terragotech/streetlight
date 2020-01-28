package com.terragoedge.edgeserver;

public class InstallAndMaintainFormData extends EdgeFormData{

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EdgeFormData other = (EdgeFormData) obj;
		if (id != other.id)
			return false;
		return true;
	}

	
	
	

}
