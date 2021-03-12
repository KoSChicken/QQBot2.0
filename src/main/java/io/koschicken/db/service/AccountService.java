package io.koschicken.db.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.koschicken.db.bean.Account;
import io.koschicken.db.dao.AccountGroupMapper;
import io.koschicken.db.dao.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService extends ServiceImpl<AccountMapper, Account> {

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    AccountGroupMapper accountGroupMapper;

    public void resetSign() {
        accountMapper.resetSign();
    }

    public void resetRoll() {
        accountMapper.resetRoll();
    }

    public void resetNekoGun() {
        accountMapper.resetNekoGun();
    }

    public Boolean selectSign(String account) {
        return accountMapper.selectSign(account);
    }

    public void sign(String account) {
        accountMapper.sign(account);
    }

    public int updateLiveOn(String account, boolean on) {
        if (on) {
            return accountMapper.liveOn(account);
        } else {
            return accountMapper.liveOff(account);
        }
    }

    public List<Account> rank(String groupCode) {
        return accountGroupMapper.rank(groupCode);
    }

    public void cygamesWin(String account) {
        accountMapper.cygamesWin(account);
    }

    public List<Account> cygamesRank() {
        return accountMapper.cygamesRank();
    }

    public List<Account> listByGroupCode(String code) {
        return accountMapper.listByGroupCode(code);
    }
}
