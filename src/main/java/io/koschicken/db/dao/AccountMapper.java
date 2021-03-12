package io.koschicken.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.koschicken.db.bean.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    @Update("update account set sign_flag = false")
    void resetSign();

    @Update("update account set roll = 3")
    void resetRoll();

    @Update("update account set nekogun = 10")
    void resetNekoGun();

    @Select("select sign_flag from account where account = #{account}")
    Boolean selectSign(@Param("account") String account);

    @Update("update account set live_switch = true where account = #{account}")
    int liveOn(@Param("account") String account);

    @Update("update account set live_switch = false where account = #{account}")
    int liveOff(@Param("account") String account);

    @Update("update account set sign_flag = true where account = #{account}")
    void sign(@Param("account") String account);

    @Update("update account set cygames_win = cygames_win + 1 where account = #{account}")
    void cygamesWin(@Param("account") String account);

    @Select("select * from account order by cygames_win desc")
    List<Account> cygamesRank();

    @Select("select s.* from account s " +
            "left join account_group qg on s.account = qg.account " +
            "where qg.group_code = #{code}")
    List<Account> listByGroupCode(String code);
}
