package com.utils.sigar;

import java.net.URL;

/**
 * @author Twilight
 * @date 18-12-6 下午2:53
 */
public class SigarUtil {

    public static void initSigar() {
        URL url = SigarUtil.class.getClassLoader().getResource("sigar");
        if(url == null) {
            throw new RuntimeException("当前项目工程resources文件夹下没有Sigar环境文件");
        }
        String path = System.getProperty("java.library.path");
        String osName = System.getProperty("os.name");
        if("Linux".equals(osName)) {
            path += ":" + url.getPath();
        } else if(osName.contains("Windows")) {
            path += ";" + url.getPath();
        }
        System.setProperty("java.library.path", path);
    }

    public static void main(String[] args) {
        System.out.println(SigarUtil.class.getClassLoader().getResource("sigar"));
        System.out.println(System.getProperty("os.name"));
    }
}
