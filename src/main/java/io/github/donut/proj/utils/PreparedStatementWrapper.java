package io.github.donut.proj.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class PreparedStatementWrapper implements AutoCloseable {
    protected PreparedStatement stat;

    /**
     * @param con connection to DB
     * @param query sql query
     * @param params dynamic params for query
     * @throws SQLException sql query error
     * @author Kord Boniadi
     */
    public PreparedStatementWrapper(Connection con, String query, Object... params) throws SQLException {
        this.stat = con.prepareStatement(query);
        this.prepareStatement(params);
    }

    /**
     * @param params dynamic params for query
     * @throws SQLException sql query error
     * @author Kord Boniadi
     */
    protected abstract void prepareStatement(Object... params) throws SQLException;

    /**
     * @return queried data
     * @throws SQLException sql query error
     * @author Kord Boniadi
     */
    public ResultSet executeQuery() throws SQLException {
        return this.stat.executeQuery();
    }

    /**
     * @return returns number of rows modded
     * @throws SQLException sql query error
     * @author Kord Boniadi
     */
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

