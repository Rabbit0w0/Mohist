package com.mohistmc.libraries;

import com.mohistmc.configuration.MohistConfigUtil;
import com.mohistmc.network.download.UpdateUtils;
import com.mohistmc.util.JarLoader;
import com.mohistmc.util.JarTool;
import com.mohistmc.util.MD5Util;
import com.mohistmc.util.i18n.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class DefaultLibraries {
    private static int retry = 0;
    public static HashMap<String, String> fail = new HashMap<>();

    public static void run() throws Exception {
        System.out.println(Message.getString("libraries.checking.start"));
        String url = "https://www.mgazul.cn/";
        if (Message.isCN()) url = "https://mohist-community.gitee.io/mohistdown/"; //Gitee Mirror
        LinkedHashMap<File, String> libs = getDefaultLibs();

        for (File lib : getDefaultLibs().keySet()) {
            if((!lib.exists() || !MD5Util.md5CheckSum(lib, libs.get(lib))) && !MohistConfigUtil.getString(MohistConfigUtil.mohistyml, "libraries_black_list:", "xxxxx").contains(lib.getName())) {
                lib.getParentFile().mkdirs();
                String u = url + "libraries/" + lib.getAbsolutePath().replaceAll("\\\\", "/").split("/libraries/")[1];
                System.out.println(Message.getString("libraries.global.percentage") + String.valueOf((float) UpdateUtils.getSizeOfDirectory(new File(JarTool.getJarDir() + "/libraries")) / 35 * 100).substring(0, 2).replace(".", "") + "%"); //Global percentage

                try {
                    UpdateUtils.downloadFile(u, lib);
                    fail.remove(u);
                } catch (Exception e) {
                    System.out.println(Message.getFormatString("file.download.nook", new Object[]{u}));
                    lib.delete();
                    fail.put(u, lib.getAbsolutePath());
                }
            }

        }
        /*FINISHED | RECHECK IF A FILE FAILED*/
        if (retry < 3 && !fail.isEmpty()) {
            retry++;
            System.out.println(Message.getFormatString("update.retry", new Object[]{retry}));
            run();
        } else {
            System.out.println(Message.getString("libraries.checking.end"));
            if(!fail.isEmpty()) {
                System.out.println(Message.getString("libraries.cant.start.server"));
                for (String lib : fail.keySet())
                    System.out.println("Link : " + lib + "\nPath : " + fail.get(lib) + "\n");
                System.exit(0);
            }
        }
    }

    public static LinkedHashMap<File, String> getDefaultLibs() throws Exception {
        LinkedHashMap<File, String> temp = new LinkedHashMap<>();
        BufferedReader b = new BufferedReader(new InputStreamReader(DefaultLibraries.class.getClassLoader().getResourceAsStream("mohist_libraries.txt")));
        String str;
        while ((str = b.readLine()) != null) {
            String[] s = str.split("\\|");
            temp.put(new File(JarTool.getJarDir() + "/" + s[0]), s[1]);
        }
        b.close();
        return temp;
    }

    public static void loadDefaultLibs() throws Exception {
        for (File lib : getDefaultLibs().keySet())
            if(lib.exists() && !MohistConfigUtil.getString(MohistConfigUtil.mohistyml, "libraries_black_list:", "xxxxx").contains(lib.getName()))
                JarLoader.loadjar(lib.getAbsolutePath());
    }
}
