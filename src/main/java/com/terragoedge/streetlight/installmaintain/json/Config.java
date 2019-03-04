package com.terragoedge.streetlight.installmaintain.json;

import com.terragoedge.streetlight.installmaintain.json.Prop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Config {

    private String formTemplateGuid;
    private String name;
    private List<Prop> props = new ArrayList<>();


    public String getFormTemplateGuid() {
        return formTemplateGuid;
    }

    public void setFormTemplateGuid(String formTemplateGuid) {
        this.formTemplateGuid = formTemplateGuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Prop> getProps() {
        return props;
    }

    public void setProps(List<Prop> props) {
        this.props = props;
    }



    @Override
    public String toString() {
        return "Config{" +
                "formTemplateGuid='" + formTemplateGuid + '\'' +
                ", name='" + name + '\'' +
                ", props=" + props +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return Objects.equals(formTemplateGuid, config.formTemplateGuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formTemplateGuid);
    }
}
