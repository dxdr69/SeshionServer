package com.seshion;

import java.util.List;
import java.util.UUID;

public interface UserSessionDao {
    public int createNewSession(UserSession sesh);
    public int cancelSession(UserSession sesh);
    public int endSession(UUID sessionID);
    public int inviteSessionUser(UUID sessionID, String username);
    public int checkInSessionUser(UUID sessionID, String username);
    public List<String> getAllSessionUsers(UserSession sesh);
    public List<UserSession> getAllOpenSessions();
}