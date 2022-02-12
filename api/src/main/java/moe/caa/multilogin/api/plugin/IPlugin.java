package moe.caa.multilogin.api.plugin;

import java.io.File;

/**
 * 公共插件实例
 */
public interface IPlugin {

    /**
     * 获得服务器对象
     *
     * @return 服务器对象
     */
    IServer getRunServer();

    /**
     * 获得配置和数据文件路径
     *
     * @return 配置和数据文件路径
     */
    File getDataFolder();

    /**
     * 获得插件版本
     *
     * @return 插件版本
     */
    String getPluginVersion();
}
