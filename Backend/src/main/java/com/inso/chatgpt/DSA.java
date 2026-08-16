package com.inso.chatgpt;

import java.time.Duration;
import java.util.*;

public class DSA {
    Map<String, Integer> incidentsByCountry = new HashMap<>();
    //Question 1: Give me a list of incidents

    Map<String, Integer> countIncidentsByCountry(List<Incident> incidents) {
        incidents.forEach(e -> {
            incidentsByCountry.put(e.getCountry(), incidentsByCountry.getOrDefault(e.getCountry(), 0) + 1);
        });
        incidentsByCountry.forEach((x, y) -> {
            System.out.println("country: " + x + " count: " + y);
        });
        return incidentsByCountry;
    }

    List<String> topThreeCountries(List<Incident> incidents) {
        return incidentsByCountry
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .map(e->e.getKey())
                .toList()
                .subList(incidentsByCountry.size()-3, incidentsByCountry.size());

    }

    //Question 2: Remove duplicates
    List<Incident> removeDuplicates(List<Incident> incidents){
        Map<String, List<Incident>> incidentsByIncidentCategory = new HashMap<>();
        List<Incident> distinctIncidents = new ArrayList<>();

        for(Incident incident: incidents){

            if(!incidentsByIncidentCategory.containsKey(incident.getCategory())){
                distinctIncidents.add(incident);
                incidentsByIncidentCategory.put(incident.getCategory(), new ArrayList<>(List.of(incident)));
            }else {
                boolean toRemove = false;
                for (Incident existingIncident : incidentsByIncidentCategory.get(incident.getCategory())) {
                    Duration difference = Duration.between(existingIncident.getTimestamp(), incident.getTimestamp());
                    if (difference.toMinutes() <= 300) {
                        toRemove = true;
                    }
                }
                if (!toRemove) {
                    List<Incident> temp = incidentsByIncidentCategory.get(incident.getCategory());
                    temp.add(incident);
                    incidentsByIncidentCategory.put(incident.getCategory(), temp);
                    distinctIncidents.add(incident);

                }
            }
        }

        return distinctIncidents;
    }


}
