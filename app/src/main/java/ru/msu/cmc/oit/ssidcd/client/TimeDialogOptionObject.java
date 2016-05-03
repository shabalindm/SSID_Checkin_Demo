package ru.msu.cmc.oit.ssidcd.client;

import java.io.Serializable;

/**
 * Created by mitya on 30.04.2016.
 */
public class TimeDialogOptionObject implements Serializable {
    private final int time;
    private final String label;

    public TimeDialogOptionObject(int time, String label) {
        this.time = time;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public int getTime() {
        return time;
    }
}
