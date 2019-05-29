package com.jtf.quartzstarter.utils;

import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Objects;
import java.util.stream.Stream;

public class JDBCUtil {

    /**
     * 判断表是否存在
     * @param connection
     * @param tableName
     * @return
     * @throws SQLException
     */
    public static boolean tableExist(Connection connection, String tableName) throws SQLException {
        boolean result = false;
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        ResultSet tables = databaseMetaData.getTables(catalog, schema, tableName, new String[]{"TABLE"});
        result = tables.next();
        tables.close();
        return result;
    }

    public static void executeBatch(Connection connection, InputStream inputStream) throws SQLException,IOException {
        boolean autoCommit = connection.getAutoCommit();
        if(autoCommit){
            connection.setAutoCommit(false);
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))){
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            Statement statement = connection.createStatement();
            while ((line = reader.readLine()) != null){
                stringBuilder.append(System.lineSeparator());
                String trim = line.trim();
                if(!trim.endsWith(";")){
                    stringBuilder.append(line);
                }else{
                    stringBuilder.append(trim.substring(0, trim.length()-1));
                    statement.addBatch(stringBuilder.toString());
                    stringBuilder.setLength(0);
                }
            }
            if(stringBuilder.length() != 0){
                statement.addBatch(stringBuilder.toString());
            }

            statement.executeBatch();
        }
        connection.commit();
        if(autoCommit){
            connection.setAutoCommit(true);
        }
    }

    public static int executeUpdate(Connection connection, String sql, Object... params) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        if(autoCommit){
            connection.setAutoCommit(false);
        }
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if(Objects.nonNull(params) && params.length != 0){
            for (int i = 1; i <= params.length; i++) {
                preparedStatement.setObject(i, params[i-1]);
            }
        }
        int result = preparedStatement.executeUpdate();
        connection.commit();
        if(autoCommit){
            connection.setAutoCommit(true);
        }
        return result;
    }

    public static ResultSet select(Connection connection, String sql, Object... params) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        if(autoCommit){
            connection.setAutoCommit(false);
        }
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if(Objects.nonNull(params) && params.length != 0){
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i+1, params[i]);
            }
        }
        ResultSet resultSet = preparedStatement.executeQuery();
        if(autoCommit){
            connection.setAutoCommit(true);
        }
        return resultSet;
    }

    public static DbType getDbType(Connection connection) throws SQLException {
        String driverName = connection.getMetaData().getDriverName();
        if(driverName.toUpperCase().contains("MYSQL")){
            return DbType.MYSQL;
        }
        return DbType.ORACLE;
    }

    public enum DbType{
        MYSQL,
        ORACLE;
    }
}
