package com.example.kurzlistok;

import java.util.HashMap;

public class Listok {
    private HashMap<String, Double> hodnoty = new HashMap<String, Double>();
    public void Listok(HashMap<String, Double> hodnoty){
        this.hodnoty = hodnoty;
    }

    public Double getHodnota(String mena){
        return hodnoty.containsKey(mena) ? hodnoty.get(mena) : Double.valueOf(0);
    }

    public HashMap<String, Double> getHodnoty() {
        return hodnoty;
    }

    public void setHodnoty(HashMap<String, Double> hodnoty) {
        this.hodnoty = hodnoty;
    }
}
