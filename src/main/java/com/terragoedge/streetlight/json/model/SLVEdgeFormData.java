package com.terragoedge.streetlight.json.model;

import com.terragoedge.edgeserver.EdgeFormData;

import java.util.Objects;

public class SLVEdgeFormData extends EdgeFormData {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeFormData that = (EdgeFormData) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
