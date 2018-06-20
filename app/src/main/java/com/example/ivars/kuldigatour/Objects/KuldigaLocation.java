package com.example.ivars.kuldigatour.Objects;

public class KuldigaLocation {
    private String coordinates;
    private String discoveredDescription;
    private String discoveredName;
    private String hiddenDescription;
    private String hiddenName;
    private String workingHours;
    private String largeImageUrl;
    private String smallImageUrl;
    private Double distance;

    //String keys for passing as extra
    public static final String NAME_KEY = "location_name";
    public static final String DESCRIPTION_KEY = "location_description";
    public static final String HIDDEN_NAME_KEY = "location_hidden_name";
    public static final String HIDDEN_DESCRIPTION_KEY = "location_hidden_description";
    public static final String WORKING_HOURS_KEY = "location_working_hours";
    public static final String COORDINATES_KEY = "location_coordinates";
    public static final String LARGE_IMAGE_KEY = "large_image";
    public static final String SMALL_IMAGE_KEY = "small_image";


    //necessary for Firebase
    public KuldigaLocation() {
    }

    public KuldigaLocation(String coordinates,
                           String discoveredDescription,
                           String discoveredName,
                           String hiddenDescription,
                           String hiddenName,
                           String workingHours,
                           String largeImageUrl,
                           String smallImageUrl) {
        this.coordinates = coordinates;
        this.discoveredDescription = discoveredDescription;
        this.discoveredName = discoveredName;
        this.hiddenDescription = hiddenDescription;
        this.hiddenName = hiddenName;
        this.workingHours = workingHours;
        this.largeImageUrl = largeImageUrl;
        this.smallImageUrl = smallImageUrl;
    }

    public String getLargeImageUrl() {
        return largeImageUrl;
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }


    //TODO: delete
    public String toString(){
        return  coordinates + "\n" +
                discoveredDescription + "\n" +
                discoveredName + "\n" +
                hiddenDescription + "\n" +
                hiddenName + "\n" +
                workingHours + "\n" +
                largeImageUrl + "\n" +
                smallImageUrl + "\n";
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getDiscoveredDescription() {
        return discoveredDescription;
    }

    public String getDiscoveredName() {
        return discoveredName;
    }

    public String getHiddenDescription() {
        return hiddenDescription;
    }

    public String getHiddenName() {
        return hiddenName;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public Double getDistance() {
        return distance;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public void setDiscoveredDescription(String discoveredDescription) {
        this.discoveredDescription = discoveredDescription;
    }

    public void setDiscoveredName(String discoveredName) {
        this.discoveredName = discoveredName;
    }

    public void setHiddenDescription(String hiddenDescription) {
        this.hiddenDescription = hiddenDescription;
    }

    public void setHiddenName(String hiddenName) {
        this.hiddenName = hiddenName;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public void setLargeImageUrl(String largeImageUrl) {

        this.largeImageUrl = largeImageUrl;
    }

    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
