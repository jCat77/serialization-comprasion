package com.jcat.manual;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class Event {

    String id;
    String userId;
    String payload;
    long currentTimestamp;

    public Event() {
    }
}
