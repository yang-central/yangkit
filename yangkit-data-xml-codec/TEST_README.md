# XML Codec 测试说明

## 概述

本文档描述 `yangkit-data-xml-codec` 模块当前测试结构、自动执行的测试套件、补充类型测试以及后续扩展方向。

与早期版本不同，当前 XML codec 的主测试套件已经在仓库现有日志中稳定通过，不再处于“7 个负向测试失败”的状态。当前应以最新日志为准，而不是以旧分析结论为准。

主要证据：

- `current-test.log`
- `xml-codec-full-test.log`

---

## 当前测试现状

### 自动执行的主测试套件

根据 `current-test.log`，当前 XML codec 模块自动执行并通过以下测试类：

1. `AnydataValidationOptionsXmlCodecTest`
   - `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`
2. `XmlCodecBasicTest`
   - `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`
3. `XmlCodecComprehensiveTest`
   - `Tests run: 18, Failures: 0, Errors: 0, Skipped: 0`

汇总结果：

```text
Tests run: 29, Failures: 0, Errors: 0, Skipped: 0
```

### 补充的类型测试

当前还存在一组按类型组织的补充测试，用于针对特定基础类型做更细粒度验证：

- `type/XmlCodecDataTestString.java`
- `type/XmlCodecDataTestUint8.java`
- `type/XmlCodecTypeTestFunc.java`

这些测试类当前更适合作为**定向执行的补充类型测试**来使用，而不是将其误写成“自动执行主套件的一部分”。

---

## 测试结构

### 1. 自动执行测试类

```text
src/test/java/org/yangcentral/yangkit/data/codec/xml/test/
├── AnydataValidationOptionsXmlCodecTest.java
├── XmlCodecBasicTest.java
└── XmlCodecComprehensiveTest.java
```

### 2. 定向类型测试类

```text
src/test/java/org/yangcentral/yangkit/data/codec/xml/test/type/
├── XmlCodecDataTestString.java
├── XmlCodecDataTestUint8.java
└── XmlCodecTypeTestFunc.java
```

### 3. 资源目录

```text
src/test/resources/
├── comprehensive/
│   └── yang/
├── type/
│   ├── string/
│   └── uint8/
└── anydata-validation/
```

---

## 当前覆盖范围

### `XmlCodecBasicTest`

主要覆盖：

- 基本 XML 序列化与反序列化
- 命名空间处理
- config true / false 过滤行为
- 空文档与多模块基本行为

### `XmlCodecComprehensiveTest`

主要覆盖：

- 复杂嵌套结构
- List / Leaf-list
- Choice / Case
- RPC / Action / Notification
- Augment
- Grouping / Uses
- IdentityRef
- Bits
- leaf-list `value` attribute 场景
- ordered-by user list 的 `insert` 场景
- anydata / anyxml 的 `select` 场景

### `AnydataValidationOptionsXmlCodecTest`

主要覆盖：

- `AnydataValidationOptions` 在 XML codec 中的文档级解析与匹配行为
- payload schema context 的解析与注入

### 定向类型测试

当前已提供：

- String 类型测试
  - 正常字符串
  - Pattern 约束
  - Length 约束
  - 多 pattern 约束
  - invert-match 修饰符
- UInt8 类型测试
  - 正常值
  - 边界值
  - Range 约束
  - 非法数值场景

---

## 与旧状态的差异说明

本文件此前记录过如下过时结论：

- `Tests run: 37`
- `Passed: 30`
- `Failed: 7`
- “负向测试，需要改进验证逻辑”

这些信息反映的是**早期阶段的测试结果**，不再代表当前主分支状态。

当前仓库中的最新日志显示：

- XML 主测试套件已全绿
- `insert` / `value` / `select` 等场景已有回归测试
- 旧问题描述不能继续作为当前版本对外说明

因此，后续文档、发布说明、RFC 合规分析应以最新日志与当前测试类为准。

---

## 建议的后续工作

### 1. 扩展类型覆盖

建议新增：

- `int8`, `int16`, `int32`, `int64`
- `uint16`, `uint32`, `uint64`
- `boolean`, `empty`, `decimal64`
- `enumeration`, `bits`
- `identityref`, `union`
- `binary`, `instance-identifier`

### 2. 补强负向断言

建议进一步增加：

- 非法 `insert` 值
- `before` / `after` 缺失引用值
- 无效 XPath `select`
- 复杂 anydata / anyxml 边界场景

### 3. 明确自动执行与定向执行边界

建议后续统一：

- 哪些测试类属于 CI 默认执行集合
- 哪些测试类属于手工定向回归集合

避免再次出现“测试类存在，但文档误认为已默认执行”的偏差。

---

## 使用方法

### 运行默认 XML codec 测试

```bash
mvn test -pl yangkit-data-xml-codec
```

### 运行定向类型测试

```bash
# String 类型测试
mvn test -pl yangkit-data-xml-codec "-Dtest=XmlCodecDataTestString"

# UInt8 类型测试
mvn test -pl yangkit-data-xml-codec "-Dtest=XmlCodecDataTestUint8"
```

### 运行特定主测试套件

```bash
# 基础功能测试
mvn test -pl yangkit-data-xml-codec "-Dtest=XmlCodecBasicTest"

# 综合测试
mvn test -pl yangkit-data-xml-codec "-Dtest=XmlCodecComprehensiveTest"

# anydata 验证上下文测试
mvn test -pl yangkit-data-xml-codec "-Dtest=AnydataValidationOptionsXmlCodecTest"
```

---

## 参考资料

- RFC 7950 - YANG 1.1 Data Modeling Language
- RFC 6020 - YANG - A Data Modeling Language for NETCONF
- `current-test.log`
- `xml-codec-full-test.log`
- `yangkit-data-json-codec/src/test/java/org/yangcentral/yangkit/data/codec/json/test/`

---

**创建日期**: 2024-03-28  
**最后更新**: 2026-04-02  
**版本**: 1.1
