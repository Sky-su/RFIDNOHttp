package com.example.rfidnohttp;

import androidx.annotation.NonNull;

public class ParamEntity {
   String Epc;

    public ParamEntity() {
    }

    public ParamEntity(String epc) {
        Epc = epc;
    }

    public String getEpc() {
        return Epc;
    }

    public void setEpc(String epc) {
        Epc = epc;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
