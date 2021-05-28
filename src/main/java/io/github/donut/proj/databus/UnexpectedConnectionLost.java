package io.github.donut.proj.databus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnexpectedConnectionLost {
    private final String uuid;
    private final long timeStamp;
    private final String[] channels;
}
