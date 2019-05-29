package com.jtf.quartzstarter.support.plug;

import com.jtf.quartzstarter.utils.JDBCUtil;
import org.quartz.JobKey;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 数据库持久化
 * @author jiangtaofeng
 */
public class DbLoggingPersister implements LoggingPersister{

    private DataSource dataSource;


    public DbLoggingPersister(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @Override
    public void init() {
        Connection connection = getConnection();
        try{

            boolean exist = JDBCUtil.tableExist(connection, "QRTZ_LOGGING");
            if(exist){
                return;
            }
            String sql = null;
            JDBCUtil.DbType dbType = JDBCUtil.getDbType(connection);
            switch (dbType){
                case MYSQL:{
                    sql = "CREATE TABLE QRTZ_LOGGING(\n" +
                            "ID VARCHAR(120) ,\n" +
                            "JOB_NAME VARCHAR(200) NOT NULL,\n" +
                            "JOB_GROUP VARCHAR(200) NOT NULL,\n" +
                            "FIRE_TIME DATETIME NOT NULL,\n" +
                            "SUCCESS VARCHAR(1) NOT NULL,\n" +
                            "JOB_LOG LONGTEXT NULL,\n" +
                            "PRIMARY KEY (ID))\n" +
                            "ENGINE=InnoDB;";
                    break;
                }
                case ORACLE:{
                    sql = "CREATE TABLE QRTZ_LOGGING\n" +
                            "(\n" +
                            "  ID VARCHAR2(120),\n" +
                            "  JOB_NAME  VARCHAR2(200) NOT NULL,\n" +
                            "  JOB_GROUP VARCHAR2(200) NOT NULL,\n" +
                            "  FIRE_TIME TIMESTAMP NOT NULL,\n" +
                            "  SUCCESS VARCHAR(1) NOT NULL,\n" +
                            "  JOB_LOG CLOB NULL,\n" +
                            "  CONSTRAINT QRTZ_LOGGING_PK PRIMARY KEY (ID)\n" +
                            ");";
                    break;
                }
                default:{
                    throw new RuntimeException("不支持的数据库");
                }
            }
            JDBCUtil.executeUpdate(connection, sql);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }finally {
            closeConnection(connection);
        }


    }

    @Override
    public void persister(LoggingJobPlugin.JobRunInfo jobRunInfo) {
        JobKey jobKey = jobRunInfo.getJobKey();
        LoggingJobPlugin.LoggingOutputStream outputStream = jobRunInfo.getOutputStream();
        String sql = "INSERT INTO QRTZ_LOGGING(ID, JOB_NAME, JOB_GROUP, FIRE_TIME, SUCCESS, JOB_LOG) VALUES(?,?,?,?,?,?)";
        Connection connection = getConnection();
        try {
            JDBCUtil.executeUpdate(connection, sql,
                    UUID.randomUUID().toString().replaceAll("\\-",""),
                    jobKey.getName(), jobKey.getGroup(), new Timestamp(jobRunInfo.getFireTime().getTime()),
                    jobRunInfo.isSuccess(), new String(outputStream.toByteArray(), StandardCharsets.UTF_8));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            closeConnection(connection);
        }

    }

    @Override
    public long deleteLogging(JobKey jobKey) {
        String sql = "DELETE FROM QRTZ_LOGGING WHERE  JOB_NAME = ? AND JOB_GROUP = ? ";
        Connection connection = getConnection();
        try {
            return (long)JDBCUtil.executeUpdate(connection, sql,
                    jobKey.getName(), jobKey.getGroup());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            closeConnection(connection);
        }
    }

    @Override
    public void deleteLogging(JobKey jobKey, Date date, boolean success) {

        String sql = "DELETE FROM QRTZ_LOGGING WHERE FIRE_TIME = ? AND JOB_NAME = ? AND JOB_GROUP = ? AND SUCCESS = ?";
        Connection connection = getConnection();
        try {
            JDBCUtil.executeUpdate(connection, sql,
                    new java.sql.Timestamp(date.getTime()),
                    jobKey.getName(), jobKey.getGroup(), success?"1":"0");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<JobRunHistory> findAllHistory(JobKey jobKey) {
        String sql = "SELECT SUCCESS, FIRE_TIME FROM QRTZ_LOGGING WHERE  JOB_NAME = ? AND JOB_GROUP = ? ORDER BY FIRE_TIME DESC";
        List<JobRunHistory> jobRunHistories = new ArrayList<>();
        Connection connection = getConnection();
        try{
            ResultSet resultSet = JDBCUtil.select(connection, sql,
                    jobKey.getName(), jobKey.getGroup());
            while (resultSet.next()){
                JobRunHistory jobRunHistory = new JobRunHistory();
                jobRunHistory.setSuccess("1".equals(resultSet.getString(1)));
                Timestamp timestamp = resultSet.getTimestamp(2);
                jobRunHistory.setFireTime(new Date(timestamp.getTime()));
                jobRunHistories.add(jobRunHistory);
            }
            resultSet.close();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }finally {
            closeConnection(connection);
        }
        return jobRunHistories;
    }

    @Override
    public String findHistoryDetail(JobKey jobKey, Date date, boolean success) {
        String sql = "SELECT JOB_LOG FROM QRTZ_LOGGING WHERE FIRE_TIME = ? AND JOB_NAME = ? AND JOB_GROUP = ? AND SUCCESS = ?";
        Connection connection = getConnection();
        try{
            ResultSet resultSet = JDBCUtil.select(connection, sql,
                    new Timestamp(date.getTime()),
                    jobKey.getName(), jobKey.getGroup(), success ? "1" : "0");
            if(resultSet.next()){
                return resultSet.getString(1);
            }
            resultSet.close();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }finally {
            closeConnection(connection);
        }
        return "";
    }

    @Override
    public long deleteLogging(JobKey jobKey, Date before) {

        String sql = "DELETE FROM QRTZ_LOGGING WHERE FIRE_TIME < ? AND JOB_NAME = ? AND JOB_GROUP = ?";
        Connection connection = getConnection();
        try {
            return (long)JDBCUtil.executeUpdate(connection, sql, new java.sql.Timestamp(before.getTime()), jobKey.getName(), jobKey.getGroup());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            closeConnection(connection);
        }

    }

    @Override
    public long  deleteLogging(Date before) {
        String sql = "DELETE FROM QRTZ_LOGGING WHERE FIRE_TIME < ?";
        Connection connection = getConnection();
        try {
            return  (long)JDBCUtil.executeUpdate(connection, sql, new java.sql.Timestamp(before.getTime()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            closeConnection(connection);
        }

    }

    private Connection getConnection(){
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeConnection(Connection connection){
        try {
            connection.close();
        } catch (SQLException e) {
            //
        }
    }
}
