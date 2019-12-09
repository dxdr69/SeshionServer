package com.seshion;

import java.util.List;

public interface UserAccountDao {
    public int createNewUser(UserAccount user);
    public int userLogIn(UserAccount user);
    public int changeUserVisibility(UserAccount user);
    public int userLogOut(String username);
    public int setUserCoordinates(String username, double latitude, double longitude);
    public List<Double> getUserCoordinates(String username);
    public List<UserAccount> searchForFriend(String userToFind);
    public int sendFriendRequest(String theUser, String friendUsername);
    public int manageFriendRequest(String theUser, String senderUsername, boolean requestAccepted);
    public int removeFriend(String theUser, String friendUsername);
    public List<UserAccount> getPendingFriendRequests(String username);
    public List<UserAccount> getFriends(String username);
    public List<UserSession> getOwnedSessions(String username);
    public List<UserSession> getInvitedSessions(String username);
    public List<UserSession> getJoinedSessions(String username);
    public List<UserGroup> getOwnedGroups(String username);
    public List<UserGroup> getJoinedGroups(String username);
    public List<Message> getUserMessages(String username);
}