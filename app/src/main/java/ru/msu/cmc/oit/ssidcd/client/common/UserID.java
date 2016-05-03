package ru.msu.cmc.oit.ssidcd.client.common;


import java.io.Serializable;

/**
 * Abstract social media user identifier.
 */
public abstract class UserID implements Serializable {
    protected final String id;

    public UserID(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserID that = (UserID) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * @return unique string that identifies the user.
     */
    public abstract String serialize();

    /**
     * @param s  encoded string from UserID.serialize() method.
     * @return
     * @ throws RuntimeException, if encoded string has wrong format
     */
    public static UserID deserialize(String s){
        if(s.startsWith(FacebookUserID.prefix))
            return new FacebookUserID(s.substring(FacebookUserID.prefix.length()));

        throw new RuntimeException("Invalid identifier");
    }
}
