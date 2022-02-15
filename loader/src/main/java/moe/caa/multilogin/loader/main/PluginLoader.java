package moe.caa.multilogin.loader.main;

import lombok.Getter;
import moe.caa.multilogin.api.MultiLoginAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.ParallelFlows;
import moe.caa.multilogin.loader.Library;
import moe.caa.multilogin.loader.PriorURLClassLoader;
import moe.caa.multilogin.logger.Logger;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 插件内 ‘JarFile’ 文件加载器
 */
public class PluginLoader {

    // 内嵌 jar 包的文件名
    private static final String NEST_JAR_NAME = "MultiLogin-Core.JarFile";

    // 依赖文件夹
    private final File librariesFolder;

    // 临时目录文件夹
    private final File tempLibrariesFolder;

    @Getter
    private URLClassLoader pluginClassLoader;

    /**
     * @param librariesFolder     依赖文件夹
     * @param tempLibrariesFolder 临时目录文件夹
     */
    public PluginLoader(File librariesFolder, File tempLibrariesFolder) {
        this.librariesFolder = librariesFolder;
        this.tempLibrariesFolder = tempLibrariesFolder;
    }

    /**
     * 关闭
     */
    public void close() throws IOException {
        pluginClassLoader.close();
        removeAllFiles(tempLibrariesFolder);
    }

    /**
     * 校验依赖 Sha256 值，返回需要下载和重新下载的依赖清单
     */
    private List<Library> getNeedDownloadLibraries() {
        List<Library> ret = new ArrayList<>();
        // 首先遍历 relocate 工具包
        // 服务端可能会自带老版本的 relocate 工具包，将会报错，强制载入。
        for (Library library : Library.getLibraryMap().get(0)) {
            File libraryFile = new File(librariesFolder, library.getFileName());
            if (libraryFile.exists() && libraryFile.length() != 0) {
                if (checkSha256(library, libraryFile)) continue;
                Logger.LoggerProvider.getLogger().warn(
                        String.format("The sha256 value of file %s failed to be verified and will be downloaded again.", libraryFile.getAbsolutePath())
                );
            }
            ret.add(library);
        }

        // 这是运行时依赖，受到 relocate 影响
        for (Library library : Library.getLibraryMap().get(1)) {
            if (library.isLoaded(null)) continue;
            File libraryFile = new File(librariesFolder, library.getFileName());
            if (libraryFile.exists() && libraryFile.length() != 0) {
                if (checkSha256(library, libraryFile)) continue;
                Logger.LoggerProvider.getLogger().warn(
                        String.format("The sha256 value of file %s failed to be verified and will be downloaded again.", libraryFile.getAbsolutePath())
                );
            }
            ret.add(library);
        }
        return ret;
    }

    /**
     * 返回插件最终需要加载的依赖清单
     */
    private List<Library> getNeedLoadLibraries() {
        return Library.getLibraryMap().get(1).stream().filter(library -> !library.isLoaded(null)).collect(Collectors.toList());
    }

    /**
     * 运行依赖重定向程序，加工和返回最终要加载的 jar 文件清单
     */
    private List<URL> runRelocate(List<Library> needLoad) throws Throwable {
        List<URL> urls = new ArrayList<>();
        Set<String> pagName = new HashSet<>();
        List<URL> ret = new ArrayList<>();

        for (Library library : Library.getLibraryMap().get(0)) {
            urls.add(new File(librariesFolder, library.getFileName()).toURI().toURL());
            pagName.add(library.getPagName());
        }

        final PriorURLClassLoader loader = new PriorURLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader(), pagName);

        Class<?> jarRelocatorClass = Class.forName("me.lucko.jarrelocator.JarRelocator", true, loader);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle jarRelocatorConstructor = lookup.unreflectConstructor(jarRelocatorClass.getConstructor(File.class, File.class, Map.class));
        MethodHandle jarRelocator_runMethod = lookup.unreflect(jarRelocatorClass.getMethod("run"));

        Map<String, String> relocateRules = new HashMap<>();
        for (Library library : needLoad) {
            relocateRules.putAll(library.getRelocateRules());
        }

        for (Library library : needLoad) {
            File file = new File(librariesFolder, library.getFileName());
            if (library.isRelocated()) {
                File outFile = File.createTempFile("MultiLogin-", "-" + library.getFileName(), tempLibrariesFolder);
                outFile.deleteOnExit();
                Object o = jarRelocatorConstructor.invoke(file, outFile, relocateRules);
                jarRelocator_runMethod.invoke(o);
                ret.add(outFile.toURI().toURL());
            } else {
                ret.add(file.toURI().toURL());
            }
        }
        loader.close();
        return ret;
    }

    /**
     * 校验依赖 Sha256 值
     */
    private boolean checkSha256(Library library, File file) {
        if (!file.exists()) return false;
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            byte[] buff = new byte[1024];
            int n;
            while ((n = fis.read(buff)) > 0) {
                baos.write(buff, 0, n);
            }
            return library.checkSha256(MessageDigest.getInstance("SHA-256").digest(baos.toByteArray()));
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 开始加载
     */
    public MultiLoginAPI load(IPlugin plugin) throws Throwable {
        Logger.LoggerProvider.getLogger().info("Loading libraries...");
        generateFolder();
        // 需要下载的依赖项
        List<Library> needDownload = getNeedDownloadLibraries();
        // 需要加载的依赖项
        List<Library> needLoad = getNeedLoadLibraries();

        // 打印下载信息
        if (needDownload.size() != 0) {
            Logger.LoggerProvider.getLogger().info("Downloading missing files, this will take a while...");
            downloadLibraries(needDownload);
        }

        List<URL> urls = runRelocate(needLoad);
        File fbt = File.createTempFile("MultiLogin-", "-" + NEST_JAR_NAME + ".jar", tempLibrariesFolder);
        fbt.deleteOnExit();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(NEST_JAR_NAME);
             FileOutputStream output = new FileOutputStream(fbt)) {
            byte[] buff = new byte[1024];
            int b;
            while ((b = input.read(buff)) != -1) {
                output.write(buff, 0, b);
            }
            output.flush();
        }

        urls.add(fbt.toURI().toURL());

        pluginClassLoader = new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader());

        Class<?> coreMain = Class.forName("moe.caa.multilogin.core.main.MultiCore", true, pluginClassLoader);
        final Constructor<?> constructor = coreMain.getConstructor(IPlugin.class);
        return (MultiLoginAPI) constructor.newInstance(plugin);
    }

    /**
     * 文件下载
     *
     * @param downloads 需要下载的依赖列表
     */
    private void downloadLibraries(List<Library> downloads) throws IOException {
        ParallelFlows<Void> downloadFlows = new ParallelFlows<>();
        // 存放下载失败的依赖项
        List<Library> failList = Collections.synchronizedList(new ArrayList<>());

        for (Library library : downloads) {
            downloadFlows.getSteps().add(new BaseFlows<>() {
                @Override
                public Signal run(Void o) {
                    try {
                        File output = new File(librariesFolder, library.getFileName());
                        String libraryUrl = library.getLibraryUrl("https://repo1.maven.org/maven2", false);
                        downloadFile(libraryUrl, output);
                        Logger.LoggerProvider.getLogger().info("Downloaded: " + library.getFileName());
                        return Signal.PASSED;
                    } catch (Throwable throwable) {
                        Logger.LoggerProvider.getLogger().error(throwable);
                        failList.add(library);
                        return Signal.TERMINATED;
                    }
                }
            });
        }
        downloadFlows.run(null);

        if (failList.isEmpty()) return;
        // 抛出依赖下载异常
        throw new IOException("Unable to download missing files: " +
                failList.stream().map(Library::getFileName).collect(Collectors.joining(", ")));
    }

    /**
     * 文件下载
     *
     * @param url 下载链接
     * @param out 目标文件
     */
    private void downloadFile(String url, File out) throws IOException, URISyntaxException, InterruptedException {
        url = urlEncode(url);
        Logger.LoggerProvider.getLogger().debug("Downloading file: " + url);
        if (out.exists() && !out.delete()) throw new IOException("Unable to delete file: " + out.getAbsolutePath());
        File temp = new File(tempLibrariesFolder, "MultiLogin-" + out.getName() + ".downloading");
        final HttpClient client = HttpClient.newHttpClient();
        HttpResponse.BodyHandler<Path> handler = HttpResponse.BodyHandlers.ofFile(temp.toPath());
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).build();
        final HttpResponse<Path> send = client.send(request, handler);
        final int responseCode = send.statusCode();
        if (responseCode != 200)
            throw new IOException(String.format("Unable to download file(%d): %s", responseCode, url));
        if (!temp.renameTo(out)) throw new IOException("Unable to move file: " + temp.getAbsolutePath());
    }

    /**
     * URL 编码
     */
    private String urlEncode(String url) {
        StringBuilder sb;
        if (url.startsWith("http://")) {
            url = url.substring(7);
            sb = new StringBuilder("http://");
        } else if (url.startsWith("https://")) {
            url = url.substring(8);
            sb = new StringBuilder("https://");
        } else {
            sb = new StringBuilder();
        }
        var urls = url.split("/");
        for (int i = 0; i < urls.length; i++) {
            String ns = urls[i];
            if (i != 0) ns = URLEncoder.encode(ns, StandardCharsets.UTF_8);

            sb.append(ns);
            if (i != urls.length - 1)
                sb.append("/");
        }
        return sb.toString();
    }

    private boolean removeAllFiles(File file) {
        if (!file.exists()) return false;
        if (!file.isFile()) {
            File[] files = file.listFiles();
            if (files == null) return false;
            for (File f : files) {
                removeAllFiles(f);
            }
        }
        return file.delete();
    }

    /**
     * 生成依赖和临时目录文件夹
     */
    private void generateFolder() throws IOException {
        if (!librariesFolder.exists() && !librariesFolder.mkdirs()) {
            throw new IOException(String.format("Unable to create folder: %s", librariesFolder.getAbsolutePath()));
        }
        if (!tempLibrariesFolder.exists() && !tempLibrariesFolder.mkdirs()) {
            throw new IOException(String.format("Unable to create folder: %s", tempLibrariesFolder.getAbsolutePath()));
        }
    }
}
