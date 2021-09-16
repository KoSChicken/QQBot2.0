package io.koschicken.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;

@Slf4j
@Component
public class InitDatabase {

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
            log.info(String.valueOf(exist));
        } catch (SQLException ignore) {
            log.info("初次运行，即将新建数据库。");
        } catch (ClassNotFoundException classNotFoundException) {
            log.error("数据库驱动不存在");
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
                statement.executeUpdate("create table account (account varchar(15) primary key, sign_flag boolean default false, live_switch boolean default false, coin integer default 0, roll integer default 3, cygames_win integer default 0, nekogun integer default 10)");
                // 群成员与群关联表
                statement.executeUpdate("create table account_group (id integer not null constraint group_pk primary key autoincrement, account varchar(15) not null, group_code varchar(15) not null)");
                // 群成员的直播提醒表
                statement.executeUpdate("create table live (id integer not null constraint live_pk primary key autoincrement, account varchar(15) not null, bili_uid varchar(15) not null)");
                // 随机事件记录（type 0-天选 1-中奖 etc.）
                statement.executeUpdate("create table lucky (id integer not null constraint lucky_pk primary key autoincrement, account integer, group_code varchar(15) not null, date datetime, coin integer, type integer)");
                // 角色表
                statement.executeUpdate("create table character_game (code integer not null primary key, name text not null, profile text, available boolean default true)");
                // 群成员与角色关联表
                statement.executeUpdate("create table character_account (account varchar(15) not null, group_code varchar(15) not null, code integer not null)");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
