# BIBO-BibTeX Converter - Project Roadmap

- **Last Updated:** 2025-11-07 (Post-Professor Feedback)
- **Project Status:** v0.1.0-SNAPSHOT (Thesis Phase)
- **Current Phase:** 🔥 Sprint 00 - Critical Hot Fixes

---

## ⚠️ BREAKING: Sprint 0 Inserted (Professor Feedback)

After professor review, **4 critical issues** identified requiring immediate fixes:
1. 🔴 `bibo:sequence` property doesn't exist - invented by us!
2. 🔴 BibTeX escape characters not converted to Unicode
3. 🔴 Missing InProceedings/Proceedings support (VERY common)
4. 🟡 Turtle should be default RDF format

**Action:** Inserted **Sprint 00** before Sprint 01 to fix these issues.

See: `sprint-00-hot-fixes.md`

---

## Project Timeline

```
Timeline:

Sprint 00 - Critical Hot Fixes
├─ FIX-01: Remove bibo:sequence, use RDF Lists
├─ FIX-02: BibTeX → Unicode conversion
├─ FIX-03: Turtle as default format
└─ FIX-04: InProceedings/Proceedings support

Sprint 01 - Critical MVP/Thesis
├─ VocBench Integration (US-01)
├─ Input Validation (US-02)
├─ Extended Fields (US-03)
├─ Edge Case Testing (US-04)
├─ Round-Trip Tests (US-05)
└─ Documentation (US-06)

Sprint 02 - Production Quality
├─ Complete Type Support (US-08)
├─ Name Parsing (US-09)
├─ Identifier Validation (US-11)
├─ JavaDoc (US-12)
└─ Performance (US-13)

Sprint 03 - VocBench Integration
├─ VocBench Lifter (US-23)
├─ VocBench Reformatter (US-24)
├─ Duplicate Detection (US-17)
├─ Deployment Packaging (US-21)
└─ Integration Tests (US-22)

Post-Release
├─ Additional Formats
├─ Web Interface
└─ Community Features
```

---

## Current Status (v0.1.0-SNAPSHOT)

### ✅ Completed (70% of Core - Pre-Hot Fixes)

**Converter Functionality:**
- ✅ Bidirectional BibTeX ↔ BIBO conversion
- ⚠️ Support for 8 document types (Article, Book, Conference Paper, etc.)
    - 🔴 **Missing:** InProceedings, Proceedings, InCollection (FIX-04)
- ✅ 20+ field mappings (title, authors, date, publisher, etc.)
- ✅ Identifier extraction (DOI, ISBN, ISSN, URL, Handle)
- ✅ Name parsing (basic)
    - 🔴 **Issue:** BibTeX escapes not converted (FIX-02)
- ✅ Date parsing with month aliases
- ✅ Citation key generation

**Model Layer:**
- ✅ Immutable BiboDocument with Builder pattern
- ⚠️ BIBO vocabulary support
    - 🔴 **Critical Issue:** Uses non-existent `bibo:sequence` (FIX-01)
- ✅ RDF4J integration
- ✅ Type-safe enums for types, roles, identifiers
- ⚠️ RDF serialization
    - 🔴 **Issue:** RDF/XML default, should use Turtle (FIX-03)

**Testing:**
- ✅ 7 passing unit tests
- ✅ Basic conversion tests
- ✅ Field mapping tests
- ✅ Model construction tests
- ✅ 100% round-trip accuracy (will re-verify after fixes)

**Infrastructure:**
- ✅ Maven multi-module project
- ✅ Clean separation of core and plugin
- ✅ Example conversion utilities

### 🔥 In Progress (Sprint 00 - Hot Fixes)

**Critical Fixes (BLOCKING):**
- 🔴 FIX-01: Replace bibo:sequence with RDF Lists (P0)
- 🔴 FIX-02: BibTeX Unicode conversion (P0)
- 🔴 FIX-03: Turtle as default format (P1)
- 🔴 FIX-04: InProceedings/Proceedings support (P0)

### 🚧 Pending (20%)

**VocBench Plugin:**
- 🚧 Skeleton classes created
- 🚧 Basic lifecycle defined
- ❌ No real repository integration
- ❌ No Lifter/Reformatter implementation (Sprint 03)
- ❌ No UI components

**Testing:**
- 🚧 Happy path covered
- ❌ Edge cases not tested (Sprint 01)
- ❌ No integration tests (Sprint 03)
- ❌ No performance tests (Sprint 02)
- ❌ Not tested with complex examples (Sprint 00, Sprint 01)

### ❌ Not Started

**Advanced Features:**
- ❌ Duplicate detection (Sprint 03)
- ❌ Conflict resolution (Sprint 03)
- ❌ Custom field mapping (Sprint 03)
- ❌ Multi-format RDF support (Sprint 02)
- ❌ Statistics and reporting (Sprint 02)
- ❌ Batch processing API (Post-release)

**Quality:**
- ❌ Limited JavaDoc (Sprint 02)
- ❌ No code coverage reports (Sprint 01)
- ❌ No static analysis (Sprint 02)
- ❌ Limited error handling (Sprint 01)

**Documentation:**
- ❌ No field mapping reference (Sprint 01)
- ❌ No API documentation (Sprint 02)
- ❌ No user guide (Sprint 03)
- ❌ No installation guide (Sprint 03)

---

## Roadmap

### 🔥 v0.2.0 - Hot Fixes (Sprint 00) **NEW**

**Goal:** Fix critical architectural issues before thesis work continues

**Must Have:**
- RDF Lists for author ordering (not bibo:sequence)
- BibTeX escape sequences → Unicode conversion
- InProceedings and Proceedings document types
- Turtle as default RDF serialization format
- Maintain 100% round-trip accuracy

**Success Criteria:**
- ✅ Zero references to non-existent `bibo:sequence`
- ✅ Author order preserved via RDF Lists
- ✅ Unicode characters display correctly (é not {\'e})
- ✅ Turtle output is pretty-printed and readable
- ✅ @inproceedings and @proceedings entries convert successfully
- ✅ All existing tests updated and passing
- ✅ Round-trip tests maintain 100% accuracy
- ✅ Professor reviews and approves

**Deliverables:**
- Fixed core converter JAR
- Updated test data (Turtle format)
- BibTeXUnicodeConverter utility class
- Test report showing all fixes validated

**Blockers Resolved:**
- 🔴 Invalid RDF (bibo:sequence doesn't exist)
- 🔴 Poor data quality (escape characters)
- 🔴 Limited academic type support

---

### 🎯 v0.5.0 - Thesis MVP (Sprint 01)

**Goal:** Thesis defense ready

**Must Have:**
- Real VocBench repository integration
- Comprehensive validation with clear errors
- Extended field support (series, edition, keywords)
- Edge case testing (70% coverage)
- Field mapping documentation

**Success Criteria:**
- ✅ All acceptance criteria met for US-01 to US-07
- ✅ Test coverage > 70%
- ✅ Documentation complete
- ✅ Demo-ready for thesis defense
- ✅ Build passing with 0 failures

**Deliverables:**
- Working VocBench plugin JAR
- Test report
- Field mapping documentation
- Thesis chapter on implementation

---

### 🎯 v0.8.0 - Production Quality (Sprint 02)

**Goal:** Production-ready code

**Must Have:**
- All 14 BibTeX entry types supported
- Robust name parsing (particles, suffixes)
- Identifier validation with checksums
- Complete JavaDoc (>90%)
- Performance optimization (1000 entries < 10s)

**Success Criteria:**
- ✅ All standard BibTeX types work
- ✅ Complex names handled correctly
- ✅ JavaDoc coverage > 90%
- ✅ Performance benchmarks met
- ✅ Code quality checks passing

**Deliverables:**
- Beta release JAR
- JavaDoc HTML
- Performance benchmark report

---

### 🎯 v1.0.0 - Production Release (Sprint 03)

**Goal:** Full VocBench Lifter/Reformatter integration, production deployment

**Must Have:**
- **VocBench RDF Lifter implementation** (BibTeX → BIBO) 🆕
- **VocBench Reformatting Exporter implementation** (BIBO → BibTeX) 🆕
- Duplicate detection and conflict resolution
- Production packaging and installation
- Integration tests with VocBench/Semantic Turkey
- Security audit passed

**Nice to Have:**
- Extended BIBO metadata
- Custom field mapping config
- Multi-format RDF support (JSON-LD, N-Triples)
- VocBench UI components (if needed)

**Success Criteria:**
- ✅ Implements VocBench `RDFLifter` interface correctly
- ✅ Implements VocBench `ReformattingExporter` interface correctly
- ✅ Installation tested on VocBench instance
- ✅ Lifter can import BibTeX files to VocBench repository
- ✅ Exporter can export BIBO data to BibTeX format
- ✅ Security audit clean
- ✅ Integration tests passing
- ✅ User documentation complete (how to install in VocBench)

**Deliverables:**
- Production JAR compatible with VocBench plugin system
- VocBench plugin descriptor/manifest
- Installation guide for VocBench administrators
- User guide with VocBench screenshots
- API documentation
- Release notes

**References:**
- VocBench I/O Extensions: https://vocbench.uniroma2.it/doc/user/ioext/
- RDF Lifters: https://semanticturkey.uniroma2.it/doc/sys/rdf_lifter.jsf
- Reformatting Exporters: https://semanticturkey.uniroma2.it/doc/sys/reformatting_exporter.jsf

---

### 🔮 v1.x - Future Enhancements (Post-Release)

**Potential Features:**
- 📝 Zotero RDF support
- 📝 CSL-JSON support
- 📝 Microsoft Word Bibliography integration
- 📝 Mendeley API integration
- 🌐 Web interface for online conversion
- 💻 CLI tool for scripting
- 🔍 GraphQL API
- 🔄 Bibliography merging with deduplication
- 🌍 Internationalization (i18n)
- 📊 Analytics dashboard

**Community-Driven:**
- GitHub issue tracking
- Pull request reviews
- Feature requests from users
- Bug fixes

---

## Feature Matrix

### By Release

| Feature | v0.1.0 | v0.5.0 | v0.8.0 | v1.0.0 | Future |
|---------|--------|--------|--------|--------|--------|
| **Core Conversion** |
| BibTeX → BIBO | ✅ | ✅ | ✅ | ✅ | ✅ |
| BIBO → BibTeX | ✅ | ✅ | ✅ | ✅ | ✅ |
| 8 Basic Types | ✅ | ✅ | ✅ | ✅ | ✅ |
| 14 Standard Types | ❌ | ❌ | ✅ | ✅ | ✅ |
| 20+ Fields | ✅ | ✅ | ✅ | ✅ | ✅ |
| 30+ Fields | ❌ | ✅ | ✅ | ✅ | ✅ |
| **Quality** |
| Basic Tests | ✅ | ✅ | ✅ | ✅ | ✅ |
| Edge Case Tests | ❌ | ✅ | ✅ | ✅ | ✅ |
| Integration Tests | ❌ | ❌ | ❌ | ✅ | ✅ |
| Test Coverage > 70% | ❌ | ✅ | ✅ | ✅ | ✅ |
| JavaDoc > 90% | ❌ | ❌ | ✅ | ✅ | ✅ |
| **VocBench** |
| Skeleton Plugin | ✅ | ✅ | ✅ | ✅ | ✅ |
| Repository Integration | ❌ | ✅ | ✅ | ✅ | ✅ |
| UI Integration | ❌ | ❌ | ❌ | ✅ | ✅ |
| Configuration | ❌ | ✅ | ✅ | ✅ | ✅ |
| **Advanced Features** |
| Validation | ❌ | ✅ | ✅ | ✅ | ✅ |
| Name Parsing | Basic | Basic | ✅ | ✅ | ✅ |
| Duplicate Detection | ❌ | ❌ | ❌ | ✅ | ✅ |
| Conflict Resolution | ❌ | ❌ | ❌ | ✅ | ✅ |
| Custom Mappings | ❌ | ❌ | ❌ | ✅ | ✅ |
| Multi-format RDF | ❌ | ❌ | ❌ | ✅ | ✅ |
| Performance Opt | ❌ | ❌ | ✅ | ✅ | ✅ |
| **Documentation** |
| README | ✅ | ✅ | ✅ | ✅ | ✅ |
| CLAUDE.md | ❌ | ✅ | ✅ | ✅ | ✅ |
| Field Mapping | ❌ | ✅ | ✅ | ✅ | ✅ |
| JavaDoc | Minimal | Minimal | ✅ | ✅ | ✅ |
| User Guide | ❌ | ❌ | ❌ | ✅ | ✅ |
| Installation Guide | ❌ | ❌ | ❌ | ✅ | ✅ |
| **Deployment** |
| Maven Build | ✅ | ✅ | ✅ | ✅ | ✅ |
| Uber JAR | ❌ | ❌ | ❌ | ✅ | ✅ |
| Install Scripts | ❌ | ❌ | ❌ | ✅ | ✅ |
| CI/CD | ❌ | ❌ | ❌ | ✅ | ✅ |

---

## Sprint Overview

### Sprint 00: Critical Hot Fixes - **38 Story Points** 🆕

**Focus:** Fix critical architectural issues identified by professor

**Key Deliverables:**
- Remove bibo:sequence, implement RDF Lists (12 pts)
- BibTeX Unicode conversion (8 pts)
- Turtle as default format (5 pts)
- InProceedings/Proceedings support (13 pts)

**Risk Level:** 🔴 HIGH
- Affects core architecture
- Breaking changes to RDF output
- Must maintain 100% round-trip accuracy
- Time pressure to unblock Sprint 01

---

### Sprint 01: Critical MVP - **46 Story Points**

**Focus:** Thesis completion, core functionality

**Key Deliverables:**
- VocBench repository working (13 pts)
- Input validation (5 pts)
- Extended fields: series, edition, keywords (8 pts)
- Edge case tests (8 pts)
- Round-trip tests (5 pts)
- Documentation (3 pts)
- Configuration (5 pts)

**Risk Level:** 🟡 Medium
- VocBench integration complexity
- Time constraints

---

### Sprint 02: Production Quality - **41 Story Points**

**Focus:** Code quality, robustness, completeness

**Key Deliverables:**
- All BibTeX types (8 pts)
- Advanced name parsing (8 pts)
- Date validation (5 pts)
- Identifier validation (5 pts)
- JavaDoc (5 pts)
- Performance (8 pts)
- Statistics (5 pts)

**Risk Level:** 🟢 Low
- Well-defined scope
- No external dependencies

---

### Sprint 03: VocBench Integration - **51 Story Points** (Updated)

**Focus:** VocBench Lifter/Reformatter integration, production deployment

**Key Deliverables:**
- **VocBench RDF Lifter (13 pts)** 🆕
- **VocBench Reformatting Exporter (13 pts)** 🆕
- Extended metadata (8 pts)
- Duplicate detection (8 pts)
- Multi-format serialization (5 pts)
- Packaging (5 pts)
- Integration tests (8 pts)

**Deferred to v1.1+:**
- Conflict resolution UI (moved to post-release)
- Custom mappings UI (moved to post-release)
- VocBench UI components (only if needed)

**Risk Level:** 🔴 HIGH
- VocBench/Semantic Turkey plugin API learning curve
- Complex integration with external system
- May require understanding VocBench internals

---

## Success Metrics

### Code Quality Metrics

| Metric | Current | v0.5.0 | v0.8.0 | v1.0.0 |
|--------|---------|--------|--------|--------|
| Test Coverage | ~40% | >70% | >80% | >85% |
| JavaDoc Coverage | ~10% | ~30% | >90% | >95% |
| Code Smells | Unknown | <20 | <10 | <5 |
| Technical Debt | Unknown | <10% | <5% | <3% |
| Passing Tests | 7/7 | >30 | >60 | >100 |

### Performance Metrics

| Metric | Target | v0.8.0 | v1.0.0 |
|--------|--------|--------|--------|
| Conversion Speed | <10s for 1000 | ✅ | ✅ |
| Memory Usage | <512MB for 10K | ✅ | ✅ |
| Throughput | >100/sec | ✅ | ✅ |

### Feature Completeness

| Category | v0.5.0 | v0.8.0 | v1.0.0 |
|----------|--------|--------|--------|
| BibTeX Types | 57% (8/14) | 100% (14/14) | 100% |
| BibTeX Fields | 70% (21/30) | 90% (27/30) | 100% |
| BIBO Properties | 80% | 90% | 100% |
| Error Handling | 30% | 70% | 90% |
| Documentation | 40% | 70% | 95% |

---

## Risk Assessment

### High Risk Items 🔴

1. **VocBench UI Integration (US-15)**
    - Unknown framework/API
    - Complex integration
    - **Mitigation:** Research early, consider minimal UI first

2. **Performance at Scale (US-13)**
    - Untested with large datasets
    - Memory constraints possible
    - **Mitigation:** Benchmark early, streaming API, optimization sprint

### Medium Risk Items 🟡

1. **Name Parsing Complexity (US-09)**
    - BibTeX name format complex
    - Many edge cases
    - **Mitigation:** Use existing library if available

2. **Integration Testing (US-22)**
    - Requires full stack setup
    - Environment complexity
    - **Mitigation:** Docker containers, mock services

### Low Risk Items 🟢

1. **Field Mapping (US-03)** - Straightforward implementation
2. **Documentation (US-06, US-12)** - Time-consuming but low risk
3. **Validation (US-02)** - Well-defined requirements

---

## Resources & Dependencies

### Team
- **Developer:** 1 (me)
- **Thesis Supervisor:** Available for reviews
- **VocBench Team:** Consultation available
- **Community:** Future contributors

### Technology Stack
- **Language:** Java 17
- **Build:** Maven 3.x
- **Libraries:** RDF4J 5.1.5, JBibTeX 1.0.18, JUnit 5
- **Tools:** Git, IntelliJ IDEA, Docker (for tests)

### External Dependencies
- **VocBench:** Version compatibility TBD
- **RDF4J:** Stable, actively maintained
- **JBibTeX:** Mature, low update frequency

---

## Next Steps (Immediate) - UPDATED

### ⚠️ Sprint 00 Replaces

### Sprint 00 - Hot Fixes
1. 🔴 **FIX-01:** Remove bibo:sequence, implement RDF Lists (CRITICAL)
2. 🔴 **FIX-02:** BibTeX Unicode conversion (CRITICAL)
3. 🟡 **FIX-03:** Turtle as default format
4. 🔴 **FIX-04:** InProceedings/Proceedings support
5. ⏳ Get professor feedback on fixes

### Sprint 01 - Begins
1. Start US-02 (Validation) - foundation for testing
2. Research VocBench plugin architecture
3. Set up code coverage tools (JaCoCo)
4. Test with PapersDB examples

### Sprint 01 - Continues
1. Complete US-01 (VocBench Integration)
2. Complete US-03 (Extended Fields)
3. Write edge case tests (US-04)
4. Round-trip testing (US-05)

### Sprint 01 - Completion
1. Finish Sprint 01 stories
2. Documentation (US-06, US-07)
3. Sprint review and demo
4. Prepare for Sprint 02

---

## Questions & Decisions Needed

### Questions for Professor (Next Meeting)
1. ✅ RDF Lists for author ordering - **CONFIRMED**
2. ✅ Blank nodes for authors - **CONFIRMED (already using)**
3. ❓ InProceedings modeling: always separate proceedings resource or inline?
4. ❓ Should we support old VocBench versions or only latest?
5. ❓ Identifier validation: strict (fail) or lenient (warn)?
6. ❓ Are there BIBO guidelines for conference proceedings modeling?
7. ❓ VocBench: implement as Lifter/Reformatter or generic plugin?
    - **UPDATE:** Professor confirmed Lifter/Reformatter approach

### Architectural Decisions

#### Decided:
1. ✅ **Author ordering:** RDF Lists (not bibo:sequence)
2. ✅ **Author identity:** Blank nodes (not URIs)
3. ✅ **Text encoding:** Unicode (not BibTeX escapes)
4. ✅ **Default serialization:** Turtle (not RDF/XML)
5. ✅ **VocBench integration:** Lifter + Reformatter (not generic plugin)

#### To Be Decided:
1. **Repository abstraction:** Use RDF4J directly or VocBench API?
2. **Error handling:** Exceptions vs. Result types?
3. **Configuration:** Properties files vs. YAML vs. database?
4. **Plugin packaging:** Uber JAR vs. separate dependencies?
5. **Proceedings modeling:** Separate resource vs. inline?

### Project Metadata (To Be Decided)
- [ ] Final name for the plugin (e.g., "BIBO-BibTeX Lifter")
- [ ] Versioning scheme (SemVer - likely 0.x.x → 1.0.0)
- [ ] License (Apache 2.0? MIT? Same as VocBench?)
- [ ] GitHub organization vs. personal repo
- [ ] Support/maintenance plan post-thesis

---

- **Document Owner:** Riccardo Sacco
- **Last Review:** 2025-11-07
- **Next Review:** After Sprint 01 completion

---

*This roadmap is a living document and will be updated as the project evolves.*
