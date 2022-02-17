package moe.caa.multilogin.core.auth.verify;

import lombok.Data;
import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.core.util.ModifyThere;
import moe.caa.multilogin.core.util.There;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 二次验证上下文
 */
@Data
public class VerifyContext {

    // HasJoined 从 Yggdrasil 上扒来的档案信息
    private final HasJoinedResponse response;

    // 来自哪个 Yggdrasil Service
    private final YggdrasilService service;

    // 存放数据库中原有的数据，对应的值是 inGameUuid， currentUsername， whitelist
    private final There<UUID, String, Boolean> originalData;

    // 需要写入的数据库新值
    private final ModifyThere<UUID, String, Boolean> newData;

    // 存放踢出信息
    private final AtomicReference<String> kickMessage = new AtomicReference<>();

    private final AtomicReference<UUID> inGameUuid = new AtomicReference<>();

    protected VerifyContext(HasJoinedResponse response, YggdrasilService service, There<UUID, String, Boolean> originalData) {
        this.response = response;
        this.service = service;
        this.originalData = originalData;
        this.newData = new ModifyThere<>();
        this.inGameUuid.set(response.getId());
        if (originalData != null) {
            newData.setValue1(originalData.getValue1());
            newData.setValue2(originalData.getValue2());
            newData.setValue3(originalData.getValue3());
        } else {
            newData.setValue3(false);
        }
    }
}
