# XML Codec 增强测试套件

## 概述

参考 JSON codec 的测试模式，为 yangkit-data-xml-codec 模块创建了全面的 RFC 7950/6020 符合性测试。

## 测试结构

### 1. 按类型分类的测试目录

```
src/test/resources/type/
├── string/          # String 类型测试
│   ├── string.yang        # YANG 模型
│   ├── valid*.xml         # 有效测试用例
│   └── invalid*.xml       # 无效测试用例
├── uint8/           # UInt8 类型测试
│   ├── uint8.yang
│   ├── valid*.xml
│   └── invalid*.xml
└── ... (其他类型)
```

### 2. 测试类组织

```java
// 辅助工具类
XmlCodecTypeTestFunc
├── expectedNoError()   // 验证有效数据
└── expectedError()     // 验证无效数据

// 类型测试类
XmlCodecDataTestString    // String 类型测试
XmlCodecDataTestUint8     // UInt8 类型测试
XmlCodecDataTestInt8      // Int8 类型测试（待添加）
XmlCodecDataTestBoolean   // Boolean 类型测试（待添加）
...
```

## 已实现的测试覆盖

### String 类型测试
- ✅ 正常字符串
- ✅ Pattern 约束（正则表达式）
- ✅ Length 约束（长度范围）
- ✅ 多 Pattern 约束
- ✅ Invert-match 修饰符
- ❌ 违反 Pattern 约束
- ❌ 违反 Length 约束
- ❌ 违反多 Pattern 约束

### UInt8 类型测试
- ✅ 正常值（0-255）
- ✅ Range 约束
- ✅ 边界值（0, 255）
- ❌ 超出范围（>255）
- ❌ 负数值
- ❌ 违反 Range 约束

### 综合测试（XmlCodecComprehensiveTest）
- ✅ 复杂嵌套结构
- ✅ 各种数据类型
- ✅ List / Leaf-list
- ✅ Choice / Case
- ✅ RPC Input/Output
- ✅ Action
- ✅ Notification
- ✅ Augment
- ✅ Config false 状态数据
- ✅ Grouping / Uses
- ✅ IdentityRef
- ✅ Bits 类型

## 测试结果

### 当前状态
```
Tests run: 37
- Passed: 30 (81%)
- Failed: 7 (19%) - 负向测试，需要改进验证逻辑
```

### 详细分析

#### 通过的测试（30 个）
1. **基础测试** (8/8) - 基本编解码功能正常
2. **综合测试** (13/13) - 复杂结构和高级特性支持良好
3. **String 有效数据** (5/5) - 字符串类型解析正确
4. **UInt8 有效数据** (4/4) - 无符号 8 位整数解析正确

#### 失败的测试（7 个）
这些测试**故意使用无效数据**，期望反序列化失败，但当前实现通过了验证：

1. `invalidPattern` - Pattern 约束未触发验证错误
2. `invalidLength` - Length 约束未触发验证错误
3. `invalidPatternModifier` - Invert-match 修饰符未生效
4. `invalidPatternMulti` - 多 Pattern 约束未生效
5. `invalidOverflow` - UInt8 溢出未检测
6. `invalidRange` - Range 约束未生效
7. `invalidNegative` - 负数未检测

**这些失败表明**：XML codec 在数据验证阶段需要加强对 YANG 约束的检查。

## 与 JSON Codec 对比

### JSON Codec 测试特点
- 每个数据类型都有独立的测试类
- 大量测试用例（每种类型 15-28 个测试）
- 包含有效和无效数据的完整覆盖
- 使用统一的测试辅助函数

### XML Codec 测试改进
- ✅ 已采用相同的测试结构
- ✅ 已创建类型化的测试目录
- ✅ 已实现辅助测试函数
- ⏳ 需要增加更多数据类型测试
- ⏳ 需要修复验证逻辑以捕获无效数据

## 下一步工作

### 1. 扩展测试覆盖
- 添加其他数值类型（int8, int16, int32, int64, uint16, uint32, uint64）
- 添加 boolean, empty, decimal64 类型
- 添加 enumeration, bits 类型
- 添加 identityref, union 类型
- 添加 binary, instance-identifier 类型

### 2. 修复验证问题
- 调查为什么 pattern 约束未生效
- 检查 length 约束的验证逻辑
- 确保数值范围约束被正确执行
- 验证 must 约束的处理

### 3. 增加边界测试
- 最小值/最大值边界
- 空字符串处理
- 特殊字符处理
- Unicode 支持

## 使用方法

### 运行特定类型测试
```bash
# 运行 String 类型测试
mvn test -pl yangkit-data-xml-codec "-Dtest=XmlCodecDataTestString"

# 运行 UInt8 类型测试
mvn test -pl yangkit-data-xml-codec "-Dtest=XmlCodecDataTestUint8"

# 运行所有类型测试
mvn test -pl yangkit-data-xml-codec "-Dtest=*type*"
```

### 运行综合测试
```bash
# 运行基础测试
mvn test -pl yangkit-data-xml-codec "-Dtest=XmlCodecBasicTest"

# 运行综合测试
mvn test -pl yangkit-data-xml-codec "-Dtest=XmlCodecComprehensiveTest"

# 运行所有 XML codec 测试
mvn test -pl yangkit-data-xml-codec "-Dtest=*XmlCodec*"
```

## 文件清单

### 测试资源
- `type/string/string.yang` - String 类型 YANG 模型
- `type/string/valid[1-5].xml` - String 有效测试数据
- `type/string/invalid[1-4].xml` - String 无效测试数据
- `type/uint8/uint8.yang` - UInt8 类型 YANG 模型
- `type/uint8/valid[1-4].xml` - UInt8 有效测试数据
- `type/uint8/invalid[1-3].xml` - UInt8 无效测试数据

### 测试代码
- `XmlCodecTypeTestFunc.java` - 测试辅助函数
- `XmlCodecDataTestString.java` - String 类型测试
- `XmlCodecDataTestUint8.java` - UInt8 类型测试
- `XmlCodecBasicTest.java` - 基础功能测试
- `XmlCodecComprehensiveTest.java` - 综合功能测试

### 现有综合测试资源
- `comprehensive/yang/xml-test-types.yang` - 综合测试 YANG 模型
- `comprehensive/yang/xml-test-augment.yang` - Augment 测试模块

## 参考资料

- RFC 7950 - YANG 1.1 Data Modeling Language
- RFC 6020 - YANG - A Data Modeling Language for NETCONF
- JSON Codec 测试实现：`yangkit-data-json-codec/src/test/java/org/yangcentral/yangkit/data/codec/json/test/`

## 维护说明

### 添加新的数据类型测试
1. 在 `src/test/resources/type/` 下创建类型目录
2. 创建 YANG 模型文件定义测试容器
3. 创建有效和无效的 XML 测试数据
4. 创建对应的测试类继承测试模式
5. 运行测试验证

### 测试数据命名规范
- `valid*.xml` - 应该成功反序列化的有效数据
- `invalid*.xml` - 应该失败的反序列化测试数据
- 文件名应描述测试场景（如 `validPattern`, `invalidLength`）

---

**创建日期**: 2024-03-28  
**最后更新**: 2024-03-28  
**版本**: 1.0
