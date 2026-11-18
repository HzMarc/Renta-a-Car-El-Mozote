package com.example.el_mozote.reservations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Inspection implements Serializable {

    public static final String TYPE_DEPARTURE = "SALIDA";
    public static final String TYPE_RETURN = "REGRESO";

    private long id;
    private long reservationId;
    private String type;
    private int mileage;
    private String fuelLevel;
    private String exteriorState;
    private String interiorState;
    private boolean spareTire;
    private boolean tools;
    private String damages;
    private String observations;
    private List<String> photoPaths;
    private long createdAt;

    public Inspection() {
        this.photoPaths = new ArrayList<>();
        this.spareTire = true;
        this.tools = true;
        this.createdAt = System.currentTimeMillis();
    }

    public Inspection(long id, long reservationId, String type, int mileage, String fuelLevel,
                      String exteriorState, String interiorState, boolean spareTire, boolean tools,
                      String damages, String observations, List<String> photoPaths, long createdAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.type = type;
        this.mileage = mileage;
        this.fuelLevel = fuelLevel;
        this.exteriorState = exteriorState;
        this.interiorState = interiorState;
        this.spareTire = spareTire;
        this.tools = tools;
        this.damages = damages;
        this.observations = observations;
        this.photoPaths = (photoPaths != null) ? new ArrayList<>(photoPaths) : new ArrayList<>();
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public String getType() {
        return type != null ? type : TYPE_DEPARTURE;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public String getFuelLevel() {
        return fuelLevel != null ? fuelLevel : "";
    }

    public void setFuelLevel(String fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public String getExteriorState() {
        return exteriorState != null ? exteriorState : "";
    }

    public void setExteriorState(String exteriorState) {
        this.exteriorState = exteriorState;
    }

    public String getInteriorState() {
        return interiorState != null ? interiorState : "";
    }

    public void setInteriorState(String interiorState) {
        this.interiorState = interiorState;
    }

    public boolean isSpareTire() {
        return spareTire;
    }

    public void setSpareTire(boolean spareTire) {
        this.spareTire = spareTire;
    }

    public boolean isTools() {
        return tools;
    }

    public void setTools(boolean tools) {
        this.tools = tools;
    }

    public String getDamages() {
        return damages != null ? damages : "";
    }

    public void setDamages(String damages) {
        this.damages = damages;
    }

    public String getObservations() {
        return observations != null ? observations : "";
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public List<String> getPhotoPaths() {
        if (photoPaths == null) {
            photoPaths = new ArrayList<>();
        }
        return photoPaths;
    }

    public void setPhotoPaths(List<String> photoPaths) {
        this.photoPaths = (photoPaths != null) ? new ArrayList<>(photoPaths) : new ArrayList<>();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean hasPhotos() {
        return photoPaths != null && !photoPaths.isEmpty();
    }
}
