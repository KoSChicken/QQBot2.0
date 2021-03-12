package io.koschicken.db.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.koschicken.db.bean.AccountGroup;
import io.koschicken.db.dao.AccountGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountGroupService extends ServiceImpl<AccountGroupMapper, AccountGroup> {

    @Autowired
    AccountGroupMapper accountGroupMapper;

    public List<AccountGroup> findByAccount(String qq) {
        return accountGroupMapper.findByAccount(qq);
    }

    public AccountGroup findOne(String qq, String groupCode) {
        return accountGroupMapper.findOne(qq, groupCode);
    }

    public void deleteOne(String qq, String groupCode) {
        accountGroupMapper.deleteOne(qq, groupCode);
    }
}
