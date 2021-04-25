package io.github.donut.proj;

import io.github.donut.proj.utils.PreparedStatementWrapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBManager extends DBSource {
    private DBManager() {
        // empty
    }

    private static class InstanceHolder {
        private static final DBManager INSTANCE = new DBManager();
    }

    public static DBManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public String getPlayerInfo(String userName) {
        String sql = "SELECT * FROM users WHERE username = ?;";
//        JSONObject jsonObj = new JSONObject();
        JSONArray array = new JSONArray();
        try (
                Connection connection = getDataSource().getConnection();
                PreparedStatementWrapper stat = new PreparedStatementWrapper(connection, sql, userName) {
                    @Override
                    protected void prepareStatement(Object... params) throws SQLException {
                        stat.setString(1, (String) params[0]);
                    }
                };
                ResultSet rs = stat.executeQuery();
        ) {
            while (rs.next()) {
                JSONObject record = new JSONObject();
                record.put("id", rs.getString("id"));
                record.put("username", rs.getString("username"));
                record.put("firstname", rs.getString("firstname"));
                record.put("lastname", rs.getString("lastname"));
                record.put("password", rs.getString("password"));
                array.put(record);
            }
//            jsonObj.put("users", array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array.toString();
    }

    public boolean createAccount(String firstName, String lastName, String userName, String password) {
        String sql = "INSERT INTO users(username, firstname, lastname, password) VALUES(?, ?, ?, ?);";
        boolean temp = false;
        try (
                Connection connection = getDataSource().getConnection();
                PreparedStatementWrapper stat = new PreparedStatementWrapper(connection, sql, firstName, lastName, userName, password) {
                    @Override
                    protected void prepareStatement(Object... params) throws SQLException {
                        stat.setString(1, (String) params[0]);
                        stat.setString(2, (String) params[1]);
                        stat.setString(3, (String) params[2]);
                        stat.setString(4, (String) params[3]);
                    }
                };
        ) {
            if (stat.executeUpdate() != 0) temp = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    /**
     * Handles soft deletion of user account. Account persists in DB,
     * with isDeleted field set to true.
     * Currently a stub
     * @param userName user to delete
     * @return true if operation is successful, false otherwise
     * @author Grant Goldsworth
     */
    public boolean deleteAccount(String userName) {
        // DO THE SQL CHEESE HERE
        return false;
    }

    /**
     * Updates the first name in the DB for a user (not to be confused with updating actual username).
     * @param userName account name to be updated
     * @param firstName new name to be used
     * @return true if operation was successful
     * @author Grant Goldsworth
     */
    public boolean updateFirstName(String userName, String firstName) {
        return false;
    }

    /**
     * Updates the last name in the DB for a user (not to be confused with updating actual username).
     * @param userName account name to be updated
     * @param lastName new name to be used
     * @return true if operation was successful
     * @author Grant Goldsworth
     */
    public boolean updateLastName(String userName, String lastName) {
        return false;
    }


}
