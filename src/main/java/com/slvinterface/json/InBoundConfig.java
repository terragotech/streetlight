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
    List<SLVFields> slvchangefields;

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
