# Sprint 02: Production Quality Improvements

- **Sprint Goal:** Complete BibTeX type coverage (all 14 standard types), document BIBO→BibTeX gap, improve code quality and robustness
- **Priority:** MEDIUM-HIGH - Important for production deployment + thesis analysis material
- **Timeline:** 15-17 days (vs 12-14 originally)
- **Story Points:** 64 (was 46)

---

## Sprint Objectives

1. ✅ Support all 14 standard BibTeX entry types with BibTeX.com conventions
2. ✅ Document BIBO→BibTeX information gap for thesis analysis
3. ✅ Implement BibTeX.com field conventions (context-aware semantics)
4. ✅ Implement robust name and date parsing
5. ✅ Add comprehensive JavaDoc documentation
6. ✅ Improve identifier validation
7. ✅ Optimize performance for batch operations
8. ✅ Add detailed conversion reports/statistics

---

## User Stories

### US-08: Complete BibTeX Type Support (Extended)
**As a** user with diverse bibliography
**I want** support for all 14 standard BibTeX entry types from BibTeX.com
**So that** I can convert any standard bibliography without type errors

- **Story Points:** 13 (was 8, +5 for comprehensive coverage with conventions)
- **Priority:** P1 - High
- **Reference:** https://www.bibtex.com/e/entry-types/ (official source)

**All 14 Standard BibTeX Types:**
1. ✅ **@article** - Already supported (journal article)
2. ✅ **@book** - Already supported
3. ✅ **@inproceedings** - Already supported (conference paper)
4. ✅ **@proceedings** - Already supported (conference proceedings)
5. ✅ **@incollection** - Already supported (book chapter with own title)
6. ✅ **@phdthesis** - Already supported (PhD dissertation)
7. ✅ **@misc** - Already supported (fallback type)
8. ✅ **@booklet** - Work printed/bound but no publisher/institution
9. ✅ **@manual** - Technical documentation/manual
10. ✅ **@mastersthesis** - Master's thesis (distinct from phdthesis)
11. ✅ **@unpublished** - Document with author/title but not formally published
12. ✅ **@inbook** - Part of book (chapter/section with pages/chapter number)
13. ✅ **@techreport** - Technical report from institution/school
14. ✅ **@conference** - Alias for @inproceedings (already supported)

**Acceptance Criteria:**
- [x] All 14 types have dedicated `BiboDocumentType` enum values where applicable
- [x] BibTeX.com required/optional fields documented per type
- [x] Field conventions implemented (see US-24 for semantics)
- [x] Type-specific BIBO class mappings:
  - [x] @booklet → bibo:Document (generic)
  - [x] @manual → bibo:Manual
  - [x] @mastersthesis → bibo:Thesis (with degreeType field)
  - [x] @unpublished → bibo:Manuscript
  - [x] @inbook → bibo:BookSection
  - [x] @techreport → bibo:Report
- [x] Round-trip tests for each of 14 types
- [x] FIELD_MAPPING.md updated with BibTeX.com references

**Technical Tasks:**
- [x] Add new types to `BiboDocumentType` enum:
  - [x] `BOOKLET` → `bibo:Document` (no specific BIBO class)
  - [x] `MANUAL` → `bibo:Manual`
  - [x] `MANUSCRIPT` (for @unpublished) → `bibo:Manuscript`
  - [x] `BOOK_SECTION` (for @inbook) → `bibo:BookSection`
  - [x] `REPORT` (for @techreport) → `bibo:Report`
- [x] Add corresponding BIBO IRIs to `BiboVocabulary`
- [x] Add `degreeType` field to `BiboDocument.Builder`:
  - [x] Store "Master's thesis" vs "PhD dissertation"
  - [x] Map to `bibo:ThesisDegree` or custom property
- [x] Update `mapDocumentType()` BibTeX → BIBO:
  - [x] @booklet → BOOKLET
  - [x] @manual → MANUAL
  - [x] @mastersthesis → THESIS (with degreeType="Master's")
  - [x] @unpublished → MANUSCRIPT
  - [x] @inbook → BOOK_SECTION
  - [x] @techreport → REPORT
  - [x] @conference → CONFERENCE_PAPER (already supported)
- [x] Update `mapEntryType()` BIBO → BibTeX (reverse):
  - [x] BOOKLET → @booklet
  - [x] MANUAL → @manual
  - [x] MANUSCRIPT → @unpublished
  - [x] BOOK_SECTION → @inbook
  - [x] REPORT → @techreport
  - [x] THESIS with degreeType → @mastersthesis or @phdthesis
- [x] Implement smart @misc fallback when type unknown
- [x] Add BibTeX.com required/optional fields per type to documentation
- [x] Create `BibTeXTypeComprehensiveTest`:
  - [x] Test all 14 types BibTeX → BIBO → BibTeX roundtrip
  - [x] Verify required fields are preserved
  - [x] Test type-specific field conventions (see US-24)
- [x] Update FIELD_MAPPING.md:
  - [x] Table of all 14 types with BIBO mappings
  - [x] Link to BibTeX.com for each type
  - [x] Document required vs optional fields per type

**Dependencies:** Sprint 01 US-03 (extended fields), US-24 (conventions)

---

### US-09: Advanced Name Parsing
**As a** bibliographer working with international names
**I want** proper handling of name particles and complex names
**So that** author names are preserved correctly

- **Story Points:** 8
- **Priority:** P1 - High

**Acceptance Criteria:**
- [x] Handles BibTeX "First von Last, Jr" format correctly
- [x] Preserves name particles (von, van, de, etc.)
- [x] Supports middle names without loss
- [x] Handles single-name authors (e.g., "Plato")
- [x] Preserves suffixes (Jr, Sr, III, etc.)
- [x] Unicode/international names handled correctly
- [x] Proper round-trip for complex names
- [x] 20+ name parsing test cases

**Technical Tasks:**
- [x] Research BibTeX name parsing specification
- [x] Add `middleName`, `nameParticle`, `nameSuffix` to `BiboPersonName`
- [x] Implement BibTeX name tokenizer:
  - [x] Split by comma first (format detection)
  - [x] Identify particles (lowercase words before last name)
  - [x] Identify suffixes after comma
  - [x] Extract given/middle/family correctly
- [x] Map particles to `foaf:familyName` with particle included
- [x] Map suffixes to custom BIBO property or append to family name
- [x] Update `parseName()` method with new algorithm
- [x] Update `formatName()` to reconstruct BibTeX format
- [x] Add 20+ test cases in `PersonNameParsingTest`:
  - [x] "Ludwig van Beethoven"
  - [x] "Charles de Gaulle"
  - [x] "Vincent van Gogh"
  - [x] "Martin Luther King, Jr."
  - [x] "John Smith, III"
  - [x] Single names
  - [x] Hyphenated names
  - [x] Multiple middle names
  - [x] Unicode names (Chinese, Arabic)

**Dependencies:** None

---

### US-10: Enhanced Date Validation
**As a** user converting historical bibliographies
**I want** proper validation of dates including edge cases
**So that** I can trust the converted dates are correct

- **Story Points:** 5
- **Priority:** P2 - Medium

**Acceptance Criteria:**
- [x] Validates month is 1-12
- [x] Validates day based on month (28/29/30/31)
- [x] Handles leap year Feb 29 correctly
- [x] Accepts historical dates (year < 1000)
- [x] Warns on future dates (year > current year + 5)
- [x] Supports circa dates (extract year with note)
- [x] Multiple date format support
- [x] Clear error messages for invalid dates

**Technical Tasks:**
- [x] Create `DateValidator` utility class
- [x] Implement `isValidDate(int year, int month, int day)` with leap year logic
- [x] Implement `isLeapYear(int year)` helper
- [x] Add days-per-month validation array
- [x] Add warning for future dates (log but allow)
- [x] Improve `extractYearFromFreeForm()`:
  - [x] Try multiple patterns (YYYY, YYYY-MM-DD, MM/DD/YYYY, etc.)
  - [x] Handle "circa YYYY", "c. YYYY", "~YYYY"
  - [x] Store "circa" info in notes or custom field
- [x] Add `parseDate(String dateString)` with multiple format support
- [x] Create `DateValidationTest` with 15+ test cases
- [x] Update `BiboPublicationDate` validation in factory methods

**Dependencies:** None

---

### US-11: Identifier Validation Library
**As a** user
**I want** identifiers validated properly
**So that** invalid ISBNs, DOIs are caught early

- **Story Points:** 5
- **Priority:** P2 - Medium

**Acceptance Criteria:**
- [x] ISBN-10 checksum validation
- [x] ISBN-13 checksum validation
- [x] DOI format validation
- [x] ISSN checksum validation
- [x] Handle validation (format check)
- [x] URL validation
- [x] Invalid identifiers rejected with clear message
- [x] Tests for valid and invalid identifiers

**Technical Tasks:**
- [x] Create `IdentifierValidator` class
- [x] Implement `validateISBN10(String isbn)` with checksum
- [x] Implement `validateISBN13(String isbn)` with checksum
- [x] Implement `validateISSN(String issn)` with checksum
- [x] Implement `validateDOI(String doi)` with regex
- [x] Implement `validateHandle(String handle)` with format check
- [x] Implement `validateURL(String url)` using Java URL class
- [x] Update `classifyIsbn()` to use validation
- [x] Update `extractIdentifiers()` to validate before adding
- [x] Add `skipInvalidIdentifiers` configuration flag
- [x] Create `IdentifierValidationTest` with 30+ cases
- [x] Document identifier format requirements

**Dependencies:** Sprint 01 US-02 (validation exceptions)

---

### US-12: Comprehensive JavaDoc
**As a** developer using the library
**I want** complete JavaDoc for all public APIs
**So that** I understand how to use the converter

- **Story Points:** 5
- **Priority:** P2 - Medium

**Acceptance Criteria:**
- [x] All public classes have class-level JavaDoc
- [x] All public methods have JavaDoc with @param, @return, @throws
- [x] Complex algorithms explained
- [x] Examples provided for main entry points
- [x] Package-level documentation (package-info.java)
- [x] JavaDoc generates without warnings
- [x] HTML JavaDoc generated and reviewed

**Technical Tasks:**
- [x] Add class-level JavaDoc to all 20+ classes
- [x] Add method-level JavaDoc to all public methods (100+ methods)
- [x] Document `BibliographicConverter` interface with usage examples
- [x] Document `BiboDocument.Builder` with fluent API example
- [x] Document `BibTeXBibliographicConverter` with conversion examples
- [x] Create `package-info.java` for each package:
  - [x] `it.riccardosacco.bibobibtex.converter`
  - [x] `it.riccardosacco.bibobibtex.model.bibo`
  - [x] `it.riccardosacco.bibobibtex.vocbench`
- [x] Add @since tags (version 0.1.0)
- [x] Add @author tags
- [x] Configure Maven JavaDoc plugin
- [x] Generate JavaDoc HTML: `mvn javadoc:javadoc`
- [x] Review generated HTML for formatting issues

**Dependencies:** None

---

### US-13: Batch Conversion Performance
**As a** user with large bibliographies
**I want** efficient batch conversion of 1000+ entries
**So that** conversion doesn't take too long

- **Story Points:** 8
- **Priority:** P2 - Medium

**Acceptance Criteria:**
- [x] Converts 1000 entries in < 10 seconds
- [x] Memory-efficient (no OOM with 10,000 entries)
- [x] Batch conversion API with progress reporting
- [x] Parallel conversion option
- [x] Streaming API for very large files
- [x] Performance benchmarks documented
- [x] Tests with large datasets

**Technical Tasks:**
- [x] Create `BatchConverter` utility class
- [x] Implement `convertBatch(Collection<BibTeXEntry> entries)` → List<BiboDocument>
- [x] Add progress callback: `convertBatch(Collection, ProgressListener)`
- [x] Implement parallel conversion with Streams:
  - [x] `entries.parallelStream().map(converter::convertToBibo)`
  - [x] Configurable parallelism level
- [x] Add streaming API:
  - [x] `convertStream(Stream<BibTeXEntry>)` → Stream<BiboDocument>
  - [x] Allows processing without loading all into memory
- [x] Write performance benchmarks (JMH):
  - [x] Benchmark 100, 1000, 10000 entry conversions
  - [x] Compare sequential vs parallel
  - [x] Measure memory usage
- [x] Create large test dataset (generate 10000 synthetic entries)
- [x] Add performance test: `BatchConversionPerformanceTest`
- [x] Document performance characteristics

**Dependencies:** None

---

### US-14: Conversion Statistics and Reports
**As a** user
**I want** a report after conversion showing what was converted
**So that** I can verify the conversion quality

- **Story Points:** 5
- **Priority:** P3 - Low

**Acceptance Criteria:**
- [x] Conversion returns statistics object
- [x] Statistics include: total entries, successful, failed, warnings
- [x] Field-level stats (how many authors, identifiers, etc.)
- [x] Missing field warnings
- [x] Data quality warnings (e.g., future dates, invalid identifiers)
- [x] Report can be exported as JSON or text
- [x] Tests verify statistics accuracy

**Technical Tasks:**
- [x] Create `ConversionStatistics` class with fields:
  - [x] totalEntries, successfulConversions, failedConversions
  - [x] warningMessages (List<String>)
  - [x] fieldStatistics (Map<String, Integer>)
  - [x] conversionTimeMs
- [x] Create `ConversionResult<T>` wrapper:
  - [x] result (Optional<T>)
  - [x] statistics (ConversionStatistics)
  - [x] warnings (List<String>)
- [x] Update converter methods to return `ConversionResult` (or keep Optional for simplicity)
- [x] Add `StatisticsCollector` that tracks conversions
- [x] Implement `toJson()` method for statistics
- [x] Implement `toTextReport()` method for human-readable output
- [x] Add logging of statistics at INFO level
- [x] Create `ConversionStatisticsTest`
- [x] Update examples to show statistics

**Dependencies:** Sprint 01 US-01 (for integration)

---

### US-23: BIBO→BibTeX Information Gap Documentation
**As a** thesis student / documentation maintainer
**I want** comprehensive documentation of BIBO→BibTeX information loss
**So that** users understand limitations and thesis includes gap analysis

- **Story Points:** 8
- **Priority:** P2 - Medium (thesis material, not code)
- **Type:** Documentation/Analysis

**Context (Professor Feedback):**
> "BIBO è più dettagliato di bibtex... quando uno parta da BIBO (certo, con perdita di informazione inevitabile)... Specialmente il secondo punto può essere uno spunto anche da mostrare in sede di tesi per descrivere il gap informativo tra i diversi modelli."

**Acceptance Criteria:**
- [x] `docs/LIMITATIONS.md` created with technical gap analysis
- [x] Thesis material drafted (Italian): "Analisi Gap Modelli Bibliografici"
- [x] 5+ practical examples documented (conference details, affiliations, etc.)
- [x] Comparison tables BIBO vs BibTeX expressiveness
- [x] Heuristic mapping strategies documented
- [x] When-to-use guidance (BIBO vs BibTeX scenarios)

**Deliverables:**

1. **LIMITATIONS.md** (Technical Documentation - 3 SP)
   - **Section 1:** Information Loss Scenarios
     - Structured conference metadata → booktitle/series/note
     - Multiple author affiliations → note field
     - Rich publisher details → publisher string
     - Multiple identifiers same type → priority selection
     - Structured dates (ISO with time) → year/month/day only
   - **Section 2:** Heuristic Mapping Strategies
     - Conference with series + location + sponsors → what goes where?
     - Organizations vs institutions vs sponsors
     - Proceedings title vs conference name (booktitle choice)
   - **Section 3:** Best Practices
     - When to prefer BIBO (rich datasets, linked data)
     - When BibTeX suffices (citations, bibliographies)
     - Migration strategies (BIBO → BibTeX for LaTeX output)

2. **Thesis Chapter Material** (Italian - 5 SP)
   - **Capitolo:** "Gap Informativo tra Modelli Bibliografici"
   - **Sezione 1:** Confronto Espressività BIBO vs BibTeX
     - Tabella comparativa capacità espressive
     - Esempi concreti di metadati perduti
   - **Sezione 2:** Casi d'Uso e Scenari
     - Conference paper dettagliata: esempio RDF Turtle snippet
     - Output BibTeX risultante con perdita annotata
     - Strategie di mitigazione (note field, custom fields)
   - **Sezione 3:** Implicazioni per l'Integrazione VocBench
     - VocBench usa BIBO nativo
     - Export BibTeX: limitazioni da comunicare all'utente
     - Possibili estensioni future (custom BibTeX fields?)

**Technical Tasks:**
- [x] Create `docs/LIMITATIONS.md` file
- [x] Document 5 key information loss scenarios with examples
- [x] Create BIBO vs BibTeX comparison table (Markdown)
- [x] Write heuristic strategies for each scenario
- [x] Draft thesis chapter in Italian (separate doc or in LIMITATIONS.md)
- [x] Include Turtle code snippets for BIBO examples
- [x] Include BibTeX output examples
- [x] Add "Limitations" section to main README.md linking to LIMITATIONS.md
- [x] Review with professor (optional feedback loop)

**Examples to Document:**

**Example 1: Rich Conference Metadata**
```turtle
:conf2024 a bibo:ConferencePaper ;
    dcterms:title "My Paper" ;
    dcterms:isPartOf :conference .

:conference a bibo:Conference ;
    dcterms:title "International Conference on Knowledge Graphs" ;
    bibo:series "ICKG" ;
    dcterms:spatial "Berlin, Germany" ;
    bibo:organizer "ACM", "IEEE" ;
    dcterms:date "2024-06-15/2024-06-17" .
```
**BibTeX Output** (information loss):
```bibtex
@inproceedings{conf2024,
  title = {My Paper},
  booktitle = {International Conference on Knowledge Graphs},
  year = {2024},
  address = {Berlin, Germany},  % Conference location (convention!)
  note = {Series: ICKG. Organizers: ACM, IEEE. Dates: June 15-17}
}
```
**Lost:** Structured dates, multiple organizers as separate entities, series as formal property

**Example 2: Multiple Author Affiliations**
```turtle
:author1 a foaf:Person ;
    foaf:name "Alice Smith" ;
    foaf:affiliation :affil1, :affil2 .

:affil1 a foaf:Organization ;
    foaf:name "University of Example" ;
    dcterms:spatial "Boston, MA, USA" .
```
**BibTeX Output:**
```bibtex
author = {Alice Smith},
note = {Affiliation: University of Example, Boston, MA, USA (primary)}
```

**Dependencies:** None (pure documentation)

---

### US-24: BibTeX.com Field Conventions Implementation
**As a** user expecting standard BibTeX behavior
**I want** field semantics to follow BibTeX.com conventions
**So that** `address` in @inproceedings means conference location (not publisher)

- **Story Points:** 5
- **Priority:** P1 - High
- **Reference:** https://www.bibtex.com/e/entry-types/ (authoritative source)

**Context (Professor Feedback):**
> "Devo dire che non tiene conto delle varie convenzioni (ad esempio, quando sei in @inproceedings, per convenzione il campo address tiene il posto dove si è tenuta la conferenza, non l'indirizzo del publisher) però insomma, è cmq la fonte ufficiale"

**Problem:** BibTeX field semantics are **context-dependent** on entry type:
- `address` in @book = publisher's address
- `address` in @inproceedings = conference location
- `organization` in @manual = publisher
- `organization` in @proceedings = conference sponsor

Current implementation treats fields uniformly → breaks conventions.

**Acceptance Criteria:**
- [x] Context-aware field mapping based on entry type
- [x] `address` field:
  - [x] @book, @article → publisher location (dcterms:spatial or custom)
  - [x] @inproceedings → conference location (part of container metadata)
- [x] `organization` field:
  - [x] @manual → publisher (dcterms:publisher)
  - [x] @proceedings → conference organizer (custom property)
- [x] `type` field override (e.g., "Master's thesis" in @mastersthesis)
- [x] `howpublished` field (@misc, @unpublished):
  - [x] If URL → map to foaf:page
  - [x] Otherwise → note field
- [x] Tests for each convention with specific entry types
- [x] FIELD_MAPPING.md section "Context-Dependent Field Conventions"

**Technical Tasks:**
- [x] Refactor `convertToBibo()` to use entry type in field resolution:
  - [x] Create `resolveAddress(BibTeXEntry entry)` → considers type
  - [x] Create `resolveOrganization(BibTeXEntry entry)` → considers type
  - [x] Create `resolveType(BibTeXEntry entry)` → handles degree type etc.
- [x] Update `BiboDocument.Builder` to support:
  - [x] `conferenceLocation` (distinct from `placeOfPublication`)
  - [x] `conferenceOrganizer` (distinct from `publisher`)
  - [x] `degreeType` (for thesis type specification)
- [x] Update BIBO model generation:
  - [x] Conference location → part of container (isPartOf) metadata
  - [x] Organizer → custom BIBO property or note
- [x] Implement reverse conversion (BIBO → BibTeX):
  - [x] Extract conference location from container
  - [x] Populate `address` field correctly per type
  - [x] Populate `organization` field correctly per type
- [x] Create `BibTeXConventionsTest`:
  - [x] Test @inproceedings with address = "Berlin, Germany"
  - [x] Verify round-trip preserves conference location
  - [x] Test @book with address = "New York, NY"
  - [x] Verify publisher address vs conference location distinction
  - [x] Test @manual with organization = "GNU Project"
  - [x] Test @proceedings with organization = "ACM"
- [x] Update FIELD_MAPPING.md:
  - [x] Add "Field Conventions by Entry Type" section
  - [x] Table: field × entry type → semantic meaning
  - [x] Examples for each context-dependent field

**Context-Dependent Fields to Implement:**

| Field | @book/@article | @inproceedings/@proceedings | @manual | @mastersthesis/@phdthesis |
|-------|----------------|----------------------------|---------|---------------------------|
| `address` | Publisher location | Conference location | Publisher location | University location |
| `organization` | N/A | Conference organizer (optional) | Publishing org | N/A |
| `institution` | N/A | N/A | N/A | University/School |
| `type` | N/A | N/A | N/A | "Master's thesis" or "PhD dissertation" |
| `howpublished` | N/A | N/A | Distribution method | N/A |

**Dependencies:** US-08 (type support), Sprint 01 US-03 (extended fields)

---

## Sprint Tasks (Technical Improvements)

### T-05: Code Quality - Checkstyle
**Priority:** P2

- [x] Add Checkstyle Maven plugin
- [x] Configure Google Java Style or Sun conventions
- [x] Fix style violations
- [x] Add Checkstyle to CI build
- [x] Document code style in CONTRIBUTING.md

### T-06: Code Quality - SpotBugs
**Priority:** P2

- [x] Add SpotBugs Maven plugin
- [x] Run analysis and review issues
- [x] Fix high/medium priority bugs
- [x] Add SpotBugs to CI build
- [x] Document in build process

### T-07: Dependency Updates
**Priority:** P3

- [x] Update RDF4J to latest 5.x version
- [x] Update JUnit to latest 5.x version
- [x] Update Maven plugins to latest versions
- [x] Test all updates
- [x] Update pom.xml

### T-08: Error Handling Standardization
**Priority:** P2

- [x] Create exception hierarchy (ConversionException base)
- [x] Use specific exceptions (ValidationException, ParsingException, etc.)
- [x] Document exception contract in JavaDoc
- [x] Add try-catch where appropriate
- [x] Update tests to verify exception throwing

---

## Definition of Done

Same as Sprint 01, plus:
- [x] JavaDoc coverage > 90%
- [x] Code style checks passing
- [x] SpotBugs analysis clean (no high/medium bugs)
- [x] Performance benchmarks documented

---

## Sprint Risks & Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Name parsing complexity higher than estimated | Medium | High | Use existing library (jbibtex has parsing) |
| Performance optimization takes too long | Medium | Medium | Start simple, optimize if needed |
| Identifier validation library dependencies | Low | Low | Implement basic validation, defer checksums |

---

## Sprint Backlog Priority Order

1. **US-24** - BibTeX.com Conventions (P1 - critical for correctness)
2. **US-08** - Complete Type Support (P1 - feature completion, 14 types)
3. **US-09** - Name Parsing (P1 - quality improvement)
4. **US-23** - BIBO→BibTeX Gap Documentation (P2 - thesis material)
5. **US-11** - Identifier Validation (P2 - data quality)
6. **US-10** - Date Validation (P2 - quality improvement)
7. **US-12** - JavaDoc (P2 - documentation)
8. **US-13** - Performance (P2 - optimization)
9. **US-14** - Statistics (P3 - nice-to-have)

---

## Sprint Review Checklist

- [x] Demo all 14 BibTeX types converting (US-08)
- [x] Show BibTeX.com conventions working (@inproceedings address = conference location) (US-24)
- [x] Present LIMITATIONS.md document with gap analysis examples (US-23)
- [x] Show complex name examples (van Gogh, de Gaulle, Jr.) (US-09)
- [x] Demonstrate identifier validation catching errors (US-11)
- [x] Show JavaDoc HTML output (US-12)
- [x] Present performance benchmarks (US-13)
- [x] Review code quality metrics (T-05, T-06)
- [x] Discuss thesis chapter material (US-23)
- [x] Discuss areas for Sprint 03

---

## Notes

- Focus on **production quality** + **BibTeX completeness** + **thesis material**
- **US-24** (conventions) is CRITICAL - must implement before US-08 type expansion
- **US-08** (all 14 types) and **US-09** (name parsing) are highest coding priority
- **US-23** (gap documentation) provides thesis chapter material - pure documentation
- Statistics (US-14) is nice-to-have, can defer to backlog if needed
- **Professor Feedback Integration:**
  - Complete BibTeX type coverage using BibTeX.com as reference ✓
  - Document BIBO→BibTeX gap for thesis analysis ✓
  - Implement field conventions (address, organization context-dependent) ✓
