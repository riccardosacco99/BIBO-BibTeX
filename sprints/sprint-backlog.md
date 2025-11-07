# Product Backlog - BIBO-BibTeX Converter

**Project:** BIBO-BibTeX Bibliographic Converter
**Product Owner:** Riccardo Sacco
**Version:** 0.1.0-SNAPSHOT
**Last Updated:** 2025-11-07

---

## Product Vision

Create a robust, production-ready bibliographic converter that uses BIBO (Bibliographic Ontology) as a pivot model to enable conversions between BibTeX, Microsoft Word Bibliography, and other formats, initially integrated as a VocBench plugin for semantic web applications.

---

## Release Plan

### 🔥 Release 0.2.0 - Critical Hot Fixes (Sprint 00) - 1 week **NEW**
**Target Date:** End of Week 1
**Goal:** Fix critical architectural issues identified by professor

**Included:**
- FIX-01: Remove bibo:sequence, implement RDF Lists
- FIX-02: BibTeX Unicode conversion
- FIX-03: Turtle as default format
- FIX-04: InProceedings/Proceedings support

**Success Criteria:**
- Zero invalid RDF properties
- Unicode characters display correctly
- Turtle output is readable
- Conference papers supported
- Round-trip tests still 100% accurate
- Professor approves changes

**Blockers Resolved:**
- Invalid RDF (bibo:sequence doesn't exist)
- Poor data quality (escape characters)
- Limited academic type support

---

### Release 0.5.0 - MVP/Thesis (Sprint 01) - 3 weeks
**Target Date:** End of Week 4 (adjusted +1 for Sprint 00)
**Goal:** Thesis-ready implementation with core features

**Included:**
- VocBench repository integration (US-01)
- Input validation with clear errors (US-02)
- Extended BibTeX field support (US-03)
- Comprehensive edge case testing (US-04)
- Round-trip conversion testing (US-05)
- Field mapping documentation (US-06)
- Basic configuration support (US-07)

**Success Criteria:**
- All tests passing
- VocBench plugin functional
- Documentation complete for thesis
- Demo-ready for thesis defense

---

### Release 0.8.0 - Production Quality (Sprint 02) - 2 weeks
**Target Date:** 5 weeks from start
**Goal:** Production-quality code ready for broader use

**Included:**
- Complete BibTeX type support (US-08)
- Advanced name parsing (US-09)
- Date validation (US-10)
- Identifier validation (US-11)
- Comprehensive JavaDoc (US-12)
- Performance optimization (US-13)
- Conversion statistics (US-14)

**Success Criteria:**
- All 14 BibTeX types supported
- Code quality metrics met (>70% coverage, JavaDoc >90%)
- Performance targets met (1000 entries < 10s)
- Ready for beta testing

---

### Release 1.0.0 - Production Ready (Sprint 03) - 2-3 weeks
**Target Date:** 7-8 weeks from start
**Goal:** Full production deployment with VocBench UI

**Included:**
- VocBench UI integration (US-15)
- Extended BIBO metadata (US-16)
- Duplicate detection (US-17)
- Conflict resolution (US-18)
- Custom field mapping (US-19)
- Multi-format support (US-20)
- Production packaging (US-21)
- Integration tests (US-22)

**Success Criteria:**
- Full VocBench integration working
- Security audit passed
- Installation tested on clean systems
- User documentation complete
- Ready for production deployment

---

## Backlog Items by Epic

### Epic 1: Core Converter Functionality
**Status:** 80% Complete

| ID | User Story | Priority | Story Points | Status | Sprint |
|----|-----------|----------|--------------|--------|--------|
| US-01 | VocBench Repository Integration | P0 | 13 | Planned | Sprint 01 |
| US-02 | Robust Input Validation | P0 | 5 | Planned | Sprint 01 |
| US-03 | Extended BibTeX Field Support | P1 | 8 | Planned | Sprint 01 |
| US-08 | Complete BibTeX Type Support | P1 | 8 | Planned | Sprint 02 |
| US-09 | Advanced Name Parsing | P1 | 8 | Planned | Sprint 02 |
| US-10 | Enhanced Date Validation | P2 | 5 | Planned | Sprint 02 |
| US-11 | Identifier Validation Library | P2 | 5 | Planned | Sprint 02 |
| US-16 | Extended BIBO Metadata Support | P2 | 8 | Planned | Sprint 03 |

**Total Story Points:** 60
**Estimated Duration:** 7-9 weeks

---

### Epic 2: Quality & Testing
**Status:** 40% Complete

| ID | User Story | Priority | Story Points | Status | Sprint |
|----|-----------|----------|--------------|--------|--------|
| US-04 | Comprehensive Edge Case Testing | P1 | 8 | Planned | Sprint 01 |
| US-05 | Round-Trip Conversion Testing | P1 | 5 | Planned | Sprint 01 |
| US-12 | Comprehensive JavaDoc | P2 | 5 | Planned | Sprint 02 |
| US-13 | Batch Conversion Performance | P2 | 8 | Planned | Sprint 02 |
| US-22 | Comprehensive Integration Tests | P2 | 8 | Planned | Sprint 03 |

**Total Story Points:** 34
**Estimated Duration:** 4-5 weeks

---

### Epic 3: Documentation & Usability
**Status:** 30% Complete

| ID | User Story | Priority | Story Points | Status | Sprint |
|----|-----------|----------|--------------|--------|--------|
| US-06 | Field Mapping Documentation | P2 | 3 | Planned | Sprint 01 |
| US-07 | VocBench Plugin Configuration | P2 | 5 | Planned | Sprint 01 |
| US-14 | Conversion Statistics and Reports | P3 | 5 | Planned | Sprint 02 |

**Total Story Points:** 13
**Estimated Duration:** 2 weeks

---

### Epic 4: VocBench Integration
**Status:** 20% Complete (skeleton only)

| ID | User Story | Priority | Story Points | Status | Sprint |
|----|-----------|----------|--------------|--------|--------|
| US-15 | VocBench UI Integration | P1 | 13 | Planned | Sprint 03 |
| US-17 | Duplicate Detection | P2 | 8 | Planned | Sprint 03 |
| US-18 | Conflict Resolution UI | P2 | 8 | Planned | Sprint 03 |
| US-19 | Custom Field Mapping Configuration | P2 | 8 | Planned | Sprint 03 |
| US-21 | Production Deployment Packaging | P1 | 5 | Planned | Sprint 03 |

**Total Story Points:** 42
**Estimated Duration:** 5-6 weeks

---

### Epic 5: Advanced Features
**Status:** 0% Complete (future)

| ID | User Story | Priority | Story Points | Status | Sprint |
|----|-----------|----------|--------------|--------|--------|
| US-20 | Multi-Format RDF Support | P3 | 5 | Planned | Sprint 03 |
| US-23 | Support for Zotero RDF | P3 | 13 | Backlog | Future |
| US-24 | Support for CSL-JSON | P3 | 8 | Backlog | Future |
| US-25 | Microsoft Word Bibliography Integration | P2 | 13 | Backlog | Future |
| US-26 | Mendeley API Integration | P3 | 13 | Backlog | Future |

**Total Story Points:** 52
**Estimated Duration:** 6-8 weeks

---

## Future Backlog Items (Post-1.0)

### US-23: Zotero RDF Support
**As a** Zotero user
**I want** to import/export Zotero RDF
**So that** I can migrate data between Zotero and BIBO

**Story Points:** 13
**Priority:** P3

**Tasks:**
- Research Zotero RDF schema
- Map Zotero types to BIBO types
- Implement bidirectional converter
- Handle Zotero-specific fields (tags, collections, attachments)
- Test with real Zotero exports

---

### US-24: CSL-JSON Support
**As a** user of Citation Style Language tools
**I want** to convert CSL-JSON to BIBO
**So that** I can use CSL bibliographies

**Story Points:** 8
**Priority:** P3

**Tasks:**
- Parse CSL-JSON format
- Map CSL types to BIBO
- Handle CSL date formats
- Implement converter
- Test with Zotero/Mendeley CSL exports

---

### US-25: Microsoft Word Bibliography Integration
**As a** Microsoft Word user
**I want** to import Word bibliography XML
**So that** I can convert Word bibliographies to BIBO/BibTeX

**Story Points:** 13
**Priority:** P2

**Tasks:**
- Parse Word bibliography XML format
- Map Word source types to BIBO
- Handle Word-specific fields
- Implement bidirectional conversion
- Test with real Word documents

---

### US-26: Mendeley API Integration
**As a** Mendeley user
**I want** to sync my Mendeley library to BIBO
**So that** I can use Mendeley as source

**Story Points:** 13
**Priority:** P3

**Tasks:**
- Integrate Mendeley API client
- Authenticate with OAuth
- Fetch library entries
- Convert Mendeley format to BIBO
- Handle pagination and rate limits

---

### US-27: Web Interface
**As a** casual user
**I want** a web interface for conversion
**So that** I don't need to install software

**Story Points:** 21
**Priority:** P3

**Tasks:**
- Design web UI (React/Vue)
- Create REST API backend
- Implement file upload/download
- Add conversion preview
- Deploy to cloud (AWS/Heroku)
- Add usage analytics

---

### US-28: Command-Line Tool
**As a** power user
**I want** a CLI tool for batch conversions
**So that** I can script conversions

**Story Points:** 8
**Priority:** P3

**Tasks:**
- Create CLI with picocli or similar
- Support file and stdin input
- Support multiple output formats
- Add progress bars for large files
- Distribute via Homebrew/apt/chocolatey

---

### US-29: GraphQL API
**As a** API consumer
**I want** a GraphQL API for flexible queries
**So that** I can fetch exactly the data I need

**Story Points:** 13
**Priority:** P3

**Tasks:**
- Design GraphQL schema
- Implement resolvers
- Add mutations for conversion
- Add subscriptions for batch progress
- Document API with GraphiQL

---

### US-30: Bibliography Merging
**As a** researcher
**I want** to merge multiple bibliographies
**So that** I can combine sources without duplicates

**Story Points:** 13
**Priority:** P2

**Tasks:**
- Implement fuzzy matching algorithm
- Create merge strategy options
- Handle conflicts intelligently
- Preserve provenance information
- Test with large bibliographies

---

## Technical Debt Items

### TD-01: Refactor Name Parsing
**Priority:** P2
**Effort:** 8 hours
**Sprint:** Sprint 02

- Current implementation is simplistic
- Should use proper BibTeX parser or library
- Need to handle edge cases better

---

### TD-02: Improve Error Messages
**Priority:** P1
**Effort:** 6 hours
**Sprint:** Sprint 01

- Many methods return Optional.empty() without context
- Need structured exceptions with details
- Add error codes for internationalization

---

### TD-03: Add Code Coverage
**Priority:** P2
**Effort:** 4 hours
**Sprint:** Sprint 01

- Configure JaCoCo Maven plugin
- Set minimum coverage thresholds
- Add coverage badge to README

---

### TD-04: Dependency Vulnerability Scanning
**Priority:** P1
**Effort:** 3 hours
**Sprint:** Sprint 03

- Add OWASP Dependency Check
- Update vulnerable dependencies
- Add to CI pipeline

---

### TD-05: Internationalization (i18n)
**Priority:** P3
**Effort:** 12 hours
**Sprint:** Future

- Externalize all user-facing strings
- Add resource bundles for multiple languages
- Support locale-specific date formatting

---

## Bug Tracking

### Known Issues

**BUG-01:** Month parsing case-sensitive
**Severity:** Low
**Priority:** P2
**Sprint:** Sprint 02
**Description:** Month field only accepts lowercase month names

**BUG-02:** Citation key sanitization too aggressive
**Severity:** Low
**Priority:** P2
**Sprint:** Sprint 01
**Description:** All special chars replaced with underscore, can create invalid keys

**BUG-03:** No handling of LaTeX special characters
**Severity:** Medium
**Priority:** P2
**Sprint:** Sprint 02
**Description:** BibTeX fields with LaTeX commands (e.g., `{\"u}`) not handled

---

## Metrics & KPIs

### Code Quality Targets
- **Unit Test Coverage:** > 70% (Sprint 01), > 80% (Sprint 02)
- **JavaDoc Coverage:** > 90% (Sprint 02)
- **Code Smells:** < 10 (Sonar)
- **Technical Debt Ratio:** < 5% (Sonar)
- **Duplicated Lines:** < 3%

### Performance Targets
- **Conversion Speed:** 1000 entries < 10 seconds
- **Memory Usage:** < 512 MB for 10,000 entries
- **Throughput:** > 100 conversions/second

### User Satisfaction Targets (Post-Release)
- **Error Rate:** < 1% of conversions fail
- **User Satisfaction:** > 4.0/5.0
- **Documentation Quality:** > 4.0/5.0

---

## Dependencies & Risks

### External Dependencies
- **RDF4J** - Core dependency, stable
- **JBibTeX** - Mature library, low risk
- **VocBench** - Integration point, needs research (HIGH RISK)

### Risk Register

| Risk | Impact | Probability | Mitigation | Owner |
|------|--------|-------------|------------|-------|
| VocBench API changes | High | Low | Use stable API version, abstract integration | Dev |
| Name parsing complexity | Medium | High | Use existing library, accept limitations | Dev |
| Performance at scale | Medium | Medium | Benchmark early, optimize if needed | Dev |
| Security vulnerabilities | High | Low | Regular scanning, follow best practices | Dev |
| Incomplete field coverage | Low | High | Document limitations clearly | Dev |

---

## Stakeholders

- **Thesis Supervisor:** Review and approve thesis deliverables
- **VocBench Team:** Integration requirements and support
- **Future Users:** Librarians, researchers, bibliography managers
- **Open Source Community:** Potential contributors post-release

---

## Definition of Ready (DoR)

A user story is ready for sprint when:
- [ ] Acceptance criteria defined
- [ ] Story points estimated
- [ ] Dependencies identified
- [ ] Technical approach discussed
- [ ] Testable
- [ ] Fits in one sprint

---

## Definition of Done (DoD)

A user story is done when:
- [ ] Code complete and reviewed
- [ ] Unit tests written (>70% coverage)
- [ ] Integration tests passing
- [ ] Documentation updated
- [ ] No critical/high bugs
- [ ] Build passing
- [ ] Acceptance criteria met
- [ ] Demo-ready

---

## Backlog Grooming Schedule

- **Frequency:** Weekly
- **Duration:** 1 hour
- **Participants:** Product Owner, Dev Team
- **Agenda:**
  - Review new items
  - Re-prioritize backlog
  - Refine upcoming stories
  - Estimate new items
  - Remove obsolete items

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0 | 2025-11-07 | Initial backlog created | AI Assistant |
| 1.1 | TBD | After Sprint 01 review | Team |
| 1.2 | TBD | After Sprint 02 review | Team |
| 2.0 | TBD | After v1.0 release | Team |
