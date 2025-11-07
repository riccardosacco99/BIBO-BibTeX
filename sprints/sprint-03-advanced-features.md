# Sprint 03: Advanced Features & VocBench Integration

**Sprint Duration:** 2-3 weeks
**Sprint Goal:** Add advanced features, complete VocBench integration, and prepare for production deployment
**Team Velocity:** Based on Sprint 01-02 completion
**Priority:** LOW - Enhancement and future-proofing

---

## Sprint Objectives

1. ✅ Full VocBench plugin with UI integration
2. ✅ Advanced metadata support (series, licensing, status)
3. ✅ Duplicate detection and conflict resolution
4. ✅ Custom field mapping configuration
5. ✅ Multi-format support (Turtle, JSON-LD)
6. ✅ Production deployment preparation

---

## User Stories

### US-15: VocBench UI Integration
**As a** VocBench user
**I want** a UI to import/export bibliographic data
**So that** I can use the converter without command line

**Story Points:** 13
**Priority:** P1 - High

**Acceptance Criteria:**
- [ ] VocBench plugin appears in plugin list
- [ ] Import dialog for uploading BibTeX files
- [ ] Export dialog for downloading as BibTeX
- [ ] Progress indicator for batch operations
- [ ] Error messages displayed in UI
- [ ] Preview of converted data before import
- [ ] Plugin integrates with VocBench's project manager

**Technical Tasks:**
- [ ] Research VocBench plugin architecture and UI framework
- [ ] Create plugin descriptor (`plugin.xml` or equivalent)
- [ ] Implement plugin lifecycle hooks:
  - [ ] `onPluginStart()`
  - [ ] `onPluginStop()`
  - [ ] `onProjectOpen()`
- [ ] Create Angular/React component for import UI (check VocBench tech stack)
- [ ] Create REST endpoints:
  - [ ] `POST /api/bibtex/import` - Upload BibTeX file
  - [ ] `GET /api/bibtex/export/{id}` - Download as BibTeX
  - [ ] `POST /api/bibtex/validate` - Validate before import
  - [ ] `GET /api/bibtex/preview` - Preview conversion
- [ ] Implement file upload handling (multipart/form-data)
- [ ] Add progress reporting via WebSocket or polling
- [ ] Create error handling and user-friendly messages
- [ ] Add integration tests with VocBench test harness
- [ ] Create user documentation with screenshots

**Dependencies:** Sprint 01 US-01, US-07
**Estimated Hours:** 32-40 hours

---

### US-16: Extended BIBO Metadata Support
**As a** librarian
**I want** to track series, licensing, and publication status
**So that** I have complete bibliographic records

**Story Points:** 8
**Priority:** P2 - Medium

**Acceptance Criteria:**
- [ ] Series name and number supported
- [ ] Licensing/rights information preserved
- [ ] Publication status (draft, published, retracted)
- [ ] Page count (separate from page range)
- [ ] Chapter number for book sections
- [ ] All new fields map to proper BIBO properties
- [ ] Bidirectional conversion for new fields
- [ ] Tests for each metadata field

**Technical Tasks:**
- [ ] Add to `BiboDocument.Builder`:
  - [ ] `seriesTitle(String)`
  - [ ] `seriesNumber(String)`
  - [ ] `numPages(Integer)`
  - [ ] `pageStart(Integer)`, `pageEnd(Integer)` (parsed from pages)
  - [ ] `chapterNumber(String)`
  - [ ] `rights(String)` - licensing info
  - [ ] `status(DocumentStatus)` - enum for status
- [ ] Add to `BiboVocabulary`:
  - [ ] `SERIES` (bibo:series)
  - [ ] `NUM_PAGES` (bibo:numPages)
  - [ ] `PAGE_START` (bibo:pageStart)
  - [ ] `PAGE_END` (bibo:pageEnd)
  - [ ] `CHAPTER_NUMBER` (bibo:chapter)
- [ ] Create `DocumentStatus` enum (DRAFT, PUBLISHED, RETRACTED, PREPRINT)
- [ ] Implement page range parsing: "123-145" → pageStart=123, pageEnd=145
- [ ] Map series to BibTeX `series` and `number` fields
- [ ] Map rights to custom BibTeX field `rights` or `copyright`
- [ ] Map status to custom field `status` or add to note
- [ ] Update converter to handle new fields
- [ ] Create `ExtendedMetadataTest`
- [ ] Update field mapping documentation

**Dependencies:** Sprint 01 US-03
**Estimated Hours:** 18-24 hours

---

### US-17: Duplicate Detection
**As a** user importing bibliographies
**I want** to detect duplicate entries
**So that** I don't create redundant records

**Story Points:** 8
**Priority:** P2 - Medium

**Acceptance Criteria:**
- [ ] Detects duplicates by DOI
- [ ] Detects duplicates by ISBN
- [ ] Detects duplicates by title + author similarity
- [ ] User can choose action: skip, merge, create new
- [ ] Similarity threshold configurable (e.g., 90% match)
- [ ] Reports show duplicate candidates
- [ ] Tests with known duplicates

**Technical Tasks:**
- [ ] Create `DuplicateDetector` class
- [ ] Implement `findDuplicates(BiboDocument doc, Collection<BiboDocument> existing)` → List<BiboDocument>
- [ ] Implement exact match strategies:
  - [ ] `matchByDOI()` - exact DOI match
  - [ ] `matchByISBN()` - exact ISBN match
  - [ ] `matchByHandle()` - exact Handle match
- [ ] Implement fuzzy match strategies:
  - [ ] `matchByTitleAuthor()` - Levenshtein distance or Jaccard similarity
  - [ ] Configurable similarity threshold (default 0.85)
- [ ] Add library for string similarity (Apache Commons Text)
- [ ] Create `DuplicateResolutionStrategy` enum:
  - [ ] SKIP - don't import duplicates
  - [ ] MERGE - merge metadata from both
  - [ ] CREATE_NEW - import anyway
  - [ ] ASK_USER - prompt for decision
- [ ] Implement merge logic: combine fields, prefer non-empty
- [ ] Add configuration: `duplicate.detection.enabled`, `duplicate.similarity.threshold`
- [ ] Create `DuplicateDetectionTest` with synthetic duplicates
- [ ] Integrate with import workflow in plugin

**Dependencies:** Sprint 01 US-01
**Estimated Hours:** 18-26 hours

---

### US-18: Conflict Resolution UI
**As a** user encountering conflicts during import
**I want** to see differences and choose which values to keep
**So that** I can resolve conflicts intelligently

**Story Points:** 8
**Priority:** P2 - Medium

**Acceptance Criteria:**
- [ ] UI shows side-by-side comparison of conflicting fields
- [ ] User can select which value to keep per field
- [ ] Option to manually enter combined value
- [ ] Preview of final merged record
- [ ] Conflict resolution saved for future duplicates
- [ ] Tests verify merge logic

**Technical Tasks:**
- [ ] Create `ConflictResolver` class
- [ ] Implement `detectConflicts(BiboDocument a, BiboDocument b)` → Map<String, Conflict>
- [ ] Create `Conflict` record with:
  - [ ] fieldName
  - [ ] valueA, valueB
  - [ ] resolution (enum: KEEP_A, KEEP_B, MERGE, MANUAL)
  - [ ] resolvedValue (if manual)
- [ ] Create merge strategies:
  - [ ] `MergeStrategy.PREFER_NEWER` - use most recent
  - [ ] `MergeStrategy.PREFER_LONGER` - use longer string
  - [ ] `MergeStrategy.COMBINE` - concatenate with separator
  - [ ] `MergeStrategy.MANUAL` - user input required
- [ ] Implement UI component for conflict resolution (VocBench framework)
- [ ] Create REST endpoint: `POST /api/bibtex/resolve-conflict`
- [ ] Store resolution rules in configuration or database
- [ ] Add tests for each merge strategy
- [ ] Create `ConflictResolutionTest`

**Dependencies:** US-17 (duplicate detection)
**Estimated Hours:** 18-24 hours

---

### US-19: Custom Field Mapping Configuration
**As a** administrator
**I want** to configure custom field mappings
**So that** I can handle non-standard BibTeX fields

**Story Points:** 8
**Priority:** P2 - Medium

**Acceptance Criteria:**
- [ ] Configuration file defines custom mappings
- [ ] Maps BibTeX field → BIBO property (IRI)
- [ ] Maps BIBO property → BibTeX field
- [ ] Supports custom namespaces
- [ ] Validation of configuration
- [ ] Hot-reload of configuration
- [ ] Tests with custom mappings

**Technical Tasks:**
- [ ] Create `field-mapping.yaml` configuration format:
  ```yaml
  custom-fields:
    - bibtex: "affiliation"
      bibo: "http://example.org/bibo/affiliation"
      direction: bidirectional
    - bibtex: "keywords"
      bibo: "http://purl.org/dc/terms/subject"
      direction: to-bibo-only
  ```
- [ ] Create `FieldMappingConfiguration` class to load YAML
- [ ] Add YAML parser dependency (SnakeYAML)
- [ ] Create `CustomFieldMapper` interface:
  - [ ] `mapToBibo(String field, String value)` → IRI + Value
  - [ ] `mapFromBibo(IRI property, Value value)` → String field + value
- [ ] Implement dynamic field mapping in converter
- [ ] Add validation: check IRI syntax, no duplicate mappings
- [ ] Support hot-reload: watch file for changes
- [ ] Create `CustomFieldMappingTest` with custom fields
- [ ] Document configuration format
- [ ] Add example configurations

**Dependencies:** Sprint 01 US-07
**Estimated Hours:** 18-24 hours

---

### US-20: Multi-Format RDF Support
**As a** user
**I want** to export BIBO as Turtle, JSON-LD, and RDF/XML
**So that** I can use the format that suits my tools

**Story Points:** 5
**Priority:** P3 - Low

**Acceptance Criteria:**
- [ ] Export as Turtle (.ttl)
- [ ] Export as JSON-LD (.jsonld)
- [ ] Export as RDF/XML (.rdf)
- [ ] Export as N-Triples (.nt)
- [ ] Import from any RDF format
- [ ] Format auto-detected by file extension or content-type
- [ ] Tests for each format

**Technical Tasks:**
- [ ] Add RDF4J Rio dependencies for all formats:
  - [ ] rdf4j-rio-turtle
  - [ ] rdf4j-rio-jsonld
  - [ ] rdf4j-rio-ntriples
  - [ ] (rdf4j-rio-rdfxml already present)
- [ ] Create `RDFFormatConverter` utility
- [ ] Implement `exportAs(BiboDocument doc, RDFFormat format)` → String
- [ ] Implement `importFrom(String rdf, RDFFormat format)` → BiboDocument
- [ ] Add format detection: `detectFormat(String filename)` or `detectFormat(InputStream)`
- [ ] Update examples to support format parameter
- [ ] Create test cases for each format:
  - [ ] Turtle round-trip
  - [ ] JSON-LD round-trip
  - [ ] N-Triples round-trip
  - [ ] Mixed format conversion
- [ ] Add REST endpoints: `GET /api/bibtex/export/{id}?format={turtle|jsonld|rdfxml|ntriples}`
- [ ] Document supported formats

**Dependencies:** None
**Estimated Hours:** 12-16 hours

---

### US-21: Production Deployment Packaging
**As a** VocBench administrator
**I want** easy installation of the plugin
**So that** I can deploy without complex setup

**Story Points:** 5
**Priority:** P1 - High

**Acceptance Criteria:**
- [ ] Plugin packaged as single JAR with dependencies
- [ ] Installation script provided
- [ ] Configuration template included
- [ ] Documentation for installation
- [ ] Tested on clean VocBench installation
- [ ] Version information in JAR manifest
- [ ] Release notes included

**Technical Tasks:**
- [ ] Configure Maven Shade plugin to create uber-JAR
- [ ] Include all dependencies except VocBench-provided ones
- [ ] Add manifest entries:
  - [ ] Implementation-Version
  - [ ] Implementation-Vendor
  - [ ] Plugin-Name, Plugin-Description
- [ ] Create `install.sh` script:
  - [ ] Copies JAR to VocBench plugins directory
  - [ ] Creates default configuration
  - [ ] Sets file permissions
- [ ] Create `install.bat` for Windows
- [ ] Write `INSTALLATION.md` guide with:
  - [ ] Prerequisites
  - [ ] Installation steps
  - [ ] Configuration guide
  - [ ] Troubleshooting
- [ ] Create `RELEASE_NOTES.md` for version 1.0.0
- [ ] Test installation on:
  - [ ] Clean VocBench 13.x installation
  - [ ] Linux and Windows
- [ ] Create GitHub release with artifacts

**Dependencies:** US-15 (VocBench integration)
**Estimated Hours:** 12-16 hours

---

### US-22: Comprehensive Integration Tests
**As a** developer
**I want** full end-to-end integration tests
**So that** I'm confident the system works in production

**Story Points:** 8
**Priority:** P2 - Medium

**Acceptance Criteria:**
- [ ] Test full import workflow (file → repository)
- [ ] Test full export workflow (repository → file)
- [ ] Test batch import of 100+ entries
- [ ] Test concurrent imports
- [ ] Test error recovery (repository down, etc.)
- [ ] Test with real VocBench instance
- [ ] All integration tests passing
- [ ] CI/CD pipeline includes integration tests

**Technical Tasks:**
- [ ] Create `integration-test` Maven profile
- [ ] Set up embedded VocBench or Docker container for tests
- [ ] Create `VocBenchIntegrationTest` class
- [ ] Implement tests:
  - [ ] `testImportBibTeXFileToRepository()`
  - [ ] `testExportFromRepositoryToBibTeX()`
  - [ ] `testBatchImport100Entries()`
  - [ ] `testConcurrentImports()` - multi-threaded
  - [ ] `testRepositoryConnectionFailure()` - error handling
  - [ ] `testDuplicateDetectionInRealRepository()`
  - [ ] `testConflictResolutionWorkflow()`
- [ ] Create test fixtures (large BibTeX files, 100+ entries)
- [ ] Add Testcontainers dependency for Docker-based tests
- [ ] Configure CI pipeline (GitHub Actions):
  - [ ] Build and unit tests
  - [ ] Integration tests (if resources available)
  - [ ] Code coverage report
- [ ] Document integration test setup

**Dependencies:** US-15, US-21
**Estimated Hours:** 18-26 hours

---

## Sprint Tasks (Production Readiness)

### T-09: Security Audit
**Priority:** P1
**Estimated Hours:** 8-12 hours

- [ ] Review for SQL/SPARQL injection vulnerabilities
- [ ] Validate all user inputs
- [ ] Check file upload security (size limits, type validation)
- [ ] Review dependency vulnerabilities (OWASP Dependency Check)
- [ ] Add rate limiting for API endpoints
- [ ] Document security considerations

### T-10: Monitoring & Observability
**Priority:** P2
**Estimated Hours:** 8-12 hours

- [ ] Add metrics (conversions/sec, error rate, etc.)
- [ ] Add health check endpoint
- [ ] Add structured logging (JSON logs)
- [ ] Add correlation IDs for request tracing
- [ ] Document monitoring setup

### T-11: Backup & Recovery
**Priority:** P2
**Estimated Hours:** 6-8 hours

- [ ] Document backup procedures
- [ ] Test repository recovery
- [ ] Add export all data functionality
- [ ] Document disaster recovery plan

### T-12: User Documentation
**Priority:** P1
**Estimated Hours:** 12-16 hours

- [ ] User guide with screenshots
- [ ] Tutorial videos (optional)
- [ ] FAQ document
- [ ] Troubleshooting guide
- [ ] API documentation (Swagger/OpenAPI)

---

## Definition of Done

Same as Sprint 01-02, plus:
- [ ] Security audit completed
- [ ] Integration tests passing on CI
- [ ] User documentation complete
- [ ] Installation tested on clean system
- [ ] Performance benchmarks meet targets
- [ ] Production deployment guide ready

---

## Sprint Risks & Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| VocBench UI framework unknown | High | High | Research early, allocate extra time |
| Integration testing complex | Medium | Medium | Use Docker containers for isolation |
| Security vulnerabilities found | High | Low | Regular scanning, follow best practices |
| Performance issues at scale | Medium | Medium | Load testing, profiling, optimization |

---

## Sprint Backlog Priority Order

1. **US-21** - Deployment Packaging (needed for release)
2. **US-15** - VocBench UI (core feature)
3. **US-22** - Integration Tests (quality assurance)
4. **US-17** - Duplicate Detection (important feature)
5. **US-16** - Extended Metadata (completeness)
6. **US-18** - Conflict Resolution (enhancement)
7. **US-19** - Custom Mappings (flexibility)
8. **US-20** - Multi-Format (nice-to-have)

---

## Sprint Review Checklist

- [ ] Demo complete VocBench plugin with UI
- [ ] Show duplicate detection in action
- [ ] Demonstrate conflict resolution workflow
- [ ] Show multi-format export
- [ ] Present integration test results
- [ ] Demo production installation
- [ ] Review security audit findings
- [ ] Plan for v1.0.0 release

---

## Notes

- This sprint prepares for **production release**
- US-15 and US-21 are critical for deployment
- Integration tests (US-22) ensure quality
- Advanced features (US-17-20) can be phased
- Security and monitoring are non-negotiable
- After this sprint, project should be **production-ready**
