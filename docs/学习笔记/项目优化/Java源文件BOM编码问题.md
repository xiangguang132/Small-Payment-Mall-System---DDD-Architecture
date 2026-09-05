# Java 源文件 BOM 编码问题 — 排查与修复笔记

> 主题：`GroupBuyOrderRepository.java` / `OrderRepository.java` 编译报「非法字符 `'﻿'`」「需要 class, interface 或 enum」的根因与处理
> 分支：`group-buy-dev` ｜ 模块：infrastructure

---

## 一、现象

`mvn compile`（或 IDE 编译）报两个连续错误：

```
java: 非法字符: '﻿'
java: 需要class, interface或enum
```

文件指向 `cn.bugstack.infrastructure.repository.GroupBuyOrderRepository` 和 `OrderRepository`。

## 二、根因：文件开头藏了一个 BOM 字符

BOM（Byte Order Mark，字节序标记）是 UTF-8 编码的**零宽度不可见字符**：

| 项目 | 值 |
| --- | --- |
| 十六进制字节 | `EF BB BF` |
| Unicode 码点 | `U+FEFF` |
| 显示 | 编辑器里看不到，长得很像"空" |

它落在 `package cn.bugstack...` 前面时：

```java
﻿package cn.bugstack.infrastructure.repository;   // BOM 就在这
```

- 第一个报错：编译器把 `U+FEFF` 当成一个**不认识的字符** → `非法字符: '﻿'`；
- 第二个报错：`package` 声明没被正确识别，编译器认为整个文件里没有任何类声明 → `需要 class, interface 或 enum`。

一句话：**Java 要求源码第一个合法 token 是 `package`/`import`/类声明，BOM 抢占了第一位。**

## 三、排查过程

`ggrep` / `cat` 看不出区别，用十六进制看文件头最直观：

```bash
head -c 8 <文件> | od -An -tx1
```

修复前后对比（`progg...` = `package `）：

```
修复前:  ef bb bf 70 61 63 6b 61 ...   <- 前 3 字节是 BOM
修复后:  70 61 63 6b 61 67 65 20 ...   <- 直接就是 "package "
```

## 四、修复：删掉开头的 3 个字节

```bash
sed -i '1s/^\xEF\xBB\xBF//' \
  s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/repository/GroupBuyOrderRepository.java \
  s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/repository/OrderRepository.java
```

全项目排查一下还有没有漏网的（返回空即干净）：

```bash
grep -rlP '^\xEF\xBB\xBF' --include='*.java' .
```

## 五、为什么会发生 / 怎么预防

- **起因**：某次用「带 BOM 的 UTF-8」（UTF-8 with BOM）编码保存了文件，例如 Windows 记事本、部分 IDE/插件或 `sed/perl` 输出重定向时易写入 BOM。
- 之前 `GroupBuyOrderRepository` 的 `git diff` 里其实就有征兆：`+package` 前面挂着一个看不见的零宽字符，肉眼极易漏掉。
- **预防**：
  1. IDE 里把默认编码设为 **UTF-8（无 BOM）**；
  2. 提交前跑一次上面的 `grep -rlP '^\xEF\xBB\xBF'` 自检；
  3. 编辑器状态栏出现"UTF-8 BOM / UTF-8 with BOM"字样时手动改为无 BOM。

## 六、备注

- 纯编码问题，与业务逻辑无关，也不影响换行符/内容；
- 若文件里出现**行尾**的 BOM（少见，多为拼接造成），`1s/...//` 只删行首，需按出现位置单独处理。