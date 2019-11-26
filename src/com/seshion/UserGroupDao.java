package com.seshion;

import java.util.List;
import java.util.UUID;

public interface UserGroupDao {
    public int createNewGroup(UserGroup group);
    public int deleteGroup(UserGroup group);
    public int addGroupMember(UUID groupID, String memberUsername);
    public int removeGroupMember(UUID groupID, String memberUsername);
    public List<String> getGroupMembers(UUID groupID);
    public int changeGroupName(UUID groupID, String newName);
    public List<Message> getGroupMessages(UUID groupID);
}