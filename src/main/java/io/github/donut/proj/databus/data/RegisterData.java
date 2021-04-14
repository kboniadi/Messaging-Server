package io.github.donut.proj.databus.data;

import io.github.donut.proj.utils.GsonWrapper;

import java.io.InvalidClassException;

public class RegisterData extends AbstractDataType {
    public final String type = "RegisterData";
    public final String[] messages;

    private RegisterData(String[] messages) {
        this.messages = messages;
    }

    private RegisterData(RegisterData temp) {
        this.messages = temp.messages;
    }

    public static IDataType of(String... interests) {
        return new RegisterData(interests);
    }

    public static IDataType of(String json) throws InvalidClassException {
        RegisterData obj = GsonWrapper.fromJson(json, RegisterData.class);
        if (obj == null) throw new InvalidClassException("incompatible json -> { not of type RegisterData.class }");
        return new RegisterData(obj);
    }
}
