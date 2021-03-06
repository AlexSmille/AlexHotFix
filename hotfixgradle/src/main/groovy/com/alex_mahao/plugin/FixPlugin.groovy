package com.alex_mahao.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import sun.rmi.runtime.Log


public class FixPlugin implements Plugin<Project> {

    static final int FLAG_RELEASE = 1
    static final int FLAG_DO_HOT = 2

    /**
     * 是release还是doHot
     */
    static int FLAG;

    private HashSet<String> buildNameList = new HashSet<>();
    private HashSet<String> flavorNameList = new HashSet<>();

    static String sFlavorName;


    @Override
    void apply(Project project) {
        project.afterEvaluate {
            //注入代码
            project.android.applicationVariants.each { variant ->
                if (!isEmpty(variant.buildType.name)) {
                    buildNameList.add(variant.buildType.name)
                }
                if (!isEmpty(variant.flavorName)) {
                    flavorNameList.add(variant.flavorName);
                }
            }

            flavorNameList.add("");
            println("产品列表:" + flavorNameList.toString())
            // 初始化代码注入工具
            // 获取transformClassesWithDexForXXX ,该task 将class 文件打包成dex
            for (String flavorName : flavorNameList) {
                def dexRelease = project.tasks.findByName("transformClassesWithDexFor${flavorName}Release")
                // 获取补丁的版本
                def dexdohot = project.tasks.findByName("transformClassesWithDexFor${flavorName}Dohot")

                if (dexRelease) {
                    println("-----" + dexRelease.name)
                    dexReleaseProcess(dexRelease)
                }

                if (dexdohot) {
                    println("-----" + dexdohot.name)
                    dexDohotProcess(dexdohot)
                }
            }
        }
    }

    public boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    //dohot 的处理
    def dexDohotProcess = { Task dexdohot ->
        //生成补丁的方式和release很像，都需要注入代码

        dexdohot.outputs.upToDateWhen { false }
        dexdohot.doFirst {
            println("***************开始注入代码***************")
            FLAG = FLAG_DO_HOT
            sFlavorName = getDohotFlavorName(dexdohot.getName())
            InjectUtils.init(project)
            FixUtils.initHotDir();
            dexdohot.inputs.files.each { File file ->

                if (file.name.endsWith(".jar") && InjectUtils.shouldInjectJar(file.absolutePath)) {
                    // 对jar包注入代码
                    InjectUtils.injectJar(file)
                } else if (file.isDirectory()) {
                    // 对主目录的clas进行注入
                    InjectUtils.injectDir(file)
                }
            }
            println("***************注入代码完成***************")

            println("***************开始生成补丁包***************")
            // 打补丁
            if (FixUtils.hotFile.listFiles().size() > 0) {
                // 有补丁，对补丁进行打包
                println("存在补丁，开始打包...")
                FixUtils.dx(project, FixUtils.hotFile.absolutePath, "patch_dex.jar")
            }

            println("***************生成补丁包结束***************")

        }

    }

// release的处理
    def dexReleaseProcess = { Task dexRelease ->
        // not up-to-date
        dexRelease.outputs.upToDateWhen { false }

        dexRelease.doFirst {
            // 注入代码
            println("***************开始注入代码***************")

            FLAG = FLAG_RELEASE
            sFlavorName = getReleaseFlavorName(dexRelease.getName())
            InjectUtils.init(project)
            dexRelease.inputs.files.each { File file ->

                if (file.name.endsWith(".jar") && InjectUtils.shouldInjectJar(file.absolutePath)) {
                    // 对jar包注入代码
                    InjectUtils.injectJar(file)
                } else if (file.isDirectory()) {
                    // 对主目录的clas进行注入
                    InjectUtils.injectDir(file)
                }
            }
            // 结束之后关闭流
            FixUtils.writer.close()
            println("***************注入代码完成***************")
        }

    }


    public String getReleaseFlavorName(String name) {
        return name.substring(0, name.length() - "Release".length()).substring("transformClassesWithDexFor".length());
    }

    public String getDohotFlavorName(String name) {
        return name.substring(0, name.length() - "Dohot".length()).substring("transformClassesWithDexFor".length());
    }

}