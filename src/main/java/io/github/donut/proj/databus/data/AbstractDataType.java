package io.github.donut.proj.databus.data;

import io.github.donut.proj.databus.DataBus;

public abstract class AbstractDataType implements IDataType {
    private DataBus dataBus;

    public DataBus getDataBus() {
        return dataBus;
    }

    public void setDataBus(DataBus dataBus) {
        this.dataBus = dataBus;
    }
}
