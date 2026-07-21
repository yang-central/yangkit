# Feedback on IETF 126 YANG Libraries Feature Comparison

Slides: https://datatracker.ietf.org/meeting/126/materials/slides-126-netmod-yang-libraries-feature-comparison-00
Test repo: https://github.com/network-analytics/yang_tests
Authors: Vivekananda Boudia, Pierre Francois, Maxence Younsi (INSA Lyon)

---

## 1. Anydata Validation — "Partially Supported" 评价有误

**Slides 结论:** yangkit anydata validation 为 Partially Supported

**反馈:**

RFC 7950 Section 7.10 明确区分了 anydata 和 anyxml：
- anydata: "an unknown set of nodes that **can be modeled with YANG**"
- anyxml: 不需要符合任何 YANG 模型的任意数据

Anydata 的内容**必须有对应的 YANG 模型**，只是在编译时不确定是哪个模型。如果没有 schema，anydata 在语义上就是无法验证的——这不是实现缺陷，而是 anydata 的本质。

Yangkit 要求通过 `AnydataValidationOptions` 显式注册 payload schema 才能验证 anydata payload，这恰恰体现了 anydata 的核心语义。

Libyang 在没有 schema 的情况下将 anydata payload 保留为 opaque 数据（类似 anyxml），这并不是 validation，而是 preservation。测试中的 "primitive values, unknown objects, arrays, null values" 场景如果不提供 schema，根本就不属于 anydata validation 的范畴。

**测试用例证据:**
- `testPrimitiveTypeAnydata()`: 测试者自己标注 TODO — "payload is primitive, invalid - in strict validation of yangkit, why is it succ?" — 测试者对预期行为不确定
- `testNullAnydata()`: 测试者标注 TODO — "Heng - is this actually valid?" 引用 RFC 7951 Sec.5.5 — 预期结果存疑
- 多个测试在不提供 schema 的情况下测试 anydata validation，这本身就偏离了 anydata 的语义

**建议:** 重新审视 anydata validation 的"支持"标准定义——"不丢数据"和"能验证数据"是两个不同的能力，前者是 anyxml 的行为，后者才是 anydata 的行为。

## 2. Anydata inside Structures — 同上

此项 Partially Supported 评价是上述 anydata validation 问题在 structure 上下文中的延伸，反馈同上。

`YangAnydataStructureTest.java` 中的 7 个测试用例（primitive, object-with/without-schema, null, array, null-object, structure-in-anydata）存在同样的问题。

## 3. XPath / Data Extraction — "Partially Supported" 评价有误

**Slides 结论:** yangkit XPath/data extraction 为 Partially Supported

**反馈:**

RFC 7950 中 XPath 的使用场景非常明确：when（条件性存在约束）、must（数据验证约束）、leafref path（叶节点引用）、instance-identifier（实例标识）。这些全部是在**数据树**上求值的。

YANG XPath 表达式中的路径是 data node path，跳过 choice/case 等 schema-only 节点，这决定了 XPath 和 schema path 是两套不同的寻址体系。用 XPath 查询 schema 节点（如 libyang 的 `lys_find_xpath()`）是 libyang 提供的额外便利功能，不是 YANG 规范对 XPath 的要求。

Yangkit 基于 Jaxen 实现了完整的 XPath 1.0 引擎，并扩展了 YANG 特有函数（current(), deref(), derived-from(), derived-from-or-self(), enum-value(), re-match(), bit-is-set()），完整覆盖了 RFC 7950 对 XPath 的所有要求。yangkit-xpath 模块的职责就是 RFC 7950 定义的 XPath 数据树求值，它完整实现了这个职责。

至于 `evaluate()` 返回 List/String/Number/Boolean，这正是 XPath 1.0 规范定义的四种结果类型（node-set, string, number, boolean），不存在"结构化结果集"缺失的问题。

**测试用例证据:**
- `XPathExtractionTest.java` 中 4 个测试：
  - `minimalXpathTest()` — 正常通过
  - `structureXpathTest()` — 正常通过
  - `nonExistentXpathTest()` — 正确返回空字符串
  - `anydataXpathTest()` — TODO: "'value' returns '' instead of 'router1'" — 唯一的"失败"是 XPath 无法穿透 anydata payload，这又回到了 anydata 必须有 schema 的问题

**建议:** 如果测试期望的是 schema 树上的 XPath 查询能力，那属于超出 YANG XPath 规范定义的额外功能，不应作为 YANG library 的 XPath 支持评价标准。

## 4. Add New Schema Node — "Not Supported" 评价不公平

**Slides 结论:** yangkit 不支持动态添加 schema 节点

**反馈:**

首先，yangkit-model **支持动态增删改 schema 节点**，但这是在**设计态**（模型编辑/编译阶段）工作的能力，`SchemaNodeContainer.addSchemaNodeChild()` 等 API 就是为此设计的。

其次，对于 yangkit-data（数据验证）来说，基于确定的 schema 来验证数据正确性是基本前提。在运行时改变 schema 意味着在验证过程中移动标准线，这在数据验证场景下是不可接受的。如果需要使用新的 schema，正确的做法是重新运行一次模型解析，构建新的 schema context，而不是在已有 context 上动态修改。

这个测试实际上混淆了两个不同的职责：
- **模型编辑/编译**（设计态）：yangkit-model 支持，可以动态增删改节点
- **数据验证**（运行态）：yangkit-data 基于不可变的 schema 进行验证，这是正确的设计

**建议:** 如果测试的目的是评估"模型编辑能力"，应当在设计态下测试 yangkit-model 的 API，而非在数据验证的上下文中评价。

## 5. Update Existing Data Node — "Not Supported" 评价有误

**Slides 结论:** yangkit 不支持更新现有数据节点（slides 注明 "can easily be changed in code source"）

**反馈:**

Yangkit 完全支持更新数据节点值。`LeafData.setValue()` 方法可以直接修改节点值，修改后调用 `validate()` 即可重新验证。这是 yangkit 的设计理念：**数据操作和验证是解耦的**，用户可以自由修改数据树，然后在需要时统一触发验证。

这种设计比"每次 setValue 自动触发验证"更灵活——批量修改多个节点时，不需要每改一个就验证一次（中间状态可能本来就不合法），而是在所有修改完成后统一验证一次。

**测试用例证据:**
`updateValueTest()` 中的代码尝试通过 `container.addChild(newLeafNode)` 传入同名新节点来"更新"值——**测试者根本没有使用 `LeafData.setValue()` API**。正确的做法是：
```java
leafData.setValue(newValue);
dataDocument.validate();
```
这是 API 误用，不是功能缺失。

Slides 自己也标注了 "can easily be changed in code source"，说明测试者也认识到这不是能力缺失，评为 "Not Supported" 与自身注释矛盾。

## 6. Schema Comparison — "Implementable" 评价有误

**Slides 结论:** yangkit 的 schema comparison 为 Implementable（尚未实现）

**反馈:**

Yang-comparator（https://github.com/yang-central/yang-comparator）是基于 yangkit schema 树的语义级比较工具，而非文本级比较。它通过 yangkit-model 编译两个版本的 YANG 模块，构建各自的 schema 树，然后在 schema 树层面进行语义级 diff（节点增删改、类型变更、约束变更等）。

正是因为这个 schema comparison 能力，yangkit 被瑞士电信（Swisscom）选中用于 YANG 模型版本管理，也是 yangkit 作者参与 IETF YANG Module Versioning Design Team 的原因之一。

**测试用例证据:**
测试仓库中包含 `YangkitSchemaComparator.java`——测试者**自己使用 yangkit API 从零实现了一个 schema comparator**，包括：
- 节点增删改检测（indexEffectiveSchemaTree + compareNode）
- 类型变更比较
- 约束变更评估（config, mandatory, status, default, min-elements, max-elements, presence, when, must）
- 向后兼容性评估

这个事实本身就证明了：
1. yangkit 的 schema 内省能力足够强大，足以构建完整的 schema comparison
2. 测试者没有使用已有的 yang-comparator，而是自己重写了一个
3. 评价 yangkit 的 schema comparison 为 "Implementable"（暗示尚未实现）与测试者自己的代码自相矛盾

Yang-comparator 已经是一个成熟的、经过生产环境验证的 schema comparison 实现。至于 `draft-ietf-netmod-yang-schema-comparison` 定义的标准化输出格式，yang-comparator 的输出格式可能与之不完全一致，但 schema comparison 的核心能力是具备的。适配特定输出格式是工程层面的工作，不影响"是否支持 schema comparison"的评价。
