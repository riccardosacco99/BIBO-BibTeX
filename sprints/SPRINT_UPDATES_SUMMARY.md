# Sprint Updates Summary - Post Professor Feedback

**Date:** 2025-11-07
**Status:** Documentation Complete, Ready for Implementation

---

## Executive Summary

Professor feedback resulted in **critical architectural changes** requiring insertion of **Sprint 00 (Hot Fixes)** before Sprint 01.

### Impact:
- ✅ 4 positive feedback points confirmed good direction
- 🔴 4 critical issues identified (BLOCKING)
- 🟡 3 important improvements needed
- 🔵 2 strategic directions clarified

### Timeline Impact:
- **+1 week** added to overall timeline
- Sprint 01 pushed from Week 1-3 → Week 2-4
- Sprint 02 pushed from Week 4-5 → Week 5-6
- Sprint 03 pushed from Week 6-8 → Week 7-9

---

## What Changed

### Documents Created
1. ✅ **PROFESSOR_FEEDBACK_ANALYSIS.md** - Detailed analysis of all feedback points
2. ✅ **sprint-00-hot-fixes.md** - Complete Sprint 00 plan (38 story points)
3. ✅ **ROADMAP.md** - Updated with Sprint 00 and adjusted timelines
4. ✅ **SPRINT_UPDATES_SUMMARY.md** - This document

### Documents To Update Next
- [ ] **sprint-01-critical-mvp.md** - Add PapersDB testing tasks
- [ ] **sprint-03-advanced-features.md** - Rename to sprint-03-vocbench-integration.md, add US-23, US-24
- [ ] **CLAUDE.md** - Remove bibo:sequence references, document fixes
- [ ] **sprint-backlog.md** - Add Sprint 00 items

---

## Critical Issues (Sprint 00)

### 🔴 FIX-01: bibo:sequence Doesn't Exist (12 story points)

**Problem:**
We invented `bibo:sequence` property - it's not part of BIBO ontology!

**Solution:**
Replace with **RDF Lists** (standard RDF mechanism for ordered data).

**Impact:**
- Breaks all existing RDF output
- Must update all tests
- Must update reverse conversion
- BLOCKING all other work

**Example Before:**
```xml
<rdf:Description rdf:nodeID="author1">
  <foaf:name>John Smith</foaf:name>
  <bibo:sequence>0</bibo:sequence>  <!-- WRONG! -->
</rdf:Description>
```

**Example After:**
```turtle
ex:publication bibo:authorList (
    [ a foaf:Person ; foaf:name "John Smith" ]
    [ a foaf:Person ; foaf:name "Jane Doe" ]
) .
```

---

### 🔴 FIX-02: BibTeX Escape Characters (8 story points)

**Problem:**
Names like `Guasch-Ferr{\'e}` stay escaped in RDF output.

**Solution:**
Convert BibTeX LaTeX escapes to Unicode: `{\'e}` → `é`

**Impact:**
- Create `BibTeXUnicodeConverter` utility class
- Apply to all text fields during import
- 50+ escape sequences to support
- Affects data quality and presentation

**Common Escapes:**
```
{\'e} → é    {\`a} → à    {\"u} → ü    {\^o} → ô
{\~n} → ñ    {\c{c}} → ç  {\aa} → å    {\o} → ø
```

---

### 🔴 FIX-04: Missing InProceedings/Proceedings (13 story points)

**Problem:**
No support for conference papers (@inproceedings) or proceedings (@proceedings) - **VERY common** in academia!

**Solution:**
Add document types and implement proceedings modeling.

**Impact:**
- Add PROCEEDINGS, update CONFERENCE_PAPER handling
- Handle @inproceedings, @proceedings, @incollection
- Model proceedings as separate BIBO resources
- Test with professor's PapersDB examples

**BibTeX Types to Support:**
- `@inproceedings` - conference paper
- `@conference` - alias for @inproceedings
- `@proceedings` - entire conference proceedings
- `@incollection` - chapter in edited collection
- `@inbook` - part of book

---

### 🟡 FIX-03: Turtle as Default Format (5 story points)

**Problem:**
Using RDF/XML as default - verbose, less readable, shows distributed descriptions.

**Solution:**
Configure Turtle as default with pretty-print settings.

**Impact:**
- Better readability for testing/debugging
- Convert all test data from RDF/XML to Turtle
- Update examples to show Turtle
- Configure writer with optimal settings

---

## Important Improvements (Sprint 01)

### 1. Test with Complex Examples
Professor provided real-world BibTeX files:
- `PapersDB_MIUR.bib` - InCollection examples
- `PapersDB.bib` - Large collection (327KB, many types)

**Action:** Add comprehensive testing with these files in Sprint 01.

---

### 2. Proceedings as Books
Some proceedings have:
- Series (LNCS, LNAI, etc.)
- Volume numbers
- ISBN
- Publisher

**Question for Professor:** Model as separate proceedings resource or inline?

---

## Strategic Directions (Sprint 03)

### VocBench Lifter/Reformatter Architecture

Professor clarified: integrate as **Lifter + Reformatter**, not generic plugin.

**RDF Lifter:**
- Import: BibTeX file → BIBO RDF
- Interface: `it.uniroma2.art.semanticturkey.extension.RDFLifter`
- Endpoint: VocBench "Import" function

**Reformatting Exporter:**
- Export: BIBO RDF → BibTeX file
- Interface: `it.uniroma2.art.semanticturkey.extension.ReformattingExporter`
- Endpoint: VocBench "Export" function

**References:**
- Lifter docs: https://semanticturkey.uniroma2.it/doc/sys/rdf_lifter.jsf
- Exporter docs: https://semanticturkey.uniroma2.it/doc/sys/reformatting_exporter.jsf
- Semantic Turkey repo: https://bitbucket.org/art-uniroma2/semantic-turkey/src/master/

**New User Stories:**
- US-23: VocBench RDF Lifter (13 pts)
- US-24: VocBench Reformatting Exporter (13 pts)

---

## What's Correct (No Changes Needed)

### ✅ Blank Nodes for Authors
Professor asked if we use blank nodes - **we already do!**

```java
Resource person = VF.createBNode();  // ✅ Correct!
```

This is the right approach since we don't know global author identity.

---

### ✅ Round-Trip Testing
Professor confirmed 100% accuracy (modulo attribute order).
This validates our approach - keep doing this!

---

### ✅ Using RDF4J
We are using RDF4J properly (not manual string formatting).
The distributed descriptions in RDF/XML are just how RDF4J serializes - valid but not pretty.

---

## Updated Timeline

```
BEFORE (8 weeks):
Week 1-3:  Sprint 01
Week 4-5:  Sprint 02
Week 6-8:  Sprint 03

AFTER (9 weeks):
Week 1:    Sprint 00 (HOT FIXES) 🆕
Week 2-4:  Sprint 01
Week 5-6:  Sprint 02
Week 7-9:  Sprint 03
```

---

## Story Point Summary

| Sprint | Original | Updated | Change | Reason |
|--------|----------|---------|--------|--------|
| Sprint 00 | - | **38 pts** | +38 | New sprint for hot fixes |
| Sprint 01 | 46 pts | 46 pts | 0 | No change, but testing expanded |
| Sprint 02 | 41 pts | 41 pts | 0 | No change |
| Sprint 03 | 42 pts | **51 pts** | +9 | Added US-23, US-24 for Lifter/Reformatter |
| **Total** | **129 pts** | **176 pts** | **+47** | **36% increase** |

---

## Risk Assessment

### Sprint 00 Risks 🔴 HIGH

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| RDF Lists API complex | Medium | High | Study RDF4J docs, start with examples |
| Breaking tests | High | High | Update incrementally, keep backups |
| Unicode map incomplete | Medium | Medium | Research thoroughly, can expand later |
| InProceedings too complex | Medium | High | Start simple, get professor feedback |
| Time estimate too low | Medium | High | Prioritize FIX-01/02, FIX-04 can extend |

**Mitigation Strategy:**
- **Do NOT rush** - correctness > speed
- Get professor feedback after FIX-01 done
- Can parallelize FIX-02 and FIX-03 (independent)
- If time runs out, FIX-04 can continue into Sprint 01

---

## Action Items

### Immediate (This Week)
1. ⏳ Review Sprint 00 plan with professor if possible
2. ⏳ Begin FIX-01 (RDF Lists) - HIGHEST PRIORITY
3. ⏳ Research RDF4J Collections API
4. ⏳ Research BibTeX escape sequences
5. ⏳ Copy PapersDB files to project

### Week 1 (Sprint 00)
1. ⏳ Complete FIX-01 (RDF Lists)
2. ⏳ Complete FIX-02 (Unicode)
3. ⏳ Complete FIX-03 (Turtle)
4. ⏳ Start FIX-04 (InProceedings)
5. ⏳ Update all tests
6. ⏳ Get professor approval

### Week 2+ (Sprint 01)
1. ⏳ Complete FIX-04 if not done
2. ⏳ Resume Sprint 01 as planned
3. ⏳ Test with PapersDB examples

---

## Success Criteria

Sprint 00 is successful when:

1. ✅ **Zero** references to `bibo:sequence` anywhere
2. ✅ Authors stored in RDF Lists with preserved order
3. ✅ Unicode characters display correctly (é not {\'e})
4. ✅ Turtle is default, RDF/XML available as option
5. ✅ @inproceedings and @proceedings convert successfully
6. ✅ Round-trip tests maintain 100% accuracy
7. ✅ All existing tests updated and passing
8. ✅ Professor reviews and approves changes

---

## Questions for Next Professor Meeting

1. ❓ InProceedings modeling: always separate proceedings resource?
2. ❓ How to handle proceedings that are also books (series, volume, ISBN)?
3. ❓ Are there BIBO community guidelines for proceedings?
4. ❓ Should we validate strictly (fail) or leniently (warn)?
5. ❓ Which VocBench version to target for integration?

---

## Files to Read

### Professor's Examples
- `/Users/riccardosacco/Downloads/PapersDB_MIUR.bib` - InCollection examples
- `/Users/riccardosacco/Downloads/PapersDB.bib` - Large collection (need to copy to project)

### Current Project Files to Fix
- `core/src/main/java/it/riccardosacco/bibobibtex/model/bibo/BiboVocabulary.java:50` - Remove ORDER
- `core/src/main/java/it/riccardosacco/bibobibtex/model/bibo/BiboDocument.java:368` - Replace with RDF Lists
- `core/src/main/java/it/riccardosacco/bibobibtex/examples/ReverseConversion.java:164` - Update reading
- All files in `test-data/bibo/*.rdf` - Convert to Turtle

---

## Next Steps

1. **Read and understand:**
   - RDF4J Collections API documentation
   - BibTeX escape sequences reference
   - VocBench Lifter/Reformatter architecture

2. **Begin implementation:**
   - Start with FIX-01 (most critical)
   - Create feature branch: `feature/sprint-00-hot-fixes`
   - Commit frequently with clear messages

3. **Testing:**
   - Update tests as you go
   - Run full test suite after each fix
   - Verify round-trip accuracy maintained

4. **Communication:**
   - Get professor feedback early and often
   - Document decisions in code comments
   - Update CLAUDE.md as you learn

---

## Conclusion

The professor's feedback identified **critical issues** that must be fixed immediately:

1. 🔴 **Invalid RDF** (bibo:sequence doesn't exist)
2. 🔴 **Poor data quality** (escape characters)
3. 🔴 **Limited type support** (missing common academic types)
4. 🟡 **Suboptimal format** (RDF/XML instead of Turtle)

**Sprint 00 (1 week)** addresses these before continuing with thesis work.

**Good news:**
- Foundation is solid (round-trip works!)
- Using RDF4J correctly
- Already using blank nodes correctly
- Direction confirmed as correct

**The Path Forward:**
1. Fix architectural issues (Sprint 00)
2. Complete thesis MVP (Sprint 01)
3. Production quality (Sprint 02)
4. VocBench integration (Sprint 03)

**Timeline:** 9 weeks total (was 8, +1 for fixes)

---

**Document Owner:** Riccardo Sacco
**Last Updated:** 2025-11-07
**Status:** READY FOR SPRINT 00
**Next Action:** Begin FIX-01 (RDF Lists)
