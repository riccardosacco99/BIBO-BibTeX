# Sprint 01: Critical Features for MVP/Thesis Completion

- **Sprint Goal:** Complete critical features needed for thesis defense and basic VocBench integration
- **Priority:** HIGH - Required for thesis completion

---

## Sprint Objectives

1. ✅ Implement functional VocBench plugin with real repository integration
2. ✅ Expand test coverage to include edge cases and validation
3. ✅ Add missing critical BibTeX fields
4. ✅ Document conversion mappings and limitations
5. ✅ Ensure all core conversions are robust and well-tested

---

## User Stories

### US-01: VocBench Repository Integration
**As a** VocBench user
**I want** to import BibTeX entries directly into my RDF repository
**So that** I can manage bibliographic data within VocBench

- **Story Points:** 13 
- **Priority:** P0 - Critical

**Acceptance Criteria:**
- [ ] `VocBenchRepositoryGateway` has concrete implementation
- [ ] Stores `BiboDocument` RDF models in real VocBench repository
- [ ] Retrieves documents by identifier from repository
- [ ] Handles connection errors gracefully with proper exceptions
- [ ] Includes transaction support for atomic operations
- [ ] Integration test with actual RDF4J repository (can use in-memory for tests)

**Technical Tasks:**
- [ ] Create `RDF4JRepositoryGateway` implementing `VocBenchRepositoryGateway`
- [ ] Add RDF4J repository connection management
- [ ] Implement `store(Model model)` with SPARQL INSERT
- [ ] Implement `fetchByIdentifier(String id)` with SPARQL CONSTRUCT query
- [ ] Add error handling for repository operations
- [ ] Add transaction wrapper methods
- [ ] Write integration tests with embedded RDF4J repository
- [ ] Update `VocBenchPluginLifecycleTest` to use real implementation

**Dependencies:** None

---

### US-02: Robust Input Validation
**As a** developer using the converter
**I want** clear error messages when conversion fails
**So that** I can understand what went wrong and fix the input

**Story Points:** 5
**Priority:** P0 - Critical

**Acceptance Criteria:**
- [ ] `convertToBibo()` throws meaningful exceptions for invalid input
- [ ] `convertFromBibo()` validates required BIBO fields before conversion
- [ ] Error messages specify which field is invalid and why
- [ ] Null inputs produce clear `IllegalArgumentException`
- [ ] Empty/blank required fields are detected and reported
- [ ] Invalid identifier formats are validated
- [ ] Tests cover all validation scenarios

**Technical Tasks:**
- [ ] Create `BibliographicConversionException` custom exception class
- [ ] Add validation method `validateBibTeXEntry(BibTeXEntry entry)`
- [ ] Add validation method `validateBiboDocument(BiboDocument doc)`
- [ ] Replace `Optional.empty()` returns with exceptions where appropriate
- [ ] Add detailed error messages with field names
- [ ] Create `ValidationException` test class
- [ ] Add tests for each validation scenario (10+ test methods)
- [ ] Update JavaDoc to document exceptions thrown

**Dependencies:** None

---

### US-03: Extended BibTeX Field Support
**As a** bibliographer
**I want** to preserve series, edition, and keyword information
**So that** I don't lose important metadata during conversion

- **Story Points:** 8
- **Priority:** P1 - High

**Acceptance Criteria:**
- [ ] `series` field mapped to/from BIBO (add to BiboDocument model)
- [ ] `edition` field preserved in conversion
- [ ] `keywords` field converted to RDF subject/tags
- [ ] `organization` field for conferences/reports
- [ ] `howpublished` field for @misc entries
- [ ] `crossref` field preserved (at least as note)
- [ ] All new fields have bidirectional conversion
- [ ] Tests for each new field mapping

**Technical Tasks:**
- [ ] Add `series`, `edition`, `keywords`, `organization`, `howpublished` to `BiboDocument.Builder`
- [ ] Add corresponding fields to `BiboDocument` record
- [ ] Map `series` to `bibo:series` IRI in vocabulary
- [ ] Map `edition` to `bibo:edition` IRI in vocabulary
- [ ] Map `keywords` to `dcterms:subject` (multi-value)
- [ ] Update `BibTeXBibliographicConverter.convertToBibo()` to extract new fields
- [ ] Update `BibTeXBibliographicConverter.convertFromBibo()` to generate new fields
- [ ] Add `SERIES`, `EDITION` IRIs to `BiboVocabulary`
- [ ] Write tests for each new field (8+ test methods)
- [ ] Update RDF model generation in `BiboDocument.Builder.build()`

**Dependencies:** None

---

### US-04: Comprehensive Edge Case Testing
**As a** developer
**I want** edge cases and error conditions tested
**So that** the converter is robust in production

- **Story Points:** 8
- **Priority:** P1 - High

**Acceptance Criteria:**
- [ ] Name parsing edge cases tested (single name, particles, special chars)
- [ ] Date parsing edge cases tested (invalid months, Feb 29, malformed)
- [ ] Identifier edge cases tested (invalid ISBN, mixed valid/invalid)
- [ ] Missing required fields tested
- [ ] Empty/null values tested
- [ ] Special characters and Unicode tested
- [ ] Large entries tested (100+ authors, very long titles)
- [ ] Test coverage > 70%

**Technical Tasks:**
- [ ] Create `BibTeXBibliographicConverterEdgeCaseTest` class
- [ ] Add 10+ name parsing edge case tests
  - [ ] Single name (no family name)
  - [ ] Name with "von", "van", "de" particles
  - [ ] Unicode characters (Chinese, Arabic names)
  - [ ] Names with special chars (O'Brien, etc.)
  - [ ] Very long names
- [ ] Add 10+ date parsing edge case tests
  - [ ] Invalid month numbers (13, 0, -1)
  - [ ] Feb 29 in non-leap years
  - [ ] Malformed date strings
  - [ ] Future dates
  - [ ] Ancient dates (year < 1000)
- [ ] Add 10+ identifier validation tests
  - [ ] Invalid ISBN checksums
  - [ ] Wrong length ISBNs
  - [ ] Multiple ISBNs in field
  - [ ] Invalid DOI formats
  - [ ] Malformed URLs
- [ ] Add 10+ general edge case tests
  - [ ] Entry with no title
  - [ ] Very long titles (1000+ chars)
  - [ ] 100+ authors
  - [ ] All optional fields missing
  - [ ] All optional fields present
- [ ] Run code coverage tool (JaCoCo)
- [ ] Aim for 70%+ line coverage

**Dependencies:** US-02 (validation)

---

### US-05: Round-Trip Conversion Testing
**As a** user
**I want** to ensure data isn't lost in round-trip conversions
**So that** I can confidently use the converter bidirectionally

- **Story Points:** 5
- **Priority:** P1 - High

**Acceptance Criteria:**
- [ ] BibTeX → BIBO → BibTeX preserves all mapped fields
- [ ] BIBO → BibTeX → BIBO preserves all mapped fields
- [ ] At least 10 diverse test cases (different entry types)
- [ ] Documented limitations for non-lossless conversions
- [ ] Tests verify field-by-field equality
- [ ] Citation key preservation tested

**Technical Tasks:**
- [ ] Create `RoundTripConversionTest` class
- [ ] Implement `assertBibTeXFieldEquals()` helper
- [ ] Implement `assertBiboFieldEquals()` helper
- [ ] Add 5+ BibTeX → BIBO → BibTeX tests
  - [ ] @article with all fields
  - [ ] @book with all fields
  - [ ] @inproceedings with all fields
  - [ ] @phdthesis with all fields
  - [ ] @misc with minimal fields
- [ ] Add 5+ BIBO → BibTeX → BIBO tests
  - [ ] ARTICLE with full metadata
  - [ ] BOOK with series/edition
  - [ ] CONFERENCE_PAPER with proceedings
  - [ ] THESIS with advisor
  - [ ] WEBPAGE with URL only
- [ ] Document known lossy conversions in `LIMITATIONS.md`
- [ ] Add round-trip test data to `test-data/roundtrip/`

**Dependencies:** US-03 (extended fields)

---

### US-06: Field Mapping Documentation
**As a** user of the converter
**I want** to see which BibTeX fields map to which BIBO properties
**So that** I understand what data is preserved

- **Story Points:** 3
- **Priority:** P2 - Medium

**Acceptance Criteria:**
- [ ] Complete field mapping table in documentation
- [ ] Document which fields are lossy or not preserved
- [ ] Examples for each document type conversion
- [ ] Mapping table shows BibTeX field → BIBO property → BibTeX field
- [ ] Special cases documented (e.g., publisher vs school vs institution)
- [ ] Type-specific mappings documented

**Technical Tasks:**
- [ ] Create `FIELD_MAPPING.md` file
- [ ] Create table: BibTeX Field | BIBO Property | Direction | Notes
- [ ] Document all 30+ field mappings
- [ ] Add examples for each of 8 document types
- [ ] Document type-specific field logic (publisher/school/institution)
- [ ] Document container title logic (journal vs booktitle)
- [ ] Add limitation section for unsupported fields
- [ ] Add section on custom/non-standard fields
- [ ] Add conversion examples to README

**Dependencies:** US-03 (so all fields are included)

---

### US-07: VocBench Plugin Configuration
**As a** VocBench administrator
**I want** to configure the plugin via properties file
**So that** I can customize behavior without code changes

- **Story Points:** 5
- **Priority:** P2 - Medium

**Acceptance Criteria:**
- [ ] Plugin reads configuration from `plugin.properties`
- [ ] Configurable: repository URL, default namespaces, field mappings
- [ ] Invalid configuration produces clear error
- [ ] Default values provided for all settings
- [ ] Configuration can be overridden via environment variables
- [ ] Tests verify configuration loading

**Technical Tasks:**
- [ ] Create `plugin.properties` file in resources
- [ ] Create `VocBenchPluginConfiguration` class
- [ ] Add properties:
  - [ ] `repository.url` (default: in-memory)
  - [ ] `repository.type` (default: rdf4j-memory)
  - [ ] `bibo.namespace.prefix` (default: http://example.org/bibo/)
  - [ ] `field.mapping.strict` (default: false - allow custom fields)
  - [ ] `identifier.generation.strategy` (default: author-title)
- [ ] Implement configuration loading with fallback defaults
- [ ] Add validation for required properties
- [ ] Support environment variable overrides (e.g., `VOCBENCH_REPO_URL`)
- [ ] Add `VocBenchPluginConfigurationTest`
- [ ] Document all properties in `plugin.properties` with comments

**Dependencies:** US-01 (repository integration)

---

## Sprint Tasks (Technical Debt & Improvements)

### T-01: Add Logging Framework
**Priority:** P2

- [ ] Add SLF4J dependency to pom.xml
- [ ] Add Logback configuration
- [ ] Add logging to converter at INFO level
- [ ] Add logging to plugin lifecycle at DEBUG level
- [ ] Log conversion stats (fields mapped, warnings, etc.)
- [ ] Add error logging with stack traces

### T-02: Improve Name Parsing
**Priority:** P2

- [ ] Research BibTeX name parsing rules
- [ ] Implement proper "von", "van", "de" particle handling
- [ ] Handle middle names properly
- [ ] Support BibTeX "First von Last, Jr" format
- [ ] Add comprehensive name parsing tests
- [ ] Document name parsing algorithm

### T-03: Extract Reverse Conversion to Library
**Priority:** P2

- [ ] Move RDF parsing logic from `ReverseConversion.java` to converter
- [ ] Add `convertToBiboFromRDF(Model model)` method
- [ ] Support Turtle, RDF/XML, JSON-LD formats
- [ ] Update example to use library method
- [ ] Add tests for RDF format conversion
- [ ] Handle blank nodes properly

### T-04: Citation Key Generation Improvements
**Priority:** P3

- [ ] Add duplicate key detection
- [ ] Add counter suffix for duplicates (smith2024, smith2024_2)
- [ ] Validate generated keys (no invalid chars)
- [ ] Ensure minimum key length (>= 3 chars)
- [ ] Add configuration for key generation strategy
- [ ] Test key generation with edge cases

---

## Definition of Done

- [ ] All acceptance criteria met for user stories
- [ ] Unit tests written and passing (coverage > 70%)
- [ ] Integration tests passing
- [ ] Code reviewed (self-review or peer review)
- [ ] Documentation updated (JavaDoc, README)
- [ ] No critical/high bugs
- [ ] Build passing (`mvn clean package`)
- [ ] Changes committed with clear messages

---

## Sprint Risks & Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| VocBench API integration complex | High | Medium | Start with RDF4J, abstract VocBench-specific code |
| Test coverage goal too ambitious | Medium | Low | Focus on critical paths first, defer edge cases |
| Field mapping debates/decisions | Low | Medium | Document decisions, accept some limitations |

---

## Sprint Backlog Priority Order

1. **US-02** - Input Validation (prerequisite for testing)
2. **US-01** - VocBench Integration (core goal)
3. **US-03** - Extended Fields (for completeness)
4. **US-04** - Edge Case Testing (quality)
5. **US-05** - Round-Trip Testing (validation)
6. **US-06** - Documentation (communication)
7. **US-07** - Configuration (nice-to-have)

---

## Sprint Review Checklist

- [ ] Demo VocBench plugin importing BibTeX entries
- [ ] Show test coverage report
- [ ] Review field mapping documentation
- [ ] Demonstrate error handling with invalid inputs
- [ ] Show round-trip conversion examples
- [ ] Discuss remaining limitations
- [ ] Identify stories for next sprint

---

## Notes

- This sprint focuses on **thesis-critical** features
- US-01 is highest priority for VocBench integration
- Testing (US-04, US-05) is crucial for robustness
- Documentation (US-06) needed for thesis defense
