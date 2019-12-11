package com.seshion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

public class DBManager implements UserAccountDao, UserGroupDao, UserSessionDao, MessageDao {
    // Database login credentials
    private final String url = "jdbc:postgresql://localhost:5432/seshiondb";
    private final String user = "postgres";
    private final String password = "Lgn@Psql";

    // Empty constructor
    DBManager() {}

    // Connect to the database
    private Connection connectToDB() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /// User operations ///

    public int createNewUser(UserAccount user)
    {
        String username = user.getUserName();
        String passwordToHash = user.getPassword();
        int creationSuccessful = -1;

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        int affectedRows = 0;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt);
            byte[] hashedPassword = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
            String hashedPasswordText = Base64.getEncoder().encodeToString(hashedPassword);

            String SQL = "INSERT INTO useraccount (username, password, salt, isvisibilityprivate, isonline) "
            + "VALUES (?,?,?,?,?)";

            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPasswordText);
            pstmt.setBytes(3, salt);
            pstmt.setBoolean(4, false);
            pstmt.setBoolean(5, true);
            
            affectedRows = pstmt.executeUpdate();
            System.out.println("affectedRows:" + affectedRows);
            pstmt.close();
            conn.close();
            
            if (affectedRows == 1)
            {
            	creationSuccessful = 1;
            }

        } catch(NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
            
        } catch(SQLException e) {
        	
        	// Username already taken
            if (e.getMessage().startsWith("ERROR: duplicate key value violates unique constraint")) 
            {
        		creationSuccessful = 0;
            } 
            // Some other error
            else 
            {
        		creationSuccessful = -1;
            }
            
            System.out.println(e.getMessage());
            
            return creationSuccessful;
        }
        
        return creationSuccessful;
    }

    public int userLogIn(UserAccount user)
    {
        String username = user.getUserName();
        String passwordToHash = user.getPassword();
        int loginSuccessful = -1;
        byte[] salt = new byte[16];

        try {
            String SQL = "SELECT salt FROM useraccount WHERE username = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);

            ResultSet saltRs = pstmt.executeQuery();

            if (!saltRs.next())
            {
                // User does not exist
                pstmt.close();
                conn.close();
                loginSuccessful = 0;
                return loginSuccessful;
            }

            salt = saltRs.getBytes("salt");
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt);
            byte[] hashedPassword = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
            String hashedPasswordText = Base64.getEncoder().encodeToString(hashedPassword);

            SQL = "SELECT password FROM useraccount WHERE username = ?";
            pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);

            ResultSet passRs = pstmt.executeQuery();
            passRs.next();
            String passInDB = passRs.getString("password");

            if (hashedPasswordText.equals(passInDB))
            {
                // Login successful
                SQL = "UPDATE useraccount SET isonline = ? WHERE username = ?";
                pstmt = conn.prepareStatement(SQL);
                pstmt.setBoolean(1, true);
                pstmt.setString(2, username);
                pstmt.executeUpdate();
                pstmt.close();
                conn.close();
                loginSuccessful = 1;
            }
            else
            {
                // Password is incorrect
                loginSuccessful = 0;
                pstmt.close();
                conn.close();
            }
            
        } catch(NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return loginSuccessful;
    }

    public int changeUserVisibility(UserAccount user)
    {
        String username = user.getUserName();
        boolean isProfilePrivate = user.isProfilePrivate();
        int changeVisibilitySuccessful = -1;

        try {
            String SQL = "UPDATE useraccount SET isvisibilityprivate = ? WHERE username = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);

            if (isProfilePrivate)
            {
                pstmt.setBoolean(1, false);
            }
            else
            {
                pstmt.setBoolean(1, true);
            }

            pstmt.setString(2, username);
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (affectedRows == 1)
            {
                changeVisibilitySuccessful = 1;
            }
            else
            {
                changeVisibilitySuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return changeVisibilitySuccessful;
    }

    public int userLogOut(String username)
    {
        int logoutSuccessful = -1;

        try {
            String SQL = "UPDATE useraccount SET isonline = false WHERE username = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (affectedRows == 1)
            {
                // Logout successful
                logoutSuccessful = 1;
            }
            else
            {
                // Logout failed
                logoutSuccessful = 0;
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return logoutSuccessful;
    }

    public int setUserCoordinates(String username, double latitude, double longitude)
    {
        int setCoordinatesSuccessful = -1;

        try {
            String SQL = "UPDATE useraccount SET latitude = ?, longitude = ? WHERE username = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setDouble(1, latitude);
            pstmt.setDouble(2, longitude);
            pstmt.setString(3, username);

            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (affectedRows == 1)
            {
                // Set coordinates successful
                setCoordinatesSuccessful = 1;
            }
            else
            {
                // Set coordinates failed
                setCoordinatesSuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return setCoordinatesSuccessful;
    }

    public List<Double> getUserCoordinates(String username) 
    {
        List<Double> coordinates = new ArrayList<Double>(2);

        try {
            String SQL = "SELECT latitude, longitude FROM useraccount WHERE username = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next())
            {
                coordinates.add(rs.getDouble("latitude"));
                coordinates.add(rs.getDouble("longitude"));
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return coordinates;
    }

    public List<UserAccount> searchForFriend(String userToFind)
    {
        List<UserAccount> foundUsers = new ArrayList<UserAccount>();

        try {
            String SQL = "SELECT username, isonline, description "
            + "FROM useraccount "
            + "WHERE username LIKE ('' || ?) "
            + "ORDER BY username";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, userToFind + "%");

            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                String username = rs.getString("username");
                boolean isOnline = rs.getBoolean("isonline");
                String description = rs.getString("description");

                UserAccount user = new UserAccount(username, isOnline);
                user.setDescription(description);

                foundUsers.add(user);
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return foundUsers;
    }

    public int sendFriendRequest(String theUser, String friendUserName)
    {
        int addFriendSuccessful = -1;

        try {
            String SQL = "INSERT INTO friendswith VALUES (?,?,?), (?,?,?)";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, theUser);
            pstmt.setString(2, friendUserName);
            pstmt.setBoolean(3, false);
            pstmt.setString(4, friendUserName);
            pstmt.setString(5, theUser);
            pstmt.setBoolean(6, false);

            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (affectedRows == 2)
            {
                // Send friend request successful
                addFriendSuccessful = 1;
            }
            else
            {
                // Send friend request failed
                addFriendSuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return addFriendSuccessful;
    }

    public int manageFriendRequest(String theUser, 
    String senderUsername, boolean requestAccepted) 
    {
        int manageRequestSuccessful = -1;

        try {
            if (requestAccepted)
            {
                String SQL = "UPDATE friendswith SET isfriendrequestaccepted = true "
                + "WHERE theuser = ? AND friend = ?";
                Connection conn = connectToDB();
                PreparedStatement pstmt = conn.prepareStatement(SQL);
                pstmt.setString(1, theUser);
                pstmt.setString(2, senderUsername);

                int currentlyAffectedRows = pstmt.executeUpdate();

                pstmt.setString(1, senderUsername);
                pstmt.setString(2, theUser);

                int totalAffectedRows = currentlyAffectedRows + pstmt.executeUpdate();
                pstmt.close();
                conn.close();

                if (totalAffectedRows == 2)
                {
                    manageRequestSuccessful = 1;
                }
                else
                {
                    manageRequestSuccessful = 0;
                }
            }
            else
            {
                manageRequestSuccessful = 1;
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return manageRequestSuccessful;
    }

    public int removeFriend(String theUser, String friendUsername)
    {
        int removeFriendSuccessful = -1;

        try {
            String SQL = "DELETE FROM friendswith WHERE theuser = ? AND friend = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, theUser);
            pstmt.setString(2, friendUsername);

            int currentlyAffectedRows = pstmt.executeUpdate();

            pstmt.setString(1, friendUsername);
            pstmt.setString(2, theUser);

            int totalAffectedRows = currentlyAffectedRows + pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (totalAffectedRows == 2)
            {
                removeFriendSuccessful = 1;
            }
            else
            {
                removeFriendSuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return removeFriendSuccessful;
    }
    
    public List<UserAccount> getPendingFriendRequests(String username)
    {
        List<UserAccount> listOfRequests = new ArrayList<UserAccount>();

        try {
            String SQL = "SELECT username, isonline, description "
            + "FROM useraccount "
            + "JOIN friendswith "
            + "ON username = friend "
            + "WHERE theuser = ? AND isfriendrequestaccepted = ? "
            + "ORDER BY username";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);
            pstmt.setBoolean(2, false);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                String requestUsername = rs.getString("username");
                boolean isOnline = rs.getBoolean("isonline");
                String description = rs.getString("description");
                
                UserAccount requester = new UserAccount(requestUsername, isOnline);
                requester.setDescription(description);

                listOfRequests.add(requester);
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return listOfRequests;
    }

    public List<UserAccount> getFriends(String username)
    {
        List<UserAccount> listOfFriends = new ArrayList<UserAccount>();

        try {
            String SQL = "SELECT username, latitude, longitude, isonline, isvisibilityprivate, description "
            + "FROM useraccount "
            + "JOIN friendswith "
            + "ON username = friend "
            + "WHERE theuser = ? AND isfriendrequestaccepted = ? "
            + "ORDER BY username";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);
            pstmt.setBoolean(2, true);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                String friendUsername = rs.getString("username");
                double currentLatitude = rs.getDouble("latitude");
                double currentLongitude = rs.getDouble("longitude");
                boolean isOnline = rs.getBoolean("isonline");
                boolean isVisibilityPrivate = rs.getBoolean("isvisibilityprivate");
                String description = rs.getString("description");
                List<UserSession> joinedSessions = getJoinedSessions(friendUsername);
                List<UserSession> ownedSessions = getOwnedSessions(friendUsername);
                
                UserAccount friend = new UserAccount(friendUsername, currentLatitude,
                currentLongitude, isOnline, isVisibilityPrivate, joinedSessions,
                ownedSessions);
                friend.setDescription(description);

                listOfFriends.add(friend);
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return listOfFriends;
    }

    public List<UserSession> getOwnedSessions(String username)
    {
        List<UserSession> ownedSessions = new ArrayList<UserSession>();

        try {
            String SQL = "SELECT * FROM usersession "
            + "WHERE owner = ? AND hasended = ? "
            + "ORDER BY startdate DESC, starttime DESC";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);
            pstmt.setBoolean(2, false);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                UUID sessionID = UUID.fromString(rs.getString("sid"));
                String name = rs.getString("name");
                String owner = rs.getString("owner");
                String description = rs.getString("description");
                double latitudetopleft = rs.getDouble("latitudetopleft");
                double longitudetopleft = rs.getDouble("longitudetopleft");
                double latitudetopright = rs.getDouble("latitudetopright");
                double longitudetopright = rs.getDouble("longitudetopright");
                double latitudebottomleft = rs.getDouble("latitudebottomleft");
                double longitudebottomleft = rs.getDouble("longitudebottomleft");
                double latitudebottomright = rs.getDouble("latitudebottomright");
                double longitudebottomright = rs.getDouble("longitudebottomright");
                LocalDate startDate = LocalDate.parse(rs.getString("startdate"));
                LocalTime startTime = LocalTime.parse(rs.getString("starttime"));
                boolean isPrivate = rs.getBoolean("isprivate");

                LocalDate endDate;

                if (rs.getString("enddate") == null)
                {
                    endDate = null;
                }
                else
                {
                    endDate = LocalDate.parse(rs.getString("enddate"));
                }

                LocalTime endTime;
                
                if (rs.getString("endtime") == null)
                {
                    endTime = null;
                }
                else
                {
                    endTime = LocalTime.parse(rs.getString("endtime"));
                }

                SQL = "SELECT theuser FROM usersession_inviteduser WHERE sid = ? ORDER BY theuser";
                pstmt = conn.prepareStatement(SQL);
                pstmt.setObject(1, sessionID);

                ResultSet invRs = pstmt.executeQuery();
                List<String> invitedUsers = new ArrayList<String>();

                while (invRs.next())
                {
                    invitedUsers.add(invRs.getString("theuser"));
                }

                SQL = "SELECT theuser FROM usersession_showedupuser WHERE sid = ? ORDER BY theuser";
                pstmt = conn.prepareStatement(SQL);
                pstmt.setObject(1, sessionID);

                ResultSet showRs = pstmt.executeQuery();
                List<String> showedUpUsers = new ArrayList<String>();

                while (showRs.next())
                {
                    showedUpUsers.add(showRs.getString("theuser"));
                }
            
                UserSession session = new UserSession(name, owner, description,
                latitudetopleft, longitudetopleft, latitudetopright, longitudetopright,
                latitudebottomleft, longitudebottomleft, latitudebottomright, longitudebottomright,
                startDate, endDate, startTime, endTime, isPrivate, invitedUsers);
                session.setID(sessionID);
                session.setAllShowedUpUsers(showedUpUsers);
                
                ownedSessions.add(session);
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        } catch(DateTimeParseException e) {
            System.out.println(e.getMessage());
        }

        return ownedSessions;
    }

    public List<UserSession> getInvitedSessions(String username)
    {
        List<UserSession> invitedSessions = new ArrayList<UserSession>();

        try {
            String SQL = "SELECT * FROM usersession WHERE sid IN "
            + "(SELECT sid FROM usersession_inviteduser WHERE theuser = ?) "
            + "WHERE hasended = ? "
            + "ORDER BY startdate DESC, starttime DESC";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);
            pstmt.setBoolean(2, false);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next())
            {
                UUID sessionID = UUID.fromString(rs.getString("sid"));
                String name = rs.getString("name");
                String owner = rs.getString("owner");
                String description = rs.getString("description");
                double latitudetopleft = rs.getDouble("latitudetopleft");
                double longitudetopleft = rs.getDouble("longitudetopleft");
                double latitudetopright = rs.getDouble("latitudetopright");
                double longitudetopright = rs.getDouble("longitudetopright");
                double latitudebottomleft = rs.getDouble("latitudebottomleft");
                double longitudebottomleft = rs.getDouble("longitudebottomleft");
                double latitudebottomright = rs.getDouble("latitudebottomright");
                double longitudebottomright = rs.getDouble("longitudebottomright");
                LocalDate startDate = LocalDate.parse(rs.getString("startdate"));
                LocalTime startTime = LocalTime.parse(rs.getString("starttime"));
                boolean isPrivate = rs.getBoolean("isprivate");

                LocalDate endDate;

                if (rs.getString("enddate") == null)
                {
                    endDate = null;
                }
                else
                {
                    endDate = LocalDate.parse(rs.getString("enddate"));
                }

                LocalTime endTime;
                
                if (rs.getString("endtime") == null)
                {
                    endTime = null;
                }
                else
                {
                    endTime = LocalTime.parse(rs.getString("endtime"));
                }

                SQL = "SELECT theuser FROM usersession_inviteduser WHERE sid = ? ORDER BY theuser";
                pstmt = conn.prepareStatement(SQL);
                pstmt.setObject(1, sessionID);

                ResultSet invRs = pstmt.executeQuery();
                List<String> invitedUsers = new ArrayList<String>();

                while (invRs.next())
                {
                    invitedUsers.add(invRs.getString("theuser"));
                }

                SQL = "SELECT theuser FROM usersession_showedupuser WHERE sid = ? ORDER BY theuser";
                pstmt = conn.prepareStatement(SQL);
                pstmt.setObject(1, sessionID);

                ResultSet showRs = pstmt.executeQuery();
                List<String> showedUpUsers = new ArrayList<String>();

                while (showRs.next())
                {
                    showedUpUsers.add(showRs.getString("theuser"));
                }
            
                UserSession session = new UserSession(name, owner, description,
                latitudetopleft, longitudetopleft, latitudetopright, longitudetopright,
                latitudebottomleft, longitudebottomleft, latitudebottomright, longitudebottomright,
                startDate, endDate, startTime, endTime, isPrivate, invitedUsers);
                session.setID(sessionID);
                session.setAllShowedUpUsers(showedUpUsers);

                invitedSessions.add(session);
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return invitedSessions;
    }

    public List<UserSession> getJoinedSessions(String username)
    {
        List<UserSession> joinedSessions = new ArrayList<UserSession>();

        try {
            String SQL = "SELECT * FROM usersession WHERE sid IN "
            + "(SELECT sid from usersession_showedupuser WHERE theuser = ?) "
            + "WHERE hasended = ? "
            + "ORDER BY startdate DESC, starttime DESC";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);
            pstmt.setBoolean(2, false);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                UUID sessionID = UUID.fromString(rs.getString("sid"));
                String name = rs.getString("name");
                String owner = rs.getString("owner");
                String description = rs.getString("description");
                double latitudetopleft = rs.getDouble("latitudetopleft");
                double longitudetopleft = rs.getDouble("longitudetopleft");
                double latitudetopright = rs.getDouble("latitudetopright");
                double longitudetopright = rs.getDouble("longitudetopright");
                double latitudebottomleft = rs.getDouble("latitudebottomleft");
                double longitudebottomleft = rs.getDouble("longitudebottomleft");
                double latitudebottomright = rs.getDouble("latitudebottomright");
                double longitudebottomright = rs.getDouble("longitudebottomright");
                LocalDate startDate = LocalDate.parse(rs.getString("startdate"));
                LocalTime startTime = LocalTime.parse(rs.getString("starttime"));
                boolean isPrivate = rs.getBoolean("isprivate");

                LocalDate endDate;

                if (rs.getString("enddate") == null)
                {
                    endDate = null;
                }
                else
                {
                    endDate = LocalDate.parse(rs.getString("enddate"));
                }

                LocalTime endTime;
                
                if (rs.getString("endtime") == null)
                {
                    endTime = null;
                }
                else
                {
                    endTime = LocalTime.parse(rs.getString("endtime"));
                }

                SQL = "SELECT theuser FROM usersession_inviteduser WHERE sid = ? ORDER BY theuser";
                pstmt = conn.prepareStatement(SQL);
                pstmt.setObject(1, sessionID);

                ResultSet invRs = pstmt.executeQuery();
                List<String> invitedUsers = new ArrayList<String>();

                while (invRs.next())
                {
                    invitedUsers.add(invRs.getString("theuser"));
                }

                SQL = "SELECT theuser FROM usersession_showedupuser WHERE sid = ? ORDER BY theuser";
                pstmt = conn.prepareStatement(SQL);
                pstmt.setObject(1, sessionID);

                ResultSet showRs = pstmt.executeQuery();
                List<String> showedUpUsers = new ArrayList<String>();

                while (showRs.next())
                {
                    showedUpUsers.add(showRs.getString("theuser"));
                }
            
                UserSession session = new UserSession(name, owner, description,
                latitudetopleft, longitudetopleft, latitudetopright, longitudetopright,
                latitudebottomleft, longitudebottomleft, latitudebottomright, longitudebottomright,
                startDate, endDate, startTime, endTime, isPrivate, invitedUsers);
                session.setID(sessionID);
                session.setAllShowedUpUsers(showedUpUsers);

                joinedSessions.add(session);
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return joinedSessions;
    }

    public List<UserGroup> getOwnedGroups(String username)
    {
        List<UserGroup> ownedGroups = new ArrayList<UserGroup>();

        try {
            String SQL = "SELECT gid, name, owner FROM usergroup WHERE owner = ? ORDER BY name";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                UUID groupID = UUID.fromString(rs.getString("gid"));
                String name = rs.getString("name");
                String owner = rs.getString("owner");

                SQL = "SELECT usermember FROM usergroup_groupmember WHERE gid = ?";
                pstmt = conn.prepareStatement(SQL);
                pstmt.setObject(1, groupID);

                ResultSet memberRs = pstmt.executeQuery();
                List<String> groupMembers = new ArrayList<String>();

                while (memberRs.next())
                {
                    groupMembers.add(memberRs.getString("usermember"));
                }

                UserGroup group = new UserGroup(name, owner, groupMembers);
                group.setID(groupID);

                ownedGroups.add(group);
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return ownedGroups;
    }

    public List<UserGroup> getJoinedGroups(String username)
    {
        List<UserGroup> joinedGroups = new ArrayList<UserGroup>();

        try {
            String SQL = "SELECT gid, name, owner "
            + "FROM usergroup " 
            + "WHERE gid IN (SELECT gid FROM usergroup_groupmember WHERE usermember = ?) "
            + "AND owner <> ? "
            + "ORDER BY name";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);
            pstmt.setString(2, username);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                UUID groupID = UUID.fromString(rs.getString("gid"));
                String name = rs.getString("name");
                String owner = rs.getString("owner");

                SQL = "SELECT usermember FROM usergroup_groupmember WHERE gid = ?";
                pstmt = conn.prepareStatement(SQL);
                pstmt.setObject(1, groupID);

                ResultSet memberRs = pstmt.executeQuery();
                List<String> groupMembers = new ArrayList<String>();

                while (memberRs.next())
                {
                    groupMembers.add(memberRs.getString("usermember"));
                }

                UserGroup group = new UserGroup(name, owner, groupMembers);
                group.setID(groupID);

                joinedGroups.add(group);
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return joinedGroups;
    }

    public List<Message> getUserMessages(String username)
    {
        List<Message> userMessages = new ArrayList<Message>();

        try {
            String SQL = "SELECT message.*, recipient "
            + "FROM message "
            + "JOIN "
            + "message_recipient "
            + "ON message.mid = message_recipient.mid "
            + "WHERE (creator = ? OR recipient = ?) AND (isgroupmessage = ?) "
            + "ORDER BY datecreated ASC, timecreated ASC";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.setBoolean(3, false);
            
            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                UUID messageID = UUID.fromString(rs.getString("mid"));
                String creator = rs.getString("creator");
                String recipient = rs.getString("recipient");
                LocalDate dateCreated = LocalDate.parse(rs.getString("datecreated"));
                LocalTime timeCreated = LocalTime.parse(rs.getString("timecreated"));
                String messageContent = rs.getString("messagecontent");
                boolean isPrivateMsg = rs.getBoolean("isprivatemessage");
                boolean isGroupMsg = rs.getBoolean("isgroupmessage");

                Message msg = new Message(creator, recipient, dateCreated, timeCreated,
                messageContent, isPrivateMsg, isGroupMsg);
                msg.setID(messageID);

                userMessages.add(msg);
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return userMessages;
    }

    /// Group operations ///

    public int createNewGroup(UserGroup group)
    {
        UUID groupID = group.getID();
        String groupName = group.getName();
        String groupOwner = group.getOwnerUsername();
        int createGroupSuccessful = -1;

        try {
            String SQL = "INSERT INTO usergroup VALUES (?,?,?)";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, groupID);
            pstmt.setString(2, groupName);
            pstmt.setString(3, groupOwner);

            int currentlyAffectedRows = pstmt.executeUpdate();

            SQL = "INSERT INTO usergroup_groupmember VALUES (?,?)";
            pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, groupID);
            pstmt.setObject(2, groupOwner);

            int totalAffectedRows = currentlyAffectedRows + pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (totalAffectedRows == 2)
            {
                createGroupSuccessful = 1;
            }
            else
            {
                createGroupSuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return createGroupSuccessful;
    }

    public int deleteGroup(UserGroup group)
    {
        UUID groupID = group.getID();
        int deleteGroupSucessful = -1;

        try {
            String SQL = "DELETE FROM usergroup_groupmember WHERE gid = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, groupID);

            int currentlyAffectedRows = pstmt.executeUpdate();

            SQL = "DELETE FROM usergroup WHERE gid = ?";
            pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, groupID);

            int totalAffectedRows = currentlyAffectedRows + pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            int numOfDeletions = group.getAllGroupMembers().size() + 1;

            if (totalAffectedRows == numOfDeletions)
            {
                deleteGroupSucessful = 1;
            }
            else
            {
                deleteGroupSucessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return deleteGroupSucessful;
    }

    public int addGroupMember(UUID groupID, String memberUsername)
    {
        int addMemberSuccessful = -1;

        try {
            String SQL = "INSERT INTO usergroup_groupmember VALUES (?,?)";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, groupID);
            pstmt.setString(2, memberUsername);

            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (affectedRows == 1)
            {
                addMemberSuccessful = 1;
            }
            else
            {
                addMemberSuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return addMemberSuccessful;
    }

    public int removeGroupMember(UUID groupID, String memberUsername)
    {
        int removeMemberSuccessful = -1;

        try {
            String SQL = "DELETE FROM usergroup_groupmember WHERE gid = ? AND usermember = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, groupID);
            pstmt.setString(2, memberUsername);

            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (affectedRows == 1)
            {
                removeMemberSuccessful = 1;
            }
            else
            {
                removeMemberSuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return removeMemberSuccessful;
    }

    public List<String> getGroupMembers(UUID groupID)
    {
        List<String> groupMembers = new ArrayList<String>();

        try {
            String SQL = "SELECT usermember from usergroup_groupmember WHERE gid = ? ORDER BY usermember";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, groupID);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                groupMembers.add(rs.getString("usermember"));
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return groupMembers;
    }
    
    public int changeGroupName(UUID groupID, String newName)
    {
        int changeNameSuccessful = -1;

        try {
            String SQL = "UPDATE usergroup SET name = ? WHERE gid = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, newName);
            pstmt.setObject(2, groupID);

            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (affectedRows == 1)
            {
                changeNameSuccessful = 1;
            }
            else
            {
                changeNameSuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return changeNameSuccessful;
    }

    public List<Message> getGroupMessages(UUID groupID)
    {
        List<Message> groupMessages = new ArrayList<Message>();

        try {
            String SQL = "SELECT DISTINCT message.* "
            + "FROM message "
            + "JOIN "
            + "message_recipient "
            + "ON message.mid = message_recipient.mid "
            + "JOIN "
            + "usergroup_groupmember "
            + "ON usermember = creator "
            + "WHERE gid = ? AND isgroupmessage = ? "
            + "ORDER BY datecreated ASC, timecreated ASC";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, groupID);
            pstmt.setBoolean(2, true);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                UUID messageID = UUID.fromString(rs.getString("mid"));
                String creator = rs.getString("creator");
                String recipient = groupID.toString();
                LocalDate dateCreated = LocalDate.parse(rs.getString("datecreated"));
                LocalTime timeCreated = LocalTime.parse(rs.getString("timecreated"));
                String messageContent = rs.getString("messagecontent");
                boolean isPrivateMsg = rs.getBoolean("isprivatemessage");
                boolean isGroupMsg = rs.getBoolean("isgroupmessage");

                Message msg = new Message(creator, recipient, dateCreated, timeCreated,
                messageContent, isPrivateMsg, isGroupMsg);
                msg.setID(messageID);

                groupMessages.add(msg);
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return groupMessages;
    }

    /// Message operations ///

    public int sendMessageIndividual(Message msg, String recipient)
    {
        UUID messageID = msg.getID();
        String messageContent = msg.getMessage();
        boolean isMessagePrivate = msg.isPrivateMessage();
        boolean isGroupMessage = msg.isGroupMessage();
        String creator = msg.getCreatorUsername();
        LocalDate creationDate = msg.getCreationDate();
        LocalTime creationTime = msg.getCreationTime();
        int msgSucessful = -1;

        try {
            String SQL = "INSERT INTO message VALUES (?,?,?,?,?,?,?)";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, messageID);
            pstmt.setString(2, messageContent);
            pstmt.setBoolean(3, isMessagePrivate);
            pstmt.setBoolean(4, isGroupMessage);
            pstmt.setString(5, creator);
            pstmt.setObject(6, creationDate, Types.DATE);
            pstmt.setObject(7, creationTime, Types.TIME);

            int currentlyAffectedRows = pstmt.executeUpdate();

            SQL = "INSERT INTO message_recipient VALUES (?,?)";
            pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, messageID);
            pstmt.setString(2, recipient);

            int totalAffectedRows = currentlyAffectedRows + pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (totalAffectedRows == 2)
            {
                msgSucessful = 1;
            }
            else
            {
                msgSucessful = 0;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return msgSucessful;
    }

    public int sendMessageGroup(Message msg, UUID group)
    {
        UUID messageID = msg.getID();
        String messageContent = msg.getMessage();
        boolean isMessagePrivate = false;
        String creator = msg.getCreatorUsername();
        LocalDate creationDate = msg.getCreationDate();
        LocalTime creationTime = msg.getCreationTime();
        int msgSuccessful = -1;

        try {
            String SQL = "SELECT usermember FROM usergroup_groupmember WHERE gid = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, group);

            ResultSet rs = pstmt.executeQuery();
            List<String> recipients = new ArrayList<String>();

            while (rs.next())
            {
                recipients.add("usermember");
            }

            SQL = "INSERT INTO message VALUES (?,?,?,?,?,?,?)";
            pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, messageID);
            pstmt.setString(2, messageContent);
            pstmt.setBoolean(3, isMessagePrivate);
            pstmt.setBoolean(4, true);
            pstmt.setString(5, creator);
            pstmt.setObject(6, creationDate, Types.DATE);
            pstmt.setObject(7, creationTime, Types.TIME);

            int currentlyAffectedRows = pstmt.executeUpdate();

            SQL = "INSERT INTO message_recipient VALUES (?,?)";
            pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, messageID);

            int totalAffectedRows = currentlyAffectedRows;

            for (int recipient=0; recipient<recipients.size(); recipient++)
            {
                pstmt.setString(2, recipients.get(recipient));
                totalAffectedRows += pstmt.executeUpdate();
            }

            pstmt.close();
            conn.close();

            if (totalAffectedRows == recipients.size() + 1)
            {
                msgSuccessful = 1;
            }
            else
            {
                msgSuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return msgSuccessful;
    }

    /// Session operations ///

    public int createNewSession(UserSession sesh)
    {
        int createSessionSuccessful = -1;

        try {
            String SQL = "INSERT INTO usersession (sid, name, owner, description, " 
            + "latitudetopleft, longitudetopleft, latitudetopright, longitudetopright, "
            + "latitudebottomleft, longitudebottomleft, latitudebottomright, longitudebottomright, "
            + "startdate, enddate, starttime, endtime, isprivate, hasended) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);

            pstmt.setObject(1, sesh.getID());
            pstmt.setString(2, sesh.getName());
            pstmt.setString(3, sesh.getOwnerUsername());
            pstmt.setString(4, sesh.getDescription());
            pstmt.setDouble(5, sesh.getLatitudeTopLeft());
            pstmt.setDouble(6, sesh.getLongitudeTopLeft());
            pstmt.setDouble(7, sesh.getLatitudeTopRight());
            pstmt.setDouble(8, sesh.getLongitudeTopRight());
            pstmt.setDouble(9, sesh.getLatitudeBottomLeft());
            pstmt.setDouble(10, sesh.getLongitudeBottomLeft());
            pstmt.setDouble(11, sesh.getLatitudeBottomRight());
            pstmt.setDouble(12, sesh.getLongitudeBottomRight());
            pstmt.setObject(13, sesh.getStartDate(), Types.DATE);
            pstmt.setObject(14, sesh.getEndDate(), Types.DATE);
            pstmt.setObject(15, sesh.getStartTime(), Types.TIME);
            pstmt.setObject(16, sesh.getEndTime(), Types.TIME);
            pstmt.setBoolean(17, sesh.isSessionPrivate());
            pstmt.setBoolean(18, sesh.isSessionOpen());

            int currentlyAffectedRows = pstmt.executeUpdate();

            SQL = "INSERT INTO contains VALUES (?,?)";
            pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, sesh.getID());
            pstmt.setString(2, sesh.getOwnerUsername());

            int totalAffectedRows = currentlyAffectedRows + pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (totalAffectedRows == 2)
            {
                createSessionSuccessful = 1;
            }
            else
            {
                createSessionSuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return createSessionSuccessful;
    }

    public int cancelSession(UserSession sesh)
    {
        UUID sessionID = sesh.getID();
        int cancelSessionSucessful = -1;

        try {
            String SQL = "DELETE FROM usersession CASCADE WHERE sid = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, sessionID);
            
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            int removedUsers = sesh.getInvitedUsers().size();

            if (affectedRows == removedUsers + 1)
            {
                cancelSessionSucessful = 1;
            }
            else
            {
                cancelSessionSucessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return cancelSessionSucessful;
    }

    public int endSession(UUID sessionID)
    {
        int endSessionSuccessful = -1;

        try {
            String SQL = "UPDATE usersession SET hasended = true WHERE sid = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, sessionID);

            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (affectedRows == 1)
            {
                endSessionSuccessful = 1;
            }
            else
            {
                endSessionSuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return endSessionSuccessful;
    }

    public int inviteSessionUser(UUID sessionID, String username)
    {
        int inviteUserSuccessful = -1;

        try {
            String SQL = "INSERT INTO usersession_inviteduser VALUES (?,?)";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, sessionID);
            pstmt.setString(2, username);

            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (affectedRows == 1)
            {
                inviteUserSuccessful = 1;
            }
            else
            {
                inviteUserSuccessful = 0;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return inviteUserSuccessful;
    }

    public int checkInSessionUser(UUID sessionID, String username)
    {
        int checkInUserSuccessful = -1;

        try {
            String SQL = "INSERT INTO usersession_showedupuser VALUES (?,?)";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, sessionID);
            pstmt.setString(2, username);

            int currentlyAffectedRows = pstmt.executeUpdate();

            SQL = "INSERT INTO contains VALUES (?,?)";
            pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, sessionID);
            pstmt.setString(2, username);

            int totalAffectedRows = currentlyAffectedRows + pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (totalAffectedRows == 2)
            {
                checkInUserSuccessful = 1;
            }
            else
            {
                checkInUserSuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return checkInUserSuccessful;
    }

    public int removeSessionUser(UUID sessionID, String username)
    {
        int removeUserSuccessful = -1;

        try {
            String SQL = "DELETE FROM usersession_showedupuser "
            + "WHERE sid = ? AND theuser = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, sessionID);
            pstmt.setString(2, username);

            int currentlyAffectedRows = pstmt.executeUpdate();

            SQL = "DELETE FROM contains "
            + "WHERE sid = ? AND theuser = ?";
            pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, sessionID);
            pstmt.setString(2, username);

            int totalAffectedRows = currentlyAffectedRows + pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            if (totalAffectedRows == 2)
            {
                removeUserSuccessful = 1;
            }
            else
            {
                removeUserSuccessful = 0;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return removeUserSuccessful;
    }

    public List<String> getAllSessionUsers(UUID sessionID)
    {
        List<String> users = new ArrayList<String>();

        try {
            String SQL = "SELECT theuser FROM contains WHERE sid = ? ORDER BY theuser";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setObject(1, sessionID);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                users.add(rs.getString("theuser"));
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return users;
    }

    public List<UserSession> getAllOpenSessions()
    {
        List<UserSession> openSessions = new ArrayList<UserSession>();

        try {
            String SQL = "SELECT * FROM usersession WHERE hasended = ?";
            Connection conn = connectToDB();
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setBoolean(1, false);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                UUID sessionID = UUID.fromString(rs.getString("sid"));
                String name = rs.getString("name");
                String owner = rs.getString("owner");
                String description = rs.getString("description");
                double latitudetopleft = rs.getDouble("latitudetopleft");
                double longitudetopleft = rs.getDouble("longitudetopleft");
                double latitudetopright = rs.getDouble("latitudetopright");
                double longitudetopright = rs.getDouble("longitudetopright");
                double latitudebottomleft = rs.getDouble("latitudebottomleft");
                double longitudebottomleft = rs.getDouble("longitudebottomleft");
                double latitudebottomright = rs.getDouble("latitudebottomright");
                double longitudebottomright = rs.getDouble("longitudebottomright");
                LocalDate startDate = LocalDate.parse(rs.getString("startdate"));
                LocalTime startTime = LocalTime.parse(rs.getString("starttime"));
                boolean isPrivate = rs.getBoolean("isprivate");

                LocalDate endDate;

                if (rs.getString("enddate") == null)
                {
                    endDate = null;
                }
                else
                {
                    endDate = LocalDate.parse(rs.getString("enddate"));
                }

                LocalTime endTime;
                
                if (rs.getString("endtime") == null)
                {
                    endTime = null;
                }
                else
                {
                    endTime = LocalTime.parse(rs.getString("endtime"));
                }

                SQL = "SELECT theuser FROM usersession_inviteduser WHERE sid = ? ORDER BY theuser";
                pstmt = conn.prepareStatement(SQL);
                pstmt.setObject(1, sessionID);

                ResultSet invRs = pstmt.executeQuery();
                List<String> invitedUsers = new ArrayList<String>();

                while (invRs.next())
                {
                    invitedUsers.add(invRs.getString("theuser"));
                }

                SQL = "SELECT theuser FROM usersession_showedupuser WHERE sid = ? ORDER BY theuser";
                pstmt = conn.prepareStatement(SQL);
                pstmt.setObject(1, sessionID);

                ResultSet showRs = pstmt.executeQuery();
                List<String> showedUpUsers = new ArrayList<String>();

                while (showRs.next())
                {
                    showedUpUsers.add(showRs.getString("theuser"));
                }
            
                UserSession session = new UserSession(name, owner, description,
                latitudetopleft, longitudetopleft, latitudetopright, longitudetopright,
                latitudebottomleft, longitudebottomleft, latitudebottomright, longitudebottomright,
                startDate, endDate, startTime, endTime, isPrivate, invitedUsers);
                session.setID(sessionID);
                session.setAllShowedUpUsers(showedUpUsers);
                
                openSessions.add(session);
            }

            pstmt.close();
            conn.close();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return openSessions;
    }

}
