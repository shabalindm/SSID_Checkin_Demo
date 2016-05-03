package ru.msu.cmc.oit.ssidcd.client.common;

import java.io.Serializable;

/**
 *Facebook user identifier.
 */
public class FacebookUserID extends UserID {

    public static final String prefix = "1";

    public FacebookUserID(String id) {
        super(id);
    }

    @Override
    public String serialize() {
        return prefix + id;
    }
}
