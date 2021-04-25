package io.github.donut.proj.databus.data;

import io.github.donut.proj.databus.DataBus;

public interface IDataType {
    DataBus getDataBus();

    void setDataBus(DataBus dataBus);
}
