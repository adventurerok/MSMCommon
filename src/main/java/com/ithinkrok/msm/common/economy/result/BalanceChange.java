package com.ithinkrok.msm.common.economy.result;


import com.ithinkrok.msm.common.economy.AccountIdentifier;
import com.ithinkrok.msm.common.economy.Currency;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.ConfigDeserializer;
import com.ithinkrok.util.config.ConfigSerializer;
import com.ithinkrok.util.config.MemoryConfig;

import java.math.BigDecimal;
import java.util.UUID;

public final class BalanceChange {

    private final AccountIdentifier account;
    private final BigDecimal oldBalance;
    private final BigDecimal newBalance;
    private final BigDecimal change;
    private final String reason;

    private BalanceChange(AccountIdentifier account, BigDecimal oldBalance, BigDecimal newBalance,
                         BigDecimal change, String reason) {
        this.account = account;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
        this.change = change;
        this.reason = reason;
    }

    public static BalanceChange fromOldAndNew(AccountIdentifier account,
                                              BigDecimal oldBalance, BigDecimal newBalance,
                                              String reason) {
        return new BalanceChange(account, oldBalance, newBalance, newBalance.subtract(oldBalance), reason);
    }

    public static BalanceChange fromNewAndChange(AccountIdentifier account,
                                              BigDecimal newBalance, BigDecimal change,
                                              String reason) {
        return new BalanceChange(account, newBalance.subtract(change), newBalance, change, reason);
    }

    public static BalanceChange fromOldAndChange(AccountIdentifier account,
                                                 BigDecimal oldBalance, BigDecimal change,
                                                 String reason) {
        return new BalanceChange(account, oldBalance, oldBalance.add(change), change, reason);
    }

    public static BalanceChange noBalanceChange(AccountIdentifier account, BigDecimal balance, String reason) {
        return new BalanceChange(account, balance, balance, BigDecimal.ZERO, reason);
    }


    public AccountIdentifier getAccount() {
        return account;
    }


    public BigDecimal getOldBalance() {
        return oldBalance;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public BigDecimal getChange() {
        return change;
    }

    public String getReason() {
        return reason;
    }

    public Config toConfig(ConfigSerializer<Currency> currencySerializer) {
        Config config = new MemoryConfig();

        config.set("account", account.getOwner().toString());
        config.set("currency", currencySerializer.serialize(account.getCurrency()));
        config.set("change", change);
        config.set("new_balance", newBalance);
        config.set("reason", reason);

        return config;
    }

    public static BalanceChange fromConfig(Config config, ConfigDeserializer<Currency> currencyDeserializer) {

        UUID account = UUID.fromString(config.getString("account"));
        Currency currency = currencyDeserializer.deserialize(config.getConfigOrNull("currency"));
        BigDecimal change = config.getBigDecimal("change");
        BigDecimal newBalance = config.getBigDecimal("new_balance");
        String reason = config.getString("reason");

        return fromNewAndChange(new AccountIdentifier(account, currency), newBalance, change, reason);
    }
}
