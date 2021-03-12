package io.koschicken.db.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.koschicken.db.bean.AccountGroup;
import io.koschicken.db.bean.Account;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface AccountGroupMapper extends BaseMapper<AccountGroup> {
    @Select("select s.* from scores s " +
            "left join account_group g on s.account = g.account " +
            "where g.`group_code` = #{groupCode} " +
            "order by s.score desc limit 10")
    List<Account> rank(String groupCode);

    @Select("select * from account_group where account = #{account}")
    List<AccountGroup> findByAccount(String account);

    @Select("select * from account_group where account = #{account} and `group_code` = #{groupCode}")
    AccountGroup findOne(String account, String groupCode);

    @Delete("delete from account_group where account = #{account} and `group_code` = #{groupCode}")
    void deleteOne(String account, String groupCode);
}
