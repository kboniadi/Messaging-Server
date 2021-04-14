package io.github.donut.proj.databus.data;

import io.github.donut.proj.utils.GsonWrapper;

import java.io.InvalidClassException;

public class AccountData extends AbstractDataType {
    public final String type = "Account";
    public final String flag;
    public String userName;
    public String password;
    public String firstName;
    public String lastName;

    private AccountData(String userName, String password, String firstName, String lastName, String flag) {
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.flag = flag;
    }

    private AccountData(AccountData temp) {
        this.userName = temp.userName;
        this.password = temp.password;
        this.firstName = temp.firstName;
        this.lastName = temp.lastName;
        this.flag = temp.flag;
    }

    public static IDataType of(String userName, String password, String firstName, String lastName, String flag) {
        return new AccountData(userName, password, firstName, lastName, flag);
    }

    public static IDataType of(String json) throws InvalidClassException {
        AccountData obj = GsonWrapper.fromJson(json, AccountData.class);
        if (obj == null) throw new InvalidClassException("incompatible json -> { not of type AccountData.class }");
        return new AccountData(obj);
    }

}
