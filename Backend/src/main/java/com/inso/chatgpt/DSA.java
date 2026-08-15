package com.inso.chatgpt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DSA {

    //Question 1: Give me a list of incidents

    Map<String, Integer> countIncidentsByCountry(List<Incident> incidents){
        Map<String, Integer> incidentsByCountry = new HashMap<>();

        incidents.forEach(e->{
            incidentsByCountry.put(e.getCountry(), incidentsByCountry.getOrDefault(e.getCountry(),0)+1);
        });

        incidentsByCountry.forEach((x,y)->{
            System.out.println("country: "+x+" count: "+y);
        });
        return incidentsByCountry;
    }

}
