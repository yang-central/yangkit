# Email Draft

**To:** Vivekananda Boudia <vivekananda.boudia@insa-lyon.fr>, Pierre Francois <pierre.francois@insa-lyon.fr>, Maxence Younsi <maxence.younsi@insa-lyon.fr>
**CC:** netmod@ietf.org
**Subject:** Feedback on "YANG Libraries Feature Comparison" (IETF 126) — from the yangkit author

---

Hi Vivekananda, Pierre, and Maxence,

Thank you for the "YANG Libraries Feature Comparison" presentation at IETF 126 NETMOD. As the author and maintainer of yangkit, I appreciate the effort to systematically evaluate YANG libraries — this kind of cross-implementation comparison is valuable for the community.

That said, I'd like to raise a few points about the yangkit evaluation results for discussion. I've reviewed the test cases from the yang_tests repository and believe some of the ratings may need revisiting.

### 1. Anydata Validation — rated "Partially Supported"

RFC 7950 Section 7.10 draws a clear distinction between `anydata` and `anyxml`: anydata represents "an unknown set of nodes that can be modeled with YANG," while anyxml can contain arbitrary data that need not conform to any YANG model.

Yangkit requires an explicit schema context to be registered (via `AnydataValidationOptions`) before validating anydata payload. This is a deliberate design choice that reflects the semantics of anydata: without a schema, anydata content simply cannot be validated — that's the nature of anydata, not an implementation gap.

If a library preserves anydata payload as opaque data without schema-based validation (treating it like anyxml), that is data preservation, not validation.

Looking at the test cases, I also note that several have unresolved questions from the testers themselves — for example, `testPrimitiveTypeAnydata()` is annotated with "payload is primitive, invalid - in strict validation of yangkit, why is it succ?" and `testNullAnydata()` has "Heng - is this actually valid?" referencing RFC 7951 Sec.5.5. When the expected behavior is unclear even to the test authors, using these tests to rate a library seems premature.

I'd suggest we discuss what "supported" should mean for anydata validation — the distinction between "retaining unvalidated data" and "actually validating against a schema" seems important.

The same reasoning applies to the "Anydata inside structures" rating.

### 2. XPath / Data Extraction — rated "Partially Supported"

In RFC 7950, XPath is used exclusively for `when`, `must`, `leafref path`, and `instance-identifier` — all of which evaluate over the **data tree**. YANG XPath paths are data node paths that skip schema-only nodes like `choice` and `case`, which makes XPath and schema path fundamentally different addressing systems.

Yangkit implements a complete XPath 1.0 engine (based on Jaxen) with all YANG-specific extension functions: `current()`, `deref()`, `derived-from()`, `derived-from-or-self()`, `enum-value()`, `re-match()`, and `bit-is-set()`. This fully covers what RFC 7950 requires of XPath.

Looking at the test cases, the only "failure" appears to be `anydataXpathTest()`, where XPath cannot traverse into anydata payload content (the TODO note says "'value' returns '' instead of 'router1'"). This is expected behavior — anydata content without a registered schema is not part of the XPath-navigable data tree. This is again the same anydata schema issue, not an XPath engine limitation.

Using XPath to query schema nodes (as libyang's `lys_find_xpath()` does) is an extra convenience beyond the YANG specification. It should not be used as the evaluation criterion for a YANG library's XPath support.

### 3. Add New Schema Node — rated "Not Supported"

yangkit-model does support dynamically adding, removing, and modifying schema nodes — but this is a design-time capability, used during model editing and compilation. The API `SchemaNodeContainer.addSchemaNodeChild()` is designed for this purpose.

For data validation (yangkit-data), the schema must be immutable. Mutating the schema at runtime during validation is fundamentally unsound — it moves the goalpost while the game is being played. If a new schema is needed, the correct approach is to re-run model parsing and build a new schema context.

The test appears to conflate two distinct concerns: model editing (design-time, supported by yangkit-model) and data validation (runtime, where schema immutability is a correct design choice). I'd suggest evaluating these capabilities separately.

### 4. Update Existing Data Node — rated "Not Supported"

This is where reviewing the test cases was most illuminating. In `updateValueTest()`, the test code attempts to update a data node by calling `addChild()` with a new node having the same key — it does **not** use the `LeafData.setValue()` API, which is the correct way to update a value in yangkit.

The correct approach is:

```java
leafData.setValue(newValue);
// Then validate when ready:
dataDocument.validate();
```

This decoupled design is intentional — it enables efficient batch updates where intermediate states may be temporarily invalid, with a single validation pass after all modifications are complete.

The slides themselves note "can easily be changed in code source," which acknowledges this is not a missing capability. A "Not Supported" rating based on incorrect API usage seems inconsistent with that annotation.

### 5. Schema Comparison — rated "Implementable"

yang-comparator (https://github.com/yang-central/yang-comparator) is a mature, production-proven tool built on yangkit that performs semantic-level schema tree comparison — not text-level diffing. It compiles two versions of YANG modules into their respective schema trees and performs structural diff at the schema level (node additions/deletions/modifications, type changes, constraint changes, etc.).

Interestingly, the test repository contains `YangkitSchemaComparator.java` — a schema comparator written from scratch by the testers using yangkit's own APIs. This implementation includes node add/delete/modify detection, type change comparison, constraint change evaluation, and backward compatibility assessment. The fact that your team was able to build a working schema comparator using yangkit's APIs demonstrates the capability exists. Yet yangkit was rated "Implementable" (implying not yet implemented), while the testers' own code proves otherwise — and the dedicated yang-comparator tool was not evaluated at all.

This schema comparison capability is the reason yangkit was adopted by Swisscom for YANG model version management, and one of the reasons I joined the IETF YANG Module Versioning Design Team.

---

I want to emphasize that this feedback is intended to be constructive. The comparison project is a great initiative, and I'd be happy to collaborate — whether that means helping refine the test methodology, providing guidance on yangkit APIs, or discussing what the evaluation criteria should be for each feature category.

I think the most productive next step would be to review the specific test cases together and distinguish between:
- Actual capability gaps in yangkit
- Test methodology issues (e.g., incorrect API usage in `updateValueTest`)
- Philosophical differences in what "supported" means (e.g., anydata validation semantics)

Would you be open to a discussion on this?

Best regards,
Frank
