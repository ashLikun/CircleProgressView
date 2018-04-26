
[![Release](https://jitpack.io/v/ashLikun/CircleProgressView.svg)](https://jitpack.io/#ashLikun/CircleProgressView)

# **CircleProgressView**
1:google md 形式的圆形进度条
## 使用方法

build.gradle文件中添加:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
并且:

```gradle
dependencies {
    implementation 'com.github.ashLikun:CircleProgressView:{latest version}'//CircleProgressView
}
### 1.用法


### 混肴
####
    保证CommonAdapter的footerSize和headerSize字段不被混肴
    #某一变量不混淆
    -keepclasseswithmembers class com.xxx.xxx {
    }

