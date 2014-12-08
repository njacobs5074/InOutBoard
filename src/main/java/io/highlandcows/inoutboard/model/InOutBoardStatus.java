package io.highlandcows.inoutboard.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;

/**
 * @author highlandcows
 * @since 10/11/14
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum InOutBoardStatus {

    REGISTERED("Registered", true),
    UNREGISTERED("Unregistered", true),
    AVAILABLE("Available"),
    UNAVAILABLE("Unavailable"),
    UNKNOWN("Unknown"),
    OOTO("Out of the Office"),
    WFH("Working from Home");

    private static HashMap<String, InOutBoardStatus> lookupByDescription = new HashMap<>();
    static {
        for (InOutBoardStatus status : InOutBoardStatus.values()) {
            lookupByDescription.put(status.getDescription(), status);
        }
    }

    /**
     * Human-readable version of status.
     */
    private String description;

    /**
     * Field to differentiate whether can be set by user.
     * @param description
     */
    private boolean systemStatus;

    private InOutBoardStatus(String description, boolean systemStatus) {
        this.description = description;
        this.systemStatus = systemStatus;
    }

    private InOutBoardStatus(String description) {
        this(description, false);
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    @JsonIgnore
    public boolean isSystemStatus() { return systemStatus; }

    @JsonCreator
    public static InOutBoardStatus getInOutBoardStatusByDescription(String description) {
        return lookupByDescription.get(description);
    }
}
