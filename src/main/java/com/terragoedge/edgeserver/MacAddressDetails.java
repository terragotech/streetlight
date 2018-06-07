package com.terragoedge.edgeserver;

public class MacAddressDetails {

    private String nodeMacAddress;
    private String newNodeMacAddress;
    private String replaceNewNodeMacAddress;
    private String address;


    public String getNodeMacAddress() {
        return nodeMacAddress;
    }

    public void setNodeMacAddress(String nodeMacAddress) {
        this.nodeMacAddress = nodeMacAddress;
    }

    public String getNewNodeMacAddress() {
        return newNodeMacAddress;
    }

    public void setNewNodeMacAddress(String newNodeMacAddress) {
        this.newNodeMacAddress = newNodeMacAddress;
    }

    public String getReplaceNewNodeMacAddress() {
        return replaceNewNodeMacAddress;
    }

    public void setReplaceNewNodeMacAddress(String replaceNewNodeMacAddress) {
        this.replaceNewNodeMacAddress = replaceNewNodeMacAddress;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "MacAddressDetails{" +
                "nodeMacAddress='" + nodeMacAddress + '\'' +
                ", newNodeMacAddress='" + newNodeMacAddress + '\'' +
                ", replaceNewNodeMacAddress='" + replaceNewNodeMacAddress + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
