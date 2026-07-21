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

**建议:** 重新审视 anydata validation 的"支持"标准定义——"不丢数据"和"能验证数据"是两个不同的能力，前者是 anyxml 的行为，后者才是 anydata 的行为。

## 2. Anydata inside Structures — 同上

此项 Partially Supported 评价是上述 anydata validation 问题在 structure 上下文中的延伸，反馈同上。

## 3. XPath / Data Extraction — "Partially Supported"

**Slides 结论:** yangkit XPath/data extraction 为 Partially Supported

**待确认:** 需要了解具体哪些 XPath 测试用例未通过。Yangkit 基于 Jaxen 实现了完整的 XPath 1.0 引擎，并扩展了 YANG 特有函数（current(), deref(), derived-from(), derived-from-or-self(), enum-value(), re-match(), bit-is-set()）。

可能的差距：
- Schema 树上的 XPath 查询（yangkit 的 XPath navigator 面向数据树，非 schema 树）
- XPath 结果集的结构化 API

**建议:** 提供具体失败的测试用例，以便准确定位差距。

## 4. Add New Schema Node — "Not Supported"

**Slides 结论:** yangkit 不支持动态添加 schema 节点

**反馈:** 这是架构层面的设计选择。Yangkit 将 schema 编译和数据验证严格分层，schema 树一旦编译完成即不可变，保证并发安全性和验证可靠性。这在大多数使用场景下是合理的。

## 5. Update Existing Data Node — "Not Supported"

**Slides 结论:** yangkit 不支持更新现有数据节点（slides 注明 "can easily be changed in code source"）

**反馈:** `LeafData.setValue()` 方法已存在。缺少的是一个高层的 update+re-validate 便捷 API。可以考虑补充。

## 6. Schema Comparison — "Implementable"

**反馈:** 无异议。yangkit 具备所有基础设施（schema 内省 + 数据比较），但尚未实现 `draft-ietf-netmod-yang-schema-comparison` 的专门 schema diff 算法。
