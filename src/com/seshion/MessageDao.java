package com.seshion;

import java.util.UUID;

public interface MessageDao {
    public int sendMessageIndividual(Message msg, String recipient);
    public int sendMessageGroup(Message msg, UUID group);
}