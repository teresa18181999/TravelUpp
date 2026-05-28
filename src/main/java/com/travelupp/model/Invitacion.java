package com.travelupp.model;

import java.util.ArrayList;
import java.util.List;

public class Invitacion {

    private String id;
    private int viajeId;
    private String tripName;
    private String dates;
    private String invitedBy;
    private List<String> participants = new ArrayList<>();
    private String image;
    private String type;
    private String status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getViajeId() { return viajeId; }
    public void setViajeId(int viajeId) { this.viajeId = viajeId; }

    public String getTripName() { return tripName; }
    public void setTripName(String tripName) { this.tripName = tripName; }

    public String getDates() { return dates; }
    public void setDates(String dates) { this.dates = dates; }

    public String getInvitedBy() { return invitedBy; }
    public void setInvitedBy(String invitedBy) { this.invitedBy = invitedBy; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
