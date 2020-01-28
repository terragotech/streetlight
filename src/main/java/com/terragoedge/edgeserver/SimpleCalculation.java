package com.terragoedge.edgeserver;

import java.util.ArrayList;
import java.util.List;

public class SimpleCalculation {
	
	private List<CalculationNode> calculationNodes = new ArrayList<CalculationNode>();

    public List<CalculationNode> getCalculationNodes() {
        return calculationNodes;
    }

    public void setCalculationNodes(List<CalculationNode> calculationNodes) {
        this.calculationNodes = calculationNodes;
    }

}
