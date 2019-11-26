package com.seshion;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class UserSession {
    private UUID sessionID;
    private String name;
    private String owner;
    private String description;
    private double latitudeTopLeft;
    private double longitudeTopLeft;
    private double latitudeTopRight;
    private double longitudeTopRight;
    private double latitudeBottomLeft;
    private double longitudeBottomLeft;
    private double latitudeBottomRight;
    private double longitudeBottomRight;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isSessionPrivate;
    private boolean hasEnded;
    private List<String> invitedUsers;
    private List<String> showedUpUsers;

    UserSession(String name, String owner, double latitudeTopLeft,
    double longitudeTopLeft, double latitudeTopRight, double longitudeTopRight,
    double latitudeBottomLeft, double longitudeBottomLeft, double latitudeBottomRight,
    double longitudeBottomRight, LocalDate startDate, LocalTime startTime,
    boolean isSessionPrivate)
    {
        sessionID = UUID.randomUUID();
        this.name = name;
        this.owner = owner;
        description = "";
        this.latitudeTopLeft = latitudeTopLeft;
        this.longitudeTopLeft = longitudeTopLeft;
        this.latitudeTopRight = latitudeTopRight;
        this.longitudeTopRight = longitudeTopRight;
        this.latitudeBottomLeft = latitudeBottomLeft;
        this.longitudeBottomLeft = longitudeBottomLeft;
        this.latitudeBottomRight = latitudeBottomRight;
        this.longitudeBottomRight = longitudeBottomRight;
        this.startDate = startDate;
        endDate = null;
        this.startTime = startTime;
        endTime = null;
        this.isSessionPrivate = isSessionPrivate;
        hasEnded = false;
        invitedUsers = new ArrayList<String>();
        showedUpUsers = new ArrayList<String>();
    }

    public void setID(UUID id)
    {
        sessionID = id;
    }

    public UUID getID()
    {
        return sessionID;
    }

    public void setName(String newName)
    {
        name = newName;
    }

    public String getName()
    {
        return name;
    }

    public String getOwnerUsername()
    {
        return owner;
    }

    public void setDescription(String desc)
    {
        description = desc;
    }

    public String getDescription()
    {
        return description;
    }

    public void setLatitudeTopLeft(double lat)
    {
        latitudeTopLeft = lat;
    }

    public double getLatitudeTopLeft()
    {
        return latitudeTopLeft;
    }

    public void setLongitudeTopLeft(double lon)
    {
        longitudeTopLeft = lon;
    }

    public double getLongitudeTopLeft()
    {
        return longitudeTopLeft;
    }

    public void setLatitudeTopRight(double lat)
    {
        latitudeTopRight = lat;
    }

    public double getLatitudeTopRight()
    {
        return latitudeTopRight;
    }

    public void setLongitudeTopRight(double lon)
    {
        longitudeTopRight = lon;
    }

    public double getLongitudeTopRight()
    {
        return longitudeTopRight;
    }

    public void setLatitudeBottomLeft(double lat)
    {
        latitudeBottomLeft = lat;
    }

    public double getLatitudeBottomLeft()
    {
        return latitudeBottomLeft;
    }

    public void setLongitudeBottomLeft(double lon)
    {
        longitudeBottomLeft = lon;
    }

    public double getLongitudeBottomLeft()
    {
        return longitudeBottomLeft;
    }

    public void setLatitudeBottomRight(double lat)
    {
        latitudeBottomRight = lat;
    }

    public double getLatitudeBottomRight()
    {
        return latitudeBottomRight;
    }

    public void setLongitudeBottomRight(double lon)
    {
        longitudeBottomRight = lon;
    }

    public double getLongitudeBottomRight()
    {
        return longitudeBottomRight;
    }

    public void setStartDate(LocalDate date)
    {
        startDate = date;
    }

    public LocalDate getStartDate()
    {
        return startDate;
    }

    public void setEndDate(LocalDate date)
    {
        endDate = date;
    }

    public LocalDate getEndDate()
    {
        return endDate;
    }

    public void setStartTime(LocalTime time)
    {
        startTime = time;
    }

    public LocalTime getStartTime()
    {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        String startTimeText = timeFormat.format(startTime);
        return LocalTime.parse(startTimeText, timeFormat);
    }

    public void setEndTime(LocalTime time)
    {
        endTime = time;
    }

    public LocalTime getEndTime()
    {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        String endTimeText = timeFormat.format(endTime);
        return LocalTime.parse(endTimeText, timeFormat);
    }

    public void changeSessionVisibility()
    {
        if (isSessionPrivate)
        {
            isSessionPrivate = false;
        }
        else
        {
            isSessionPrivate = true;
        }
    }

    public boolean isSessionPrivate()
    {
        return isSessionPrivate;
    }

    public boolean isSessionOpen()
    {
        return hasEnded;
    }

    public void endSession()
    {
        hasEnded = true;
    }

    public void inviteUser(String username)
    {
        invitedUsers.add(username);
    }

    public List<String> getInvitedUsers()
    {
        return invitedUsers;
    }

    public void joinUser(String username)
    {
        showedUpUsers.add(username);
    }

    public List<String> getShowedUpUsers()
    {
        return showedUpUsers;
    }
    
}