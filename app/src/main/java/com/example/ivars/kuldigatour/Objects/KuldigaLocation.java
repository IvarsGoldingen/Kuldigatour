package com.example.ivars.kuldigatour.Objects;

import java.io.Serializable;

public class KuldigaLocation implements Serializable {
    //String keys for passing as extra
    public static final String NAME_KEY = "location_name";
    public static final String DESCRIPTION_KEY = "location_description";
    public static final String HIDDEN_NAME_KEY = "location_hidden_name";
    public static final String HIDDEN_DESCRIPTION_KEY = "location_hidden_description";
    public static final String WORKING_HOURS_KEY = "location_working_hours";
    public static final String COORDINATES_KEY = "location_coordinates";
    public static final String LARGE_IMAGE_KEY = "large_image";
    public static final String SMALL_IMAGE_KEY = "small_image";
    public static final String HIDDEN_LARGE_IMAGE_KEY = "hidden_large_image";
    public static final String HIDDEN_SMALL_IMAGE_KEY = "hidden_small_image";
    public static final String DISTANCE_KEY = "location_distance";
    private String coordinates;
    private String discoveredDescription;
    private String discoveredName;
    private String hiddenDescription;
    private String hiddenName;
    private String workingHours;
    private String largeImageUrl;
    private String smallImageUrl;
    private String hiddenLargeImageUrl;
    private String hiddenSmallImageUrl;
    private Double distance;


    //necessary for Firebase
    public KuldigaLocation() {
    }

    /*
     * Distance is not used in the constructor since it is calculated after creation of the objject
     * */

    public KuldigaLocation(String coordinates,
                           String discoveredDescription,
                           String discoveredName,
                           String hiddenDescription,
                           String hiddenName,
                           String workingHours,
                           String largeImageUrl,
                           String smallImageUrl,
                           String hiddenSmallImageUrl,
                           String hiddenLargeImageUrl) {
        this.coordinates = coordinates;
        this.discoveredDescription = discoveredDescription;
        this.discoveredName = discoveredName;
        this.hiddenDescription = hiddenDescription;
        this.hiddenName = hiddenName;
        this.workingHours = workingHours;
        this.largeImageUrl = largeImageUrl;
        this.smallImageUrl = smallImageUrl;
        this.hiddenSmallImageUrl = hiddenSmallImageUrl;
        this.hiddenLargeImageUrl = hiddenLargeImageUrl;
    }

    public String getLargeImageUrl() {
        return largeImageUrl;
    }

    public void setLargeImageUrl(String largeImageUrl) {

        this.largeImageUrl = largeImageUrl;
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getDiscoveredDescription() {
        return discoveredDescription;
    }

    public void setDiscoveredDescription(String discoveredDescription) {
        this.discoveredDescription = discoveredDescription;
    }

    public String getDiscoveredName() {
        return discoveredName;
    }

    public void setDiscoveredName(String discoveredName) {
        this.discoveredName = discoveredName;
    }

    public String getHiddenDescription() {
        return hiddenDescription;
    }

    public void setHiddenDescription(String hiddenDescription) {
        this.hiddenDescription = hiddenDescription;
    }

    public String getHiddenName() {
        return hiddenName;
    }

    public void setHiddenName(String hiddenName) {
        this.hiddenName = hiddenName;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getHiddenLargeImageUrl() {
        return hiddenLargeImageUrl;
    }

    public void setHiddenLargeImageUrl(String hiddenLargeImageUrl) {
        this.hiddenLargeImageUrl = hiddenLargeImageUrl;
    }

    public String getHiddenSmallImageUrl() {
        return hiddenSmallImageUrl;
    }

    public void setHiddenSmallImageUrl(String hiddenSmallImageUrl) {
        this.hiddenSmallImageUrl = hiddenSmallImageUrl;
    }
}
