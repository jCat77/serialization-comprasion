package com.jcat.manual;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class Event implements Serializable {

    String id;
    String userId;
    String payload;
    long currentTimestamp;

    public Event() {
    }
}
