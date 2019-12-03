package com.seshion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        UserAccount user1 = new UserAccount("nick123", "seshion");
        UserAccount user2 = new UserAccount("david456", "westconn");
        String user1Name = user1.getUserName();
        String user2Name = user2.getUserName();
        DBManager db = new DBManager();
        Scanner pauser = new Scanner(System.in);

        System.out.println("Creating 2 new users...");
        user1.setLatitude(-1.00);
        user1.setLongitude(-1.00);
        user2.setLatitude(-1.00);
        user2.setLongitude(-2.00);
        user1.changeLoggedInStatus();
        user2.changeLoggedInStatus();

        if (db.createNewUser(user1) == 1 && db.createNewUser(user2) == 1)
        {
            System.out.println("Created users: " + user1Name + ", " + user2Name);
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Logging in users...");

        if ( (db.userLogIn(user1) == 1) && (db.userLogIn(user2) == 1) )
        {
            System.out.println("Logged in users: " + user1Name + ", " + user2Name);
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Setting profile to private for user: " + user2Name);

        if (db.changeUserVisibility(user2) == 1)
        {
            System.out.println("Changed profile visibility for user: " + user2Name);
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Setting coordinates for user: " + user1Name);
        user1.setLatitude(1.00);
        user1.setLongitude(2.00);

        if (db.setUserCoordinates(user1Name, user1.getLatitude(), user1.getLongitude()) == 1)
        {
            System.out.println("Set coordinates to (" + user1.getLatitude() + "," + 
            user1.getLongitude() + ") " + "for user: " + user1Name);
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Getting coordinates for user: " + user1Name);
        List<Double> coordinates = db.getUserCoordinates(user1Name);
        System.out.println("Coordinates for user " + user1Name + ": "
        + coordinates.get(0) + "," + coordinates.get(1));
        System.out.println();

        System.out.println(user1Name + " sending friend request to " + user2Name);

        if (db.sendFriendRequest(user1Name, user2Name) == 1)
        {
            System.out.println("Friend request sent");
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println(user2Name + " accepting friend request from " + user1Name);

        if (db.manageFriendRequest(user2Name, user1Name, true) == 1)
        {
            System.out.println("Friend request accepted");
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Getting friends list of: " + user1Name);
        List<String> nickFriends = db.getFriends(user1Name);
        System.out.println("Friends of " + user1Name + ": " + nickFriends.get(0));
        System.out.println();

        System.out.println(user1Name + " removing " + user2Name + " as a friend");

        if (db.removeFriend(user1Name, user2Name) == 1)
        {
            System.out.println("Friend removed");
            System.out.println();
        }

        System.out.println(user1Name + " is creating a new group...");
        UserGroup nicksGroup = new UserGroup("Seshion", user1Name);

        if (db.createNewGroup(nicksGroup) == 1)
        {
            System.out.println("Group " + nicksGroup.getName() + " created");
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Getting owned groups of: " + user1Name);
        List<UserGroup> ownedGroups = db.getOwnedGroups(user1Name);
        System.out.println("Owned groups: " + ownedGroups.get(0).getName());
        System.out.println();
        

        System.out.println("Adding " + user2Name + " to the group");
        nicksGroup.addGroupMember(user2Name);

        if (db.addGroupMember(nicksGroup.getID(), user2Name) == 1)
        {
            System.out.println("Group member added");
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Getting joined groups of: " + user2Name);
        List<UserGroup> joinedGroups = db.getJoinedGroups(user2Name);
        System.out.println("Joined groups: " + joinedGroups.get(0).getName());
        System.out.println();

        System.out.println("Listing all group members...");
        List<String> groupMembers = db.getGroupMembers(nicksGroup.getID());
        System.out.println("Group members: " + groupMembers.get(0) + ", " + groupMembers.get(1));
        System.out.println();

        System.out.println("Removing " + user2Name + " from the group");
        nicksGroup.removeGroupMember(user2Name);

        if (db.removeGroupMember(nicksGroup.getID(), user2Name) == 1)
        {
            System.out.println("Group member removed");
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println(user1Name + " is sending a message to " + user2Name);
        Message msg = new Message(user1Name, user2Name, LocalDate.now(), LocalTime.now(), "Hello!", true, false);

        if (db.sendMessageIndividual(msg, user2Name) == 1)
        {
            System.out.println("Sent message: \"Hello!\"");
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Getting messages sent to: " + user2Name);
        List<Message> messages = db.getUserMessages(user2Name);
        System.out.println("Got message: " + messages.get(0).getMessage() + " from: " + messages.get(0).getCreatorUsername());
        System.out.println();

        System.out.println(user1Name + " is sending a message to his group");
        Message msgTwo = new Message(user1Name, nicksGroup.getID().toString(), LocalDate.now(), LocalTime.now(), "Yo!", true, false);

        if (db.sendMessageGroup(msgTwo, nicksGroup.getID()) == 1)
        {
            System.out.println("Group message sent");
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Getting the group message...");
        List<Message> groupMsg = db.getGroupMessages(nicksGroup.getID());
        for(int i=0; i<groupMsg.size(); i++)
        {
            System.out.println(groupMsg.get(i).getMessage());
        }
        System.out.println();

        System.out.println(user1Name + " is deleting his group...");

        if (db.deleteGroup(nicksGroup) == 1)
        {
            System.out.println("Group deleted");
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println(user1Name + " is creating a new session...");
        UserSession groupProject = new UserSession("Seshion Project", user1Name, null, 
        1.00, 2.00, 3.00, 4.00, LocalDate.now(), null, LocalTime.now(), null, false, null);

        if (db.createNewSession(groupProject) == 1)
        {
            System.out.println("Session " + groupProject.getName() + " created");
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Getting owned sessions of: " + user1Name);
        List<UserSession> ownedSessions = db.getOwnedSessions(user1Name);
        System.out.println("Owner of session: " + ownedSessions.get(0).getName());
        System.out.println();


        System.out.println("Inviting " + user2Name + " to the session");

        if (db.inviteSessionUser(groupProject.getID(), user2Name) == 1)
        {
            System.out.println("Invited " + user2Name);
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Getting invited to sessions of: " + user2Name);
        List<UserSession> invSessions = db.getInvitedSessions(user2Name);
        System.out.println("Invited to session: " + invSessions.get(0).getName() + " owned by: " + invSessions.get(0).getOwnerUsername());
        System.out.println();

        System.out.println("Checking in " + user2Name + " to the session");

        if (db.checkInSessionUser(groupProject.getID(), user2Name) == 1)
        {
            System.out.println(user2Name + " checked in");
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Getting all users in the session...");
        List<String> seshUsr = db.getAllSessionUsers(groupProject.getID());
        System.out.println("Session users: " + seshUsr.get(0) + ", " + seshUsr.get(1));
        System.out.println();

        System.out.println("Getting all open sessions...");
        List<UserSession> openSesh = db.getAllOpenSessions();
        System.out.println("ID: " + openSesh.get(0).getID() + " Name: " + openSesh.get(0).getName() + 
        " Owner: " + openSesh.get(0).getOwnerUsername());
        System.out.println();

        System.out.println(user1Name + " is ending the session...");

        if (db.endSession(groupProject.getID()) == 1)
        {
            System.out.println("Session ended");
            System.out.println();
        }
        else
        {
            pauser.nextLine();
        }

        System.out.println("Logging out users...");

        if (db.userLogOut(user1Name) == 1 && db.userLogOut(user2Name) == 1)
        {
            System.out.println("Logged out users: " + user1Name + ", " + user2Name);
        }

        pauser.close();
    }
}