package fun.ksnb.multilogin.velocity.auth;

import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.connection.client.LoginSessionHandler;
import fun.ksnb.multilogin.velocity.util.ReflectUtil;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.api.auth.yggdrasil.response.Property;
import net.kyori.adventure.text.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class VelocityUserLogin {
    private static MethodHandle INITIALIZE_PLAYER_METHOD;
    private final LoginSessionHandler sessionHandler;
    private final Disconnectable disconnectable;

    private final String username;
    private final String serverId;
    private final String ip;

    public VelocityUserLogin(String username, String serverId, String ip, LoginSessionHandler sessionHandler, Disconnectable disconnectable) {
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
        this.sessionHandler = sessionHandler;
        this.disconnectable = disconnectable;
    }

    public static void init() throws NoSuchMethodException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        INITIALIZE_PLAYER_METHOD = lookup.unreflect((Method) ReflectUtil.handleAccessible(LoginSessionHandler.class.getDeclaredMethod("initializePlayer", GameProfile.class, boolean.class), true));
    }


    @SneakyThrows
    public void doAuth() {
        final AuthResult auth = VelocityAuthCore.getInstance().getMultiLoginVelocity().getMultiLoginAPI().getAuthCore().auth(username, serverId, ip);

        if (auth.hasPassed()) {
            INITIALIZE_PLAYER_METHOD.invoke(sessionHandler, generateLoginResult(auth.getResponse()), true);
        } else {
            disconnectable.disconnect(Component.text(auth.getKickMessage()));
        }
    }

    private GameProfile generateLoginResult(HasJoinedResponse response) {
        List<Property> values = new ArrayList<>(response.getPropertyMap().values());

        List<GameProfile.Property> properties = new ArrayList<>();
        for (Property value : values) {
            properties.add(generateProperty(value));
        }

        return new GameProfile(
                response.getId().toString().replace("-", ""),
                response.getName(),
                properties
        );
    }

    private GameProfile.Property generateProperty(Property property) {
        return new GameProfile.Property(property.getName(), property.getValue(), property.getSignature());
    }
}
