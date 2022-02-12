package moe.caa.multilogin.loader;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

/**
 * 代表一个依赖
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Library {

    /**
     * 存放全部依赖
     */
    @Getter
    private static final Map<Integer, List<Library>> libraryMap;

    /**
     * 重定向依赖包名
     */
    private static final String packageRelocate = "moe.caa.multilogin.lib.%s";

    static {
        libraryMap = new HashMap<>();
        final ArrayList<Library> relocate = new ArrayList<>();
        final ArrayList<Library> runtime = new ArrayList<>();
        Scanner scanner = new Scanner(Objects.requireNonNull(Library.class.getResourceAsStream("/libraries")));
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final String[] args = line.split("\\s+");
            final Library library = new Library(
                    // group
                    args[1],
                    // name
                    args[2],
                    // version
                    args[3],
                    // sha256
                    args[4],
                    // package
                    args[5],
                    // mainClass
                    args[6],
                    // 已经重定向
                    args[7].equals("1"),
                    // 指定Maven仓库
                    args.length == 9 ? args[8] : null
            );
            if (args[0].equals("0")) {
                relocate.add(library);
            } else {
                runtime.add(library);
            }
        }
        scanner.close();
        libraryMap.put(0, relocate);
        libraryMap.put(1, runtime);
    }

    private String group;
    private String name;
    private String version;
    private String sha256;
    private String pagName;
    private String mainClass;
    private boolean relocated;
    private String specifiedMaven;

    /**
     * 返回依赖文件名
     */
    public String getFileName() {
        return name + '-' + version + ".jar";
    }

    /**
     * 生成依赖下载链接
     *
     * @param mavenCenter 指定 Maven 仓库链接
     * @param force       忽略 specifiedMaven 强制使用指定的 Maven 仓库链接
     */
    public String getLibraryUrl(String mavenCenter, boolean force) {
        StringBuilder sb = new StringBuilder();
        if (force) {
            sb.append(mavenCenter);
        } else {
            sb.append(specifiedMaven == null ? mavenCenter : specifiedMaven);
        }
        sb.append('/');
        sb.append(group.replace(".", "/"));
        sb.append('/');
        sb.append(name);
        sb.append('/');
        sb.append(version);
        sb.append('/');
        sb.append(name);
        sb.append('-');
        sb.append(version);
        sb.append(".jar");
        return sb.toString();
    }

    /**
     * 检查依赖是否已被加载
     *
     * @param loader 需要检查的类加载器
     */
    public boolean isLoaded(ClassLoader loader) {
        if (loader == null) loader = getClass().getClassLoader();
        String checkClass = relocated ? String.format(packageRelocate, mainClass) : mainClass;
        try {
            Class.forName(checkClass, true, loader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 检查 Sha256 值
     *
     * @param bytes Sha256 二进制对象
     */
    public boolean checkSha256(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String temp = Integer.toHexString((aByte & 0xFF));
            if (temp.length() == 1) {
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString().equals(sha256);
    }

    /**
     * 获得重定向规则
     *
     * @return 重定向规则
     */
    public Map<String, String> getRelocateRules() {
        Map<String, String> ret = new HashMap<>();
        if (relocated) {
            ret.put(pagName, String.format(packageRelocate, pagName));
        }
        return ret;
    }
}
