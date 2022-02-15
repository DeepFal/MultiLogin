package fun.ksnb.multilogin.velocity.auth;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import fun.ksnb.multilogin.velocity.main.MultiLoginVelocity;
import fun.ksnb.multilogin.velocity.util.ReflectUtil;
import io.netty.util.collection.IntObjectMap;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

public class VelocityAuthCore {
    @Getter
    private static VelocityAuthCore instance;

    @Getter
    private final ProxyServer server;

    @Getter
    private final MultiLoginVelocity multiLoginVelocity;

    public VelocityAuthCore(ProxyServer server, MultiLoginVelocity multiLoginVelocity) {
        this.server = server;
        this.multiLoginVelocity = multiLoginVelocity;
        VelocityAuthCore.instance = this;
    }

    public void init() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        MultiLoginEncryptionResponse.init();
        VelocityUserLogin.init();
//        要替换的方向
        StateRegistry.PacketRegistry toReplace = StateRegistry.LOGIN.serverbound;

        Field field_versions = (Field) ReflectUtil.handleAccessible(StateRegistry.PacketRegistry.class.getDeclaredField("versions"), true);
//        获取注册Map
        Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry> map = (Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry>) field_versions.get(toReplace);
        for (StateRegistry.PacketRegistry.ProtocolRegistry protocolRegistry : map.values()) {
//            获取packetIdToSupplier Map
            Field field_packetIdToSupplier = (Field) ReflectUtil.handleAccessible(StateRegistry.PacketRegistry.ProtocolRegistry.class.getDeclaredField("packetIdToSupplier"), true);
            IntObjectMap<Supplier<? extends MinecraftPacket>> packetIdToSupplier = (IntObjectMap<Supplier<? extends MinecraftPacket>>) field_packetIdToSupplier.get(protocolRegistry);
//            至此 替换完成
            packetIdToSupplier.put(0x01, MultiLoginEncryptionResponse::new);
        }
    }
}
