package com.slvinterface.json;

import java.util.List;

public class InBoundConfig {
    List<SLVFields> slvfields;
    String formtemplateguid;
    String slvquery;
    String completenotelayerguid;
    String notcompletenotelayerguid;
    String defaultnotelayerguid;
    String usedefaultlayerguid;
    String slvnmacaddressfield;
    String installmacaddress_id;
    String prevmacaddress_id;
    String replacemacaddress_id;
    String removemacaddress_id;
    List<SLVFields> slvchangefields;

    public String getPrevmacaddress_id() {
        return prevmacaddress_id;
    }

    public void setPrevmacaddress_id(String prevmacaddress_id) {
        this.prevmacaddress_id = prevmacaddress_id;
    }

    public String getInstallmacaddress_id() {
        return installmacaddress_id;
    }

    public void setInstallmacaddress_id(String installmacaddress_id) {
        this.installmacaddress_id = installmacaddress_id;
    }

    public String getReplacemacaddress_id() {
        return replacemacaddress_id;
    }

    public void setReplacemacaddress_id(String replacemacaddress_id) {
        this.replacemacaddress_id = replacemacaddress_id;
    }

    public String getRemovemacaddress_id() {
        return removemacaddress_id;
    }

    public void setRemovemacaddress_id(String removemacaddress_id) {
        this.removemacaddress_id = removemacaddress_id;
    }

    public List<SLVFields> getSlvchangefields() {
        return slvchangefields;
    }

    public void setSlvchangefields(List<SLVFields> slvchangefields) {
        this.slvchangefields = slvchangefields;
    }

    public String getCompletenotelayerguid() {
        return completenotelayerguid;
    }

    public void setCompletenotelayerguid(String completenotelayerguid) {
        this.completenotelayerguid = completenotelayerguid;
    }

    public String getNotcompletenotelayerguid() {
        return notcompletenotelayerguid;
    }

    public void setNotcompletenotelayerguid(String notcompletenotelayerguid) {
        this.notcompletenotelayerguid = notcompletenotelayerguid;
    }

    public String getDefaultnotelayerguid() {
        return defaultnotelayerguid;
    }

    public void setDefaultnotelayerguid(String defaultnotelayerguid) {
        this.defaultnotelayerguid = defaultnotelayerguid;
    }

    public String getUsedefaultlayerguid() {
        return usedefaultlayerguid;
    }

    public void setUsedefaultlayerguid(String usedefaultlayerguid) {
        this.usedefaultlayerguid = usedefaultlayerguid;
    }

    public String getSlvnmacaddressfield() {
        return slvnmacaddressfield;
    }

    public void setSlvnmacaddressfield(String slvnmacaddressfield) {
        this.slvnmacaddressfield = slvnmacaddressfield;
    }

    public List<SLVFields> getSlvfields() {
        return slvfields;
    }

    public void setSlvfields(List<SLVFields> slvfields) {
        this.slvfields = slvfields;
    }

    public String getFormtemplateguid() {
        return formtemplateguid;
    }

    public void setFormtemplateguid(String formtemplateguid) {
        this.formtemplateguid = formtemplateguid;
    }

    public String getSlvquery() {
        return slvquery;
    }

    public void setSlvquery(String slvquery) {
        this.slvquery = slvquery;
    }
}
