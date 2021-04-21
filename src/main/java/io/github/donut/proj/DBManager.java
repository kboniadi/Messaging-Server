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
        String sql = "SELECT * FROM playerinfo WHERE firstname = ?;";
        JSONObject jsonObj = new JSONObject();
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
            jsonObj.put("playerinfo", array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObj.toString();
    }

    public boolean signIn(String userName, String password) {
        String sql = "Select ? FROM playerInfo;";
        String db_password = null;
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
                db_password = rs.getString("password");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return password.equals(db_password);
    }

    public void signUp(String userName, String password, String firstName, String lastName) {
        String sql = "INSERT INTO playerInfo VALUES(?, ?, ?, ?);";
        try (
                Connection connection = getDataSource().getConnection();
                PreparedStatementWrapper stat = new PreparedStatementWrapper(connection, sql, userName, password, firstName, lastName) {
                    @Override
                    protected void prepareStatement(Object... params) throws SQLException {
                        stat.setString(1, (String) params[0]);
                        stat.setString(2, (String) params[1]);
                        stat.setString(3, (String) params[2]);
                        stat.setString(4, (String) params[3]);
                    }
                };
        ) {
            stat.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAccount(String userName, String password) {
        String sql = "DELETE from playerInfo where userName = ? AND password = ?;";
        try (
                Connection connection = getDataSource().getConnection();
                PreparedStatementWrapper stat = new PreparedStatementWrapper(connection, sql, userName, password) {
                    @Override
                    protected void prepareStatement(Object... params) throws SQLException {
                        stat.setString(1, (String) params[0]);
                        stat.setString(2, (String) params[1]);
                    }
                };
        ) {
            stat.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
