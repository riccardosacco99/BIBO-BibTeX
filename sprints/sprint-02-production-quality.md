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
8. [ ] **@booklet** - Work printed/bound but no publisher/institution
9. [ ] **@manual** - Technical documentation/manual
10. [ ] **@mastersthesis** - Master's thesis (distinct from phdthesis)
11. [ ] **@unpublished** - Document with author/title but not formally published
12. [ ] **@inbook** - Part of book (chapter/section with pages/chapter number)
13. [ ] **@techreport** - Technical report from institution/school
14. [ ] **@conference** - Alias for @inproceedings (already supported)

**Acceptance Criteria:**
- [ ] All 14 types have dedicated `BiboDocumentType` enum values where applicable
- [ ] BibTeX.com required/optional fields documented per type
- [ ] Field conventions implemented (see US-24 for semantics)
- [ ] Type-specific BIBO class mappings:
  - [ ] @booklet → bibo:Document (generic)
  - [ ] @manual → bibo:Manual
  - [ ] @mastersthesis → bibo:Thesis (with degreeType field)
  - [ ] @unpublished → bibo:Manuscript
  - [ ] @inbook → bibo:BookSection
  - [ ] @techreport → bibo:Report
- [ ] Round-trip tests for each of 14 types
- [ ] FIELD_MAPPING.md updated with BibTeX.com references

**Technical Tasks:**
- [ ] Add new types to `BiboDocumentType` enum:
  - [ ] `BOOKLET` → `bibo:Document` (no specific BIBO class)
  - [ ] `MANUAL` → `bibo:Manual`
  - [ ] `MANUSCRIPT` (for @unpublished) → `bibo:Manuscript`
  - [ ] `BOOK_SECTION` (for @inbook) → `bibo:BookSection`
  - [ ] `REPORT` (for @techreport) → `bibo:Report`
- [ ] Add corresponding BIBO IRIs to `BiboVocabulary`
- [ ] Add `degreeType` field to `BiboDocument.Builder`:
  - [ ] Store "Master's thesis" vs "PhD dissertation"
  - [ ] Map to `bibo:ThesisDegree` or custom property
- [ ] Update `mapDocumentType()` BibTeX → BIBO:
  - [ ] @booklet → BOOKLET
  - [ ] @manual → MANUAL
  - [ ] @mastersthesis → THESIS (with degreeType="Master's")
  - [ ] @unpublished → MANUSCRIPT
  - [ ] @inbook → BOOK_SECTION
  - [ ] @techreport → REPORT
  - [ ] @conference → CONFERENCE_PAPER (already supported)
- [ ] Update `mapEntryType()` BIBO → BibTeX (reverse):
  - [ ] BOOKLET → @booklet
  - [ ] MANUAL → @manual
  - [ ] MANUSCRIPT → @unpublished
  - [ ] BOOK_SECTION → @inbook
  - [ ] REPORT → @techreport
  - [ ] THESIS with degreeType → @mastersthesis or @phdthesis
- [ ] Implement smart @misc fallback when type unknown
- [ ] Add BibTeX.com required/optional fields per type to documentation
- [ ] Create `BibTeXTypeComprehensiveTest`:
  - [ ] Test all 14 types BibTeX → BIBO → BibTeX roundtrip
  - [ ] Verify required fields are preserved
  - [ ] Test type-specific field conventions (see US-24)
- [ ] Update FIELD_MAPPING.md:
  - [ ] Table of all 14 types with BIBO mappings
  - [ ] Link to BibTeX.com for each type
  - [ ] Document required vs optional fields per type

**Dependencies:** Sprint 01 US-03 (extended fields), US-24 (conventions)

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
- [ ] `docs/LIMITATIONS.md` created with technical gap analysis
- [ ] Thesis material drafted (Italian): "Analisi Gap Modelli Bibliografici"
- [ ] 5+ practical examples documented (conference details, affiliations, etc.)
- [ ] Comparison tables BIBO vs BibTeX expressiveness
- [ ] Heuristic mapping strategies documented
- [ ] When-to-use guidance (BIBO vs BibTeX scenarios)

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
- [ ] Create `docs/LIMITATIONS.md` file
- [ ] Document 5 key information loss scenarios with examples
- [ ] Create BIBO vs BibTeX comparison table (Markdown)
- [ ] Write heuristic strategies for each scenario
- [ ] Draft thesis chapter in Italian (separate doc or in LIMITATIONS.md)
- [ ] Include Turtle code snippets for BIBO examples
- [ ] Include BibTeX output examples
- [ ] Add "Limitations" section to main README.md linking to LIMITATIONS.md
- [ ] Review with professor (optional feedback loop)

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
- [ ] Context-aware field mapping based on entry type
- [ ] `address` field:
  - [ ] @book, @article → publisher location (dcterms:spatial or custom)
  - [ ] @inproceedings → conference location (part of container metadata)
- [ ] `organization` field:
  - [ ] @manual → publisher (dcterms:publisher)
  - [ ] @proceedings → conference organizer (custom property)
- [ ] `type` field override (e.g., "Master's thesis" in @mastersthesis)
- [ ] `howpublished` field (@misc, @unpublished):
  - [ ] If URL → map to foaf:page
  - [ ] Otherwise → note field
- [ ] Tests for each convention with specific entry types
- [ ] FIELD_MAPPING.md section "Context-Dependent Field Conventions"

**Technical Tasks:**
- [ ] Refactor `convertToBibo()` to use entry type in field resolution:
  - [ ] Create `resolveAddress(BibTeXEntry entry)` → considers type
  - [ ] Create `resolveOrganization(BibTeXEntry entry)` → considers type
  - [ ] Create `resolveType(BibTeXEntry entry)` → handles degree type etc.
- [ ] Update `BiboDocument.Builder` to support:
  - [ ] `conferenceLocation` (distinct from `placeOfPublication`)
  - [ ] `conferenceOrganizer` (distinct from `publisher`)
  - [ ] `degreeType` (for thesis type specification)
- [ ] Update BIBO model generation:
  - [ ] Conference location → part of container (isPartOf) metadata
  - [ ] Organizer → custom BIBO property or note
- [ ] Implement reverse conversion (BIBO → BibTeX):
  - [ ] Extract conference location from container
  - [ ] Populate `address` field correctly per type
  - [ ] Populate `organization` field correctly per type
- [ ] Create `BibTeXConventionsTest`:
  - [ ] Test @inproceedings with address = "Berlin, Germany"
  - [ ] Verify round-trip preserves conference location
  - [ ] Test @book with address = "New York, NY"
  - [ ] Verify publisher address vs conference location distinction
  - [ ] Test @manual with organization = "GNU Project"
  - [ ] Test @proceedings with organization = "ACM"
- [ ] Update FIELD_MAPPING.md:
  - [ ] Add "Field Conventions by Entry Type" section
  - [ ] Table: field × entry type → semantic meaning
  - [ ] Examples for each context-dependent field

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

- [ ] Demo all 14 BibTeX types converting (US-08)
- [ ] Show BibTeX.com conventions working (@inproceedings address = conference location) (US-24)
- [ ] Present LIMITATIONS.md document with gap analysis examples (US-23)
- [ ] Show complex name examples (van Gogh, de Gaulle, Jr.) (US-09)
- [ ] Demonstrate identifier validation catching errors (US-11)
- [ ] Show JavaDoc HTML output (US-12)
- [ ] Present performance benchmarks (US-13)
- [ ] Review code quality metrics (T-05, T-06)
- [ ] Discuss thesis chapter material (US-23)
- [ ] Discuss areas for Sprint 03

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
