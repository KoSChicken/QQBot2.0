package io.koschicken.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class InitDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitDatabase.class);

    public void initDB() {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        boolean exist = false;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:root.db");
            preparedStatement = conn.prepareStatement("select * from sqlite_master limit 1");
            ResultSet resultSet = preparedStatement.executeQuery();
            exist = resultSet.next();
            LOGGER.info(String.valueOf(exist));
        } catch (SQLException ignore) {
            LOGGER.info("初次运行，即将新建数据库。");
        } catch (ClassNotFoundException classNotFoundException) {
            LOGGER.error("数据库驱动不存在");
        } finally {
            try {
                createDatabase(exist, conn);
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void createDatabase(boolean exist, Connection conn) {
        if (!exist) {
            try (Statement statement = conn.createStatement()) {
                // 群成员表
                statement.executeUpdate("create table qq (qq varchar(15) primary key, sign_flag boolean default false, live_switch boolean default false, coin integer default 0, roll integer default 3, cygames_win integer default 0, nekogun integer default 10)");
                // 群成员与群关联表
                statement.executeUpdate("create table qq_group (id integer not null constraint group_pk primary key autoincrement, qq varchar(15) not null, group_code varchar(15) not null)");
                // 群成员的直播提醒表
                statement.executeUpdate("create table live (id integer not null constraint live_pk primary key autoincrement, qq varchar(15) not null, bili_uid varchar(15) not null)");
                // 随机事件记录（type 0-天选 1-中奖 etc.）
                statement.executeUpdate("create table lucky (id integer not null constraint lucky_pk primary key autoincrement, qq integer, group_code varchar(15) not null, date datetime, coin integer, type integer)");
                // 角色表
                statement.executeUpdate("create table character_game (code integer not null primary key, name text not null, profile text, available boolean default true)");
                // 群成员与角色关联表
                statement.executeUpdate("create table character_qq (qq varchar(15) not null, group_code varchar(15) not null, code integer not null)");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
