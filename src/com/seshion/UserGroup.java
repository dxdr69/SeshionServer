package com.seshion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserGroup {
    private UUID groupID;
    private String groupName;
    private String owner;
    private List<String> groupMembers;
    private List<Message> messages;

    UserGroup(String groupName, String owner)
    {
        groupID = UUID.randomUUID();
        this.groupName = groupName;
        this.owner = owner;
        groupMembers = new ArrayList<String>();
        groupMembers.add(getOwnerUsername());
        messages = new ArrayList<Message>();
    }

    UserGroup(String groupName, String owner, List<String> groupMembers)
    {
        groupID = null;
        this.groupName = groupName;
        this.owner = owner;
        this.groupMembers = groupMembers;
        this.messages = null;
    }

    public void setID(UUID id)
    {
        groupID = id;
    }

    public UUID getID()
    {
        return groupID;
    }

    public void setName(String name)
    {
        groupName = name;
    }

    public String getName()
    {
        return groupName;
    }

    public String getOwnerUsername()
    {
        return owner;
    }

    public void addGroupMember(String username)
    {
        groupMembers.add(username);
    }

    public void removeGroupMember(String username)
    {
        groupMembers.remove(username);
    }

    public void setAllGroupMembers(List<String> members)
    {
        groupMembers = members;
    }

    public List<String> getAllGroupMembers()
    {
        return groupMembers;
    }

    public void addMessage(Message msg)
    {
        messages.add(msg);
    }

    public List<Message> getAllMessages()
    {
        return messages;
    }

}