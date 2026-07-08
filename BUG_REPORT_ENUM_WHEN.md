# Bug 修复报告：ENUM/BIT 语句下 when 子语句解析器未报错

## 1. Bug 现象

当 YANG 文件中 `enum` 或 `bit` 语句下出现 `when` 子语句时，解析器没有报错，但按照 RFC 7950 规范，`enum` 和 `bit` 的合法子语句中**不包括** `when`。

### 触发问题的 YANG 文件示例

```yang
leaf qvileueebk {
    type "enumeration" {
        enum "ctvgtnvszf" {
            // ... extensions ...
            when "not(contains(/tnmbbsfdgx/mmiyyewzpa, '123'))";  // 第69行：不应该允许！
        }
    }
}
leaf cnpbwhckwa {
    type "bits" {
        bit "otbpthbolp" {
            // ... description ...
            when "/tnmbbsfdgx/mmiyyewzpa != '456'";  // 第117行：不应该允许！
        }
    }
}
```

按照 RFC 7950：

- `enum` 的合法子语句：`description`、`if-feature`、`reference`、`status`、`value`
- `bit` 的合法子语句：`description`、`if-feature`、`position`、`reference`、`status`

`when` 不在上述任何一个列表中。

---

## 2. 定位过程

### 2.1 确认合法子语句定义

文件：`YangSpecification.java`（第 534-549 行）

```java
// enum 的合法子语句定义（YANG 1.1）
YangStatementDef enumDef = new YangStatementDef(...);
enumDef.addSubStatementInfo(new YangSubStatementInfo(DESCRIPTION, cardinality(0,1)));
enumDef.addSubStatementInfo(new YangSubStatementInfo(IFFEATURE,   cardinality()));
enumDef.addSubStatementInfo(new YangSubStatementInfo(REFERENCE,   cardinality(0,1)));
enumDef.addSubStatementInfo(new YangSubStatementInfo(STATUS,      cardinality(0,1)));
enumDef.addSubStatementInfo(new YangSubStatementInfo(VALUE,       cardinality(0,1)));
// 注意：没有 WHEN

// bit 的合法子语句定义（YANG 1.1）
YangStatementDef bit = new YangStatementDef(...);
bit.addSubStatementInfo(new YangSubStatementInfo(DESCRIPTION, cardinality(0,1)));
bit.addSubStatementInfo(new YangSubStatementInfo(IFFEATURE,   cardinality()));
bit.addSubStatementInfo(new YangSubStatementInfo(POSITION,    cardinality(0,1)));
bit.addSubStatementInfo(new YangSubStatementInfo(REFERENCE,   cardinality(0,1)));
bit.addSubStatementInfo(new YangSubStatementInfo(STATUS,      cardinality(0,1)));
// 注意：也没有 WHEN
```

定义层没有问题，`when` 未被注册为 `enum`/`bit` 的合法子语句。

### 2.2 确认校验逻辑位置

子语句合法性校验代码位于 `YangBuiltInStatementImpl.initSelf()`（第 56-75 行）：

```java
// YangBuiltInStatementImpl.java:56-75
for (YangElement subElement : this.getSubElements()) {
    if (!(subElement instanceof YangStatement)) continue;
    YangStatement yangStatement = (YangStatement) subElement;
    if (!subStatementInfos.containsKey(yangStatement.getYangKeyword())) {
        if (!(yangStatement instanceof YangUnknown)) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(yangStatement,
                ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));  // ← 只有这里报 INVALID_SUBSTATEMENT
        }
    }
}
```

这段代码遍历父语句的所有子元素，对不在 `statementDef.subStatementInfos` 中的内置关键字子语句报告 `INVALID_SUBSTATEMENT` 错误。

### 2.3 确认类继承链（根因定位）

```
接口层：
  YangEnum extends Entity, IfFeatureSupport, YangBuiltinStatement
  Bit     extends Entity, Identifiable, IfFeatureSupport, YangBuiltinStatement

实现层（修改前）：
  YangStatementImpl                        ← 基类，initSelf() 不做子语句校验
    ├── YangBuiltInStatementImpl           ← initSelf() 中做子语句校验（正确）
    │     ├── WhenImpl, TypeImpl, ...
    └── EntityImpl                         ← 只继承 YangStatementImpl，丢失了校验能力！
          ├── EnumImpl                     ← 继承链上缺少子语句校验
          ├── BitImpl                      ← 继承链上缺少子语句校验
          ├── FeatureImpl、IdentityImpl     ← 同样的问题
          ├── TypedefImpl、ExtensionImpl    ← 同样的问题
          ├── GroupingImpl                  ← 同样的问题
          └── SchemaNodeImpl               ← SchemaNode extends Entity (非 YangBuiltinStatement) OK
```

### 2.4 根因

**`EntityImpl` 继承的是 `YangStatementImpl`，而不是 `YangBuiltInStatementImpl`。**

这使得所有 `EntityImpl` 的子类都丢失了 `YangBuiltInStatementImpl.initSelf()` 中的子语句合法性校验逻辑。当 `enum`/`bit` 下出现 `when` 时，解析流程完全不会触发错误报告。

---

## 3. 修复方案

### 设计思路

- `Entity` 接口只提供 `description`/`reference`/`status` 三个字段，不等于 `YangBuiltinStatement`
- 接口层上 `YangEnum`、`Bit`、`Feature`、`Identity`、`Typedef`、`Extension`、`Grouping` 都**同时**继承了 `Entity` + `YangBuiltinStatement`
- 实现层应该通过继承 `YangBuiltInStatementImpl` 获得子语句校验能力，通过组合 `EntitySupport` 获得 Entity 字段的操作能力

### 具体修改

```
修改前：                           修改后：
EntityImpl extends                 EntityImpl extends
  YangStatementImpl                  YangStatementImpl
     ↑                                 ↑
  无子语句校验                      无变化（Entity ≠ YangBuiltinStatement）

EnumImpl extends                   EnumImpl extends
  EntityImpl                         YangBuiltInStatementImpl
     ↑                                 ↑
  无子语句校验                      通过 YangBuiltInStatementImpl.initSelf()
                                    获得子语句校验 + 组合 EntitySupport
```

---

## 4. 修改文件清单

| 文件 | 操作 | 说明 |
|---|---|---|
| `EntitySupport.java` | **新增** | 封装 `description`/`reference`/`status` 字段的操作方法 `init()`/`getEffectiveSubStatements()`/`clear()`/getter/setter |
| `EntityImpl.java` | **修改** | 内部改用 `EntitySupport` 代理，继承链不变（仍 `extends YangStatementImpl`） |
| `EnumImpl.java` | **修改** | `extends EntityImpl` → `extends YangBuiltInStatementImpl`，组合 `EntitySupport` |
| `BitImpl.java` | **修改** | 同上 |
| `FeatureImpl.java` | **修改** | 同上 |
| `IdentityImpl.java` | **修改** | 同上 |
| `TypedefImpl.java` | **修改** | 同上 |
| `ExtensionImpl.java` | **修改** | 同上 |
| `GroupingImpl.java` | **修改** | 同上 |
| `SchemaNodeImpl.java` | **不动** | `SchemaNode` 接口不继承 `YangBuiltinStatement`，保持 `extends EntityImpl` |

所有文件路径根目录：`yangkit-model-impl/src/main/java/org/yangcentral/yangkit/model/impl/stmt/`

---

## 5. 回归测试

### 5.1 新增测试：`EnumWhenTest`

测试文件：`yangkit-parser/src/test/java/org/yangcentral/yangkit/test/parser/EnumWhenTest.java`

测试 YANG 文件：`yangkit-parser/src/test/resources/enum-when-test.yang`

```yang
module enum-when-test {
    namespace "urn:test:enum-when";
    prefix "ewt";
    yang-version "1.1";
    organization "test";
    contact "test@test.com";
    description "test";

    leaf test-leaf {
        type enumeration {
            enum alpha {
                when "true()";  // 非法！expected INVALID_SUBSTATEMENT
            }
        }
    }
}
```

测试结果：**通过** ✅ — `result.isOk()` 返回 `false`，正确报告了错误。

### 5.2 原有测试：`YangParserTest`

测试文件：`yangkit-parser/src/test/java/org/yangcentral/yangkit/test/parser/YangParserTest.java`

测试结果：**通过** ✅ — `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`

### 5.3 编译验证

`mvn compile -pl yangkit-model-impl -am -DskipTests` → **BUILD SUCCESS**

---

## 6. 结论

- **根因**：`EntityImpl` 继承 `YangStatementImpl` 而非 `YangBuiltInStatementImpl`，导致其所有子类丢失了子语句合法性校验
- **修复**：将接口层声明了 `YangBuiltinStatement` 的 Entity 子类从 `extends EntityImpl` 改为 `extends YangBuiltInStatementImpl`，通过组合 `EntitySupport` 复用 Entity 字段操作
- **影响范围**：`EnumImpl`、`BitImpl`、`FeatureImpl`、`IdentityImpl`、`TypedefImpl`、`ExtensionImpl`、`GroupingImpl` 共 7 个类
- **不受影响**：`SchemaNodeImpl`（`SchemaNode` 不继承 `YangBuiltinStatement`，保持原继承链）
- **回归验证**：编译通过，原有测试无回归，新增 `EnumWhenTest` 验证修复生效

---

## 7. 后续修复：SchemaNodeImpl 子类同样缺少子语句校验

### 7.1 问题定位

第一阶段修复完成后，进一步分析发现存在同样问题但影响面更大的第二个 Bug：

`SchemaNode` 接口是 `Entity` 的唯一**不**声明 `YangBuiltinStatement` 的子接口（其他 6 个 Entity 子接口如 `Identity`、`Feature` 等都已声明）。因此 `SchemaNodeImpl extends EntityImpl` 是合理的——它不能继承 `YangBuiltInStatementImpl`。

但问题在于：`SchemaNodeImpl` 的直接/间接子类中有 15 个具体类，其对应接口**都声明了 `YangBuiltinStatement`**，但由于继承了 `SchemaNodeImpl → EntityImpl → YangStatementImpl` 这条链，完全绕过了子语句校验。

```
YangStatementImpl                          ← initSelf() 不做子语句校验
  ├── YangBuiltInStatementImpl             ← 唯一有子语句校验的地方
  └── EntityImpl                           ← 无子语句校验
        └── SchemaNodeImpl                  ← 不能改继承，因为 SchemaNode != YangBuiltinStatement
              ├── DataDefinitionImpl        ← 15 个 Builtin 子类经过此处绕过校验
              ├── OperationImpl             ← RpcImpl, ActionImpl 同样绕过
              ├── InputImpl                 ← 直接绕过
              ├── OutputImpl                ← 直接绕过
              ├── NotificationImpl          ← 直接绕过
              └── YangDataStructureImpl     ← YangUnknown, 正确不受影响
```

唯一的例外是 `YangDataStructureImpl`（`YangStructure extends YangUnknown`，不是 `YangBuiltinStatement`），它不应该有子语句校验。

### 7.2 修复方案：接口 default 方法

`YangBuiltinStatement` 是一个空标记接口，不强制任何行为契约。由于 Java 单继承限制，15 个受影响的类散落在 7 条不同的抽象继承链上，逐一改继承不可行。

**策略**：在 `YangBuiltinStatement` 接口中添加 `validateSubStatements(ValidatorResult)` default 方法，将子语句校验逻辑从 `YangBuiltInStatementImpl.initSelf()` 提取至接口层。利用 Java 8 default method，所有 `implements YangBuiltinStatement` 的类自动获得此能力，**不依赖继承链**。

然后在 7 个中间抽象层（`DataDefinitionImpl`、`OperationImpl`、`SchemaDataNodeImpl`、`DataNodeImpl`、`TypedDataNodeImpl`、`ContainerDataNodeImpl`、`InputImpl`/`OutputImpl`/`NotificationImpl`）的 `initSelf()` 末尾，通过 `instanceof YangBuiltinStatement` 守卫调用此方法。抽象类对应的接口不声明 `YangBuiltinStatement`，但它们的**具体子类**声明了——`instanceof` 在运行时正确判断。

```java
// 各抽象层 initSelf() 末尾的模式：
ValidatorResult processedResult = validatorResultBuilder.build();
if (this instanceof YangBuiltinStatement) {
    return ((YangBuiltinStatement) this).validateSubStatements(processedResult);
}
return processedResult;
```

`YangBuiltInStatementImpl.initSelf()` 也简化为一行 delegate：

```java
return ((YangBuiltinStatement) this).validateSubStatements(super.initSelf());
```

### 7.3 修改文件清单

| 文件 | 操作 | 说明 |
|---|---|---|
| `YangBuiltinStatement.java` | **修改** | 新增 `validateSubStatements(ValidatorResult)` default 方法，提取自 YangBuiltInStatementImpl |
| `YangBuiltInStatementImpl.java` | **修改** | `initSelf()` 简化为 delegate 调用 + 清理无用 import |
| `DataDefinitionImpl.java` | **修改** | `initSelf()` 末尾加 `instanceof` 守卫 + 调用 validateSubStatements |
| `OperationImpl.java` | **修改** | 同上 |
| `SchemaDataNodeImpl.java` | **修改** | 同上 |
| `DataNodeImpl.java` | **修改** | 同上 |
| `TypedDataNodeImpl.java` | **修改** | 同上 |
| `ContainerDataNodeImpl.java` | **修改** | 同上 |
| `InputImpl.java` | **修改** | 同上 |
| `OutputImpl.java` | **修改** | 同上 |
| `NotificationImpl.java` | **修改** | 同上 |

### 7.4 被修复的具体类

`ContainerImpl`、`LeafImpl`、`ListImpl`、`LeafListImpl`、`ChoiceImpl`、`CaseImpl`、`AnyDataImpl`、`AnyxmlImpl`、`InputImpl`、`OutputImpl`、`NotificationImpl`、`RpcImpl`、`ActionImpl`、`AugmentImpl`、`UsesImpl` — 共 15 个类。

### 7.5 回归测试

```
yangkit-parser         : 10/10   ✅ (含 EnumWhenTest)
yangkit-xpath-impl     :  4/4    ✅
yangkit-data-impl      :  1/1    ✅
yangkit-data-xml-codec : 29/29   ✅
yangkit-data-json-codec: 351/351 ✅
yangkit-data-proto-codec: 71/71  ✅
yangkit-data-cbor-codec: 55/55   ✅
```

总计 521 个测试全部通过，零回归。
