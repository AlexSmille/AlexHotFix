## Android Studio 热修复插件

### 实现功能：

- 自动注入代码，防止类被打上CLASS_ISPREVERIFIED

- 可选择不被注入的代码类，同时Android Support的相关类不会被注入代码

- **自动生成补丁包**


### 使用介绍

- 正常打包时，请选择release版本，自动保存类的相关MD5，以便生成补丁时使用

- 需要生成补丁文件时，选择dohot版本。


### 文件目录介绍

- `app`:测试的文件

- `hot`:生成的补丁的存放文件，包含相应补丁的jar和差异文件

- `hotfixgradle`:Gradle插件，实现自动注入代码和生成补丁包等相关功能的实现

- `hotpatch`:动态注入的相关功能类库

- `lib`:普通的依赖库

- `antilazyLoad.jar` : 依赖注入所需要的jar包

- `repo`:本地maven库，以便使用Gradle插件，后期会发布到JCenter中

- `hack.jar`:无用文件，后期删除

### 实现原理

会于近两日补充，敬请期待

### TODO

- 混淆代码

- 多渠道打包


本框架属于交流学习使用，欢迎转载，但请注上原文链接，谢谢合作。




