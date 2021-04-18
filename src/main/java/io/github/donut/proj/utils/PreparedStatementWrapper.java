package io.github.donut.proj.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class PreparedStatementWrapper implements AutoCloseable {
    protected PreparedStatement stat;

    public PreparedStatementWrapper(Connection con, String query, Object ... params) throws SQLException {
        this.stat = con.prepareStatement(query);
        this.prepareStatement(params);
    }

    protected abstract void prepareStatement(Object ... params) throws SQLException;

    public ResultSet executeQuery() throws SQLException {
        return this.stat.executeQuery();
    }

    public int executeUpdate() throws SQLException {
        return this.stat.executeUpdate();
    }

    @Override
    public void close() {
        try {
            this.stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

