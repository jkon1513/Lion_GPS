package ase.liongps.MapOverlay;

import ase.liongps.utils.Building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SearchInteractor {
    private HashMap<String, Building> buildings;

    public SearchInteractor() {
        buildings = new HashMap<>();
    }


    public void populateBuildings(String fileEntry) {
        String[] bldngData = fileEntry.split("\t");

        String name = bldngData[0];
        double lat = Double.parseDouble(bldngData[1]);
        double lng = Double.parseDouble(bldngData[2]);

        buildings.put(name, new Building(name, lat, lng));
    }

    public boolean isValidSearch(String query) {
        return buildings.containsKey(query.toLowerCase().trim());
    }

    //returns the requested building object, error handling in presenter
    public Building getBuilding(String name) {
        return buildings.get(name);
    }

    public ArrayList<String> getValidBuildings() {
        return new ArrayList<>(buildings.keySet());
    }


    /*
    reads from a text file that stores the buildings and their coordinates. each line
    contains a building name, latitude, and longitude separated by tabs. this method
    adds each entry to the buildings hashmap using names as keys and Building objects as vals.
    */
    protected void readBuildingData() {
        String file = "res/raw/buildings.txt";
        Scanner scan = new Scanner(this.getClass().getClassLoader().getResourceAsStream(file));

        while (scan.hasNextLine()) {
            String entry = scan.nextLine();
            populateBuildings(entry);
        }

    }
}