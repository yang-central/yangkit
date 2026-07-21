# Email Draft

**To:** Vivekananda Boudia <vivekananda.boudia@insa-lyon.fr>, Pierre Francois <pierre.francois@insa-lyon.fr>, Maxence Younsi <maxence.younsi@insa-lyon.fr>
**CC:** netmod@ietf.org
**Subject:** Feedback on "YANG Libraries Feature Comparison" (IETF 126) — from the yangkit author

---

Hi Vivekananda, Pierre, and Maxence,

Thank you for the "YANG Libraries Feature Comparison" presentation at IETF 126 NETMOD. As the author and maintainer of yangkit, I'm delighted to see this kind of systematic, cross-implementation comparison — it is exactly the type of work that benefits the entire YANG ecosystem.

I'd like to offer some clarifications on a few of the yangkit evaluation results, and more broadly, to suggest that the community could benefit from developing a shared set of test criteria together.

### 1. Anydata Validation — rated "Partially Supported"

As the original proposer of the `anydata` statement during the YANG 1.1 revision (the proposal was accepted into RFC 7950), I'd like to clarify its intended semantics.

RFC 7950 Section 7.10 distinguishes `anydata` from `anyxml`: anydata represents "an unknown set of nodes that can be modeled with YANG," meaning its content must conform to some YANG model — we just don't know which one at compile time. Yangkit requires an explicit schema context (via `AnydataValidationOptions`) before validating anydata payload, which directly reflects this semantics.

There is an important distinction between "preserving anydata payload as opaque data" and "validating anydata payload against a schema." Both are useful capabilities, but they serve different purposes. It would be valuable for the community to discuss and agree on what "anydata validation support" should mean in the context of a feature comparison.

The same applies to the "Anydata inside structures" rating.

### 2. XPath / Data Extraction — rated "Partially Supported"

Yangkit implements a complete XPath 1.0 engine (based on Jaxen) with all YANG-specific extension functions defined in RFC 7950: `current()`, `deref()`, `derived-from()`, `derived-from-or-self()`, `enum-value()`, `re-match()`, and `bit-is-set()`.

In RFC 7950, XPath is used for `when`, `must`, `leafref path`, and `instance-identifier` — all of which evaluate over the data tree. YANG XPath paths are data node paths that skip schema-only constructs like `choice` and `case`, making them fundamentally different from schema paths.

It's also worth noting that RFC 7950 defines anydata as an opaque, indivisible node in the data tree — it has no addressable child nodes. XPath path steps cannot enter anydata internals regardless of implementation; this is inherent to the data model defined by the specification.

I'd be interested to understand which specific test scenarios yangkit did not pass, so we can determine whether the gap is in RFC-mandated functionality or in capabilities beyond the specification scope.

### 3. Add New Schema Node — rated "Not Supported"

yangkit-model supports dynamically adding, removing, and modifying schema nodes via APIs such as `SchemaNodeContainer.addSchemaNodeChild()` — this is a design-time capability for model editing and compilation.

For data validation (yangkit-data), the schema is treated as immutable, which is a deliberate design choice: data validation requires a stable reference point. If a new schema is needed at runtime, the approach is to re-run model parsing and build a new schema context.

These are two distinct capabilities — model editing and data validation — and it may be useful to evaluate them separately.

### 4. Update Existing Data Node — rated "Not Supported"

Yangkit supports updating data node values through the `LeafData.setValue()` API. After modification, calling `validate()` re-validates the data tree. This decoupled design enables efficient batch updates — multiple nodes can be modified without triggering validation on each intermediate (potentially invalid) state, with a single validation pass after all changes are complete.

I noticed the slides annotate this item with "can easily be changed in code source," which suggests the capability is recognized. I'd be happy to provide more guidance on the intended API usage patterns if that would be helpful.

### 5. Schema Comparison — rated "Implementable"

yang-comparator (https://github.com/yang-central/yang-comparator) is a tool built on yangkit that performs semantic-level schema tree comparison. It compiles two versions of YANG modules into their respective schema trees and performs structural diff at the schema level — node additions/deletions/modifications, type changes, constraint changes, and so on.

While its output format may not yet fully align with `draft-ietf-netmod-yang-schema-comparison`, the core schema comparison capability is implemented and has been used in production. I'd welcome discussion on what output format or criteria the comparison should use.

---

I want to emphasize that this feedback is meant to be constructive, and I think your work points to something the community needs: **a shared, open set of test cases and evaluation criteria for YANG libraries.** Different libraries make different design choices, and having an agreed-upon test specification would make comparisons fairer and more useful for everyone.

Would you be open to collaborating on this? I'd be happy to contribute — whether that means helping refine test cases, providing guidance on yangkit APIs, or working together on a community-driven test specification that the NETMOD WG could adopt.

Best regards,
Frank
