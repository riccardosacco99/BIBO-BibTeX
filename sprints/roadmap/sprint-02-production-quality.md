# Sprint 02: Production Quality Improvements

- **Sprint Goal:** Improve code quality, robustness, and complete remaining BibTeX type support
- **Priority:** MEDIUM - Important for production deployment

---

## Sprint Objectives

1. ✅ Support all standard BibTeX entry types
2. ✅ Implement robust name and date parsing
3. ✅ Add comprehensive JavaDoc documentation
4. ✅ Improve identifier validation
5. ✅ Optimize performance for batch operations
6. ✅ Add detailed conversion reports/statistics

---

## User Stories

### US-08: Complete BibTeX Type Support
**As a** user with diverse bibliography
**I want** support for all standard BibTeX entry types
**So that** I can convert any bibliography without type errors

- **Story Points:** 8
- **Priority:** P1 - High

**Acceptance Criteria:**
- [ ] Support for @booklet entries
- [ ] Support for @manual entries
- [ ] Support for @unpublished entries
- [ ] Distinguish @mastersthesis from @phdthesis
- [ ] Better handling of @misc (map to appropriate BIBO type when possible)
- [ ] Support @conference (alias for @inproceedings)
- [ ] Type-specific required field validation
- [ ] Tests for each entry type

**Technical Tasks:**
- [ ] Add `BOOKLET`, `MANUAL`, `UNPUBLISHED` to `BiboDocumentType` enum
- [ ] Add corresponding BIBO IRIs to `BiboVocabulary`
- [ ] Update `mapDocumentType()` to handle new types
- [ ] Update `mapEntryType()` for reverse conversion
- [ ] Add `degreeType` field to `BiboDocument` to preserve masters vs phd
- [ ] Implement smart @misc mapping based on fields present:
  - [ ] If has URL → WEBPAGE
  - [ ] If has howpublished → REPORT or OTHER
  - [ ] Default → OTHER
- [ ] Add validation rules per entry type (required vs optional fields)
- [ ] Create `BibTeXTypeComprehensiveTest` with tests for all 14 types
- [ ] Update field mapping documentation

**Dependencies:** Sprint 01 US-03 (extended fields)

---

### US-09: Advanced Name Parsing
**As a** bibliographer working with international names
**I want** proper handling of name particles and complex names
**So that** author names are preserved correctly

- **Story Points:** 8
- **Priority:** P1 - High

**Acceptance Criteria:**
- [ ] Handles BibTeX "First von Last, Jr" format correctly
- [ ] Preserves name particles (von, van, de, etc.)
- [ ] Supports middle names without loss
- [ ] Handles single-name authors (e.g., "Plato")
- [ ] Preserves suffixes (Jr, Sr, III, etc.)
- [ ] Unicode/international names handled correctly
- [ ] Proper round-trip for complex names
- [ ] 20+ name parsing test cases

**Technical Tasks:**
- [ ] Research BibTeX name parsing specification
- [ ] Add `middleName`, `nameParticle`, `nameSuffix` to `BiboPersonName`
- [ ] Implement BibTeX name tokenizer:
  - [ ] Split by comma first (format detection)
  - [ ] Identify particles (lowercase words before last name)
  - [ ] Identify suffixes after comma
  - [ ] Extract given/middle/family correctly
- [ ] Map particles to `foaf:familyName` with particle included
- [ ] Map suffixes to custom BIBO property or append to family name
- [ ] Update `parseName()` method with new algorithm
- [ ] Update `formatName()` to reconstruct BibTeX format
- [ ] Add 20+ test cases in `PersonNameParsingTest`:
  - [ ] "Ludwig van Beethoven"
  - [ ] "Charles de Gaulle"
  - [ ] "Vincent van Gogh"
  - [ ] "Martin Luther King, Jr."
  - [ ] "John Smith, III"
  - [ ] Single names
  - [ ] Hyphenated names
  - [ ] Multiple middle names
  - [ ] Unicode names (Chinese, Arabic)

**Dependencies:** None

---

### US-10: Enhanced Date Validation
**As a** user converting historical bibliographies
**I want** proper validation of dates including edge cases
**So that** I can trust the converted dates are correct

- **Story Points:** 5
- **Priority:** P2 - Medium

**Acceptance Criteria:**
- [ ] Validates month is 1-12
- [ ] Validates day based on month (28/29/30/31)
- [ ] Handles leap year Feb 29 correctly
- [ ] Accepts historical dates (year < 1000)
- [ ] Warns on future dates (year > current year + 5)
- [ ] Supports circa dates (extract year with note)
- [ ] Multiple date format support
- [ ] Clear error messages for invalid dates

**Technical Tasks:**
- [ ] Create `DateValidator` utility class
- [ ] Implement `isValidDate(int year, int month, int day)` with leap year logic
- [ ] Implement `isLeapYear(int year)` helper
- [ ] Add days-per-month validation array
- [ ] Add warning for future dates (log but allow)
- [ ] Improve `extractYearFromFreeForm()`:
  - [ ] Try multiple patterns (YYYY, YYYY-MM-DD, MM/DD/YYYY, etc.)
  - [ ] Handle "circa YYYY", "c. YYYY", "~YYYY"
  - [ ] Store "circa" info in notes or custom field
- [ ] Add `parseDate(String dateString)` with multiple format support
- [ ] Create `DateValidationTest` with 15+ test cases
- [ ] Update `BiboPublicationDate` validation in factory methods

**Dependencies:** None

---

### US-11: Identifier Validation Library
**As a** user
**I want** identifiers validated properly
**So that** invalid ISBNs, DOIs are caught early

- **Story Points:** 5
- **Priority:** P2 - Medium

**Acceptance Criteria:**
- [ ] ISBN-10 checksum validation
- [ ] ISBN-13 checksum validation
- [ ] DOI format validation
- [ ] ISSN checksum validation
- [ ] Handle validation (format check)
- [ ] URL validation
- [ ] Invalid identifiers rejected with clear message
- [ ] Tests for valid and invalid identifiers

**Technical Tasks:**
- [ ] Create `IdentifierValidator` class
- [ ] Implement `validateISBN10(String isbn)` with checksum
- [ ] Implement `validateISBN13(String isbn)` with checksum
- [ ] Implement `validateISSN(String issn)` with checksum
- [ ] Implement `validateDOI(String doi)` with regex
- [ ] Implement `validateHandle(String handle)` with format check
- [ ] Implement `validateURL(String url)` using Java URL class
- [ ] Update `classifyIsbn()` to use validation
- [ ] Update `extractIdentifiers()` to validate before adding
- [ ] Add `skipInvalidIdentifiers` configuration flag
- [ ] Create `IdentifierValidationTest` with 30+ cases
- [ ] Document identifier format requirements

**Dependencies:** Sprint 01 US-02 (validation exceptions)

---

### US-12: Comprehensive JavaDoc
**As a** developer using the library
**I want** complete JavaDoc for all public APIs
**So that** I understand how to use the converter

- **Story Points:** 5
- **Priority:** P2 - Medium

**Acceptance Criteria:**
- [ ] All public classes have class-level JavaDoc
- [ ] All public methods have JavaDoc with @param, @return, @throws
- [ ] Complex algorithms explained
- [ ] Examples provided for main entry points
- [ ] Package-level documentation (package-info.java)
- [ ] JavaDoc generates without warnings
- [ ] HTML JavaDoc generated and reviewed

**Technical Tasks:**
- [ ] Add class-level JavaDoc to all 20+ classes
- [ ] Add method-level JavaDoc to all public methods (100+ methods)
- [ ] Document `BibliographicConverter` interface with usage examples
- [ ] Document `BiboDocument.Builder` with fluent API example
- [ ] Document `BibTeXBibliographicConverter` with conversion examples
- [ ] Create `package-info.java` for each package:
  - [ ] `it.riccardosacco.bibobibtex.converter`
  - [ ] `it.riccardosacco.bibobibtex.model.bibo`
  - [ ] `it.riccardosacco.bibobibtex.vocbench`
- [ ] Add @since tags (version 0.1.0)
- [ ] Add @author tags
- [ ] Configure Maven JavaDoc plugin
- [ ] Generate JavaDoc HTML: `mvn javadoc:javadoc`
- [ ] Review generated HTML for formatting issues

**Dependencies:** None

---

### US-13: Batch Conversion Performance
**As a** user with large bibliographies
**I want** efficient batch conversion of 1000+ entries
**So that** conversion doesn't take too long

- **Story Points:** 8
- **Priority:** P2 - Medium

**Acceptance Criteria:**
- [ ] Converts 1000 entries in < 10 seconds
- [ ] Memory-efficient (no OOM with 10,000 entries)
- [ ] Batch conversion API with progress reporting
- [ ] Parallel conversion option
- [ ] Streaming API for very large files
- [ ] Performance benchmarks documented
- [ ] Tests with large datasets

**Technical Tasks:**
- [ ] Create `BatchConverter` utility class
- [ ] Implement `convertBatch(Collection<BibTeXEntry> entries)` → List<BiboDocument>
- [ ] Add progress callback: `convertBatch(Collection, ProgressListener)`
- [ ] Implement parallel conversion with Streams:
  - [ ] `entries.parallelStream().map(converter::convertToBibo)`
  - [ ] Configurable parallelism level
- [ ] Add streaming API:
  - [ ] `convertStream(Stream<BibTeXEntry>)` → Stream<BiboDocument>
  - [ ] Allows processing without loading all into memory
- [ ] Write performance benchmarks (JMH):
  - [ ] Benchmark 100, 1000, 10000 entry conversions
  - [ ] Compare sequential vs parallel
  - [ ] Measure memory usage
- [ ] Create large test dataset (generate 10000 synthetic entries)
- [ ] Add performance test: `BatchConversionPerformanceTest`
- [ ] Document performance characteristics

**Dependencies:** None

---

### US-14: Conversion Statistics and Reports
**As a** user
**I want** a report after conversion showing what was converted
**So that** I can verify the conversion quality

- **Story Points:** 5
- **Priority:** P3 - Low

**Acceptance Criteria:**
- [ ] Conversion returns statistics object
- [ ] Statistics include: total entries, successful, failed, warnings
- [ ] Field-level stats (how many authors, identifiers, etc.)
- [ ] Missing field warnings
- [ ] Data quality warnings (e.g., future dates, invalid identifiers)
- [ ] Report can be exported as JSON or text
- [ ] Tests verify statistics accuracy

**Technical Tasks:**
- [ ] Create `ConversionStatistics` class with fields:
  - [ ] totalEntries, successfulConversions, failedConversions
  - [ ] warningMessages (List<String>)
  - [ ] fieldStatistics (Map<String, Integer>)
  - [ ] conversionTimeMs
- [ ] Create `ConversionResult<T>` wrapper:
  - [ ] result (Optional<T>)
  - [ ] statistics (ConversionStatistics)
  - [ ] warnings (List<String>)
- [ ] Update converter methods to return `ConversionResult` (or keep Optional for simplicity)
- [ ] Add `StatisticsCollector` that tracks conversions
- [ ] Implement `toJson()` method for statistics
- [ ] Implement `toTextReport()` method for human-readable output
- [ ] Add logging of statistics at INFO level
- [ ] Create `ConversionStatisticsTest`
- [ ] Update examples to show statistics

**Dependencies:** Sprint 01 US-01 (for integration)

---

## Sprint Tasks (Technical Improvements)

### T-05: Code Quality - Checkstyle
**Priority:** P2

- [ ] Add Checkstyle Maven plugin
- [ ] Configure Google Java Style or Sun conventions
- [ ] Fix style violations
- [ ] Add Checkstyle to CI build
- [ ] Document code style in CONTRIBUTING.md

### T-06: Code Quality - SpotBugs
**Priority:** P2

- [ ] Add SpotBugs Maven plugin
- [ ] Run analysis and review issues
- [ ] Fix high/medium priority bugs
- [ ] Add SpotBugs to CI build
- [ ] Document in build process

### T-07: Dependency Updates
**Priority:** P3

- [ ] Update RDF4J to latest 5.x version
- [ ] Update JUnit to latest 5.x version
- [ ] Update Maven plugins to latest versions
- [ ] Test all updates
- [ ] Update pom.xml

### T-08: Error Handling Standardization
**Priority:** P2

- [ ] Create exception hierarchy (ConversionException base)
- [ ] Use specific exceptions (ValidationException, ParsingException, etc.)
- [ ] Document exception contract in JavaDoc
- [ ] Add try-catch where appropriate
- [ ] Update tests to verify exception throwing

---

## Definition of Done

Same as Sprint 01, plus:
- [ ] JavaDoc coverage > 90%
- [ ] Code style checks passing
- [ ] SpotBugs analysis clean (no high/medium bugs)
- [ ] Performance benchmarks documented

---

## Sprint Risks & Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Name parsing complexity higher than estimated | Medium | High | Use existing library (jbibtex has parsing) |
| Performance optimization takes too long | Medium | Medium | Start simple, optimize if needed |
| Identifier validation library dependencies | Low | Low | Implement basic validation, defer checksums |

---

## Sprint Backlog Priority Order

1. **US-08** - Complete Type Support (feature completion)
2. **US-09** - Name Parsing (quality improvement)
3. **US-11** - Identifier Validation (data quality)
4. **US-10** - Date Validation (quality improvement)
5. **US-12** - JavaDoc (documentation)
6. **US-13** - Performance (optimization)
7. **US-14** - Statistics (nice-to-have)

---

## Sprint Review Checklist

- [ ] Demo all 14 BibTeX types converting
- [ ] Show complex name examples (van Gogh, de Gaulle, Jr.)
- [ ] Demonstrate identifier validation catching errors
- [ ] Show JavaDoc HTML output
- [ ] Present performance benchmarks
- [ ] Review code quality metrics
- [ ] Discuss areas for Sprint 03

---

## Notes

- Focus on **production quality** improvements
- US-08 and US-09 are highest priority
- Statistics (US-14) is nice-to-have, can move to backlog
