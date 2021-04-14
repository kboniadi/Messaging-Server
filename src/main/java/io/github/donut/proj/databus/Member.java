package io.github.donut.proj.databus;

import io.github.donut.proj.databus.data.IDataType;

public interface Member {
    void send(IDataType event);
}
