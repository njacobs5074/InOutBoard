package io.highlandcows.inoutboard.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;

/**
 * @author highlandcows
 * @since 10/11/14
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum InOutBoardStatus {

    REGISTERED("Registered"),
    UNREGISTERED("Unregistered"),
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

    private InOutBoardStatus(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static InOutBoardStatus getInOutBoardStatusByDescription(String description) {
        return lookupByDescription.get(description);
    }
}
