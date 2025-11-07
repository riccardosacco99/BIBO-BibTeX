# Sprint 00: Critical Hot Fixes (Pre-MVP)

**Sprint Duration:** 1 week
**Sprint Goal:** Fix critical architectural issues identified by professor before continuing with Sprint 01
**Priority:** 🔴 CRITICAL - BLOCKING Sprint 01
**Date Created:** 2025-11-07

---

## Sprint Context

**Why This Sprint Exists:**
Professor feedback identified 4 critical issues that must be fixed before proceeding:
1. `bibo:sequence` property doesn't exist in BIBO ontology (invented by us!)
2. BibTeX escape characters not converted to Unicode
3. Limited document type support (missing @inproceedings, @proceedings)
4. Turtle should be default serialization format

These issues affect the **correctness** and **validity** of our RDF output and must be resolved immediately.

---

## Sprint Objectives

1. 🎯 Remove non-existent `bibo:sequence` and implement proper RDF Lists for author ordering
2. 🎯 Convert BibTeX escape sequences to Unicode characters
3. 🎯 Support InProceedings, Proceedings, and InCollection document types
4. 🎯 Configure Turtle as default RDF serialization format
5. 🎯 Maintain 100% round-trip test success rate

---

## Hot Fixes

### FIX-01: Replace bibo:sequence with RDF Lists ⚠️ CRITICAL

**Priority:** P0 - BLOCKING
**Estimated Hours:** 8-12 hours

**Problem:**
- We created `bibo:sequence` property which doesn't exist in BIBO
- Current code: `model.add(person, BiboVocabulary.ORDER, VF.createLiteral(order))`
- Visible in output RDF, making our data invalid

**Correct Approach:**
Use RDF Collections (Lists) as specified in RDF standard:

```turtle
ex:publication a bibo:Article ;
    bibo:authorList (
        [ a foaf:Person ; foaf:name "First Author" ]
        [ a foaf:Person ; foaf:name "Second Author" ]
        [ a foaf:Person ; foaf:name "Third Author" ]
    ) .
```

**Tasks:**
- [ ] Remove `BiboVocabulary.ORDER` constant
- [ ] Research RDF4J RDF Collections API
- [ ] Update `BiboDocument.Builder.build()` to use RDF Lists for authors
  - [ ] Replace loop adding individual `dcterms:creator` statements
  - [ ] Use `org.eclipse.rdf4j.model.util.RDFCollections.asRDF()`
  - [ ] Create `bibo:authorList` property in vocabulary
- [ ] Update `BiboVocabulary` to add `AUTHOR_LIST` constant
- [ ] Update reverse conversion (`ReverseConversion.java`) to read from RDF Lists
  - [ ] Use `RDFCollections.asValues()` to extract list
  - [ ] Preserve order from list
- [ ] Update all test expectations (RDF files in `test-data/bibo/`)
- [ ] Verify round-trip tests still pass
- [ ] Add specific test for author ordering preservation

**Acceptance Criteria:**
- ✅ No references to `bibo:sequence` anywhere in code
- ✅ Authors stored in RDF List using `bibo:authorList`
- ✅ Author order preserved in round-trip conversions
- ✅ All existing tests updated and passing
- ✅ New test verifies 5+ author ordering

**Resources:**
- RDF4J Collections: https://rdf4j.org/javadoc/latest/org/eclipse/rdf4j/model/util/RDFCollections.html
- RDF Collections spec: https://www.w3.org/TR/rdf-schema/#ch_collectionvocab
- BIBO vocab: Check if `bibo:authorList` exists, or use custom property

**Risks:**
- RDF Lists more complex than simple properties
- May need to handle empty lists
- Need to ensure list ordering is preserved through serialization

---

### FIX-02: BibTeX Unicode Conversion

**Priority:** P0 - CRITICAL
**Estimated Hours:** 6-10 hours

**Problem:**
Names with accented characters like `Guasch-Ferr{\'e}` remain escaped in RDF output.
Should convert to `Guasch-Ferré` (proper Unicode).

**Common BibTeX Escapes:**
```
{\'e} → é        {\`a} → à        {\"u} → ü
{\^o} → ô        {\~n} → ñ        {\c{c}} → ç
{\aa} → å        {\o} → ø         {\ae} → æ
{\oe} → œ        {\ss} → ß        {\l} → ł
{\'E} → É        etc. (all uppercase variants)
```

**Tasks:**
- [ ] Research complete list of BibTeX escape sequences
  - [ ] Check LaTeX/BibTeX documentation
  - [ ] Check if JBibTeX library has utilities for this
- [ ] Create `BibTeXUnicodeConverter` utility class
  - [ ] `static String decode(String bibtexText)` - escapes → Unicode
  - [ ] `static String encode(String unicodeText)` - Unicode → escapes (for export)
  - [ ] Use regex or string replacement map
- [ ] Create comprehensive escape sequence map (50+ sequences)
- [ ] Apply `decode()` to all text fields in `convertToBibo()`:
  - [ ] Author names (given, family, full)
  - [ ] Title, subtitle
  - [ ] Publisher, place of publication
  - [ ] Journal/container title
  - [ ] Notes, abstract
- [ ] Check if JBibTeX library handles UTF-8 BibTeX files
  - [ ] If yes: don't encode on export, use UTF-8 directly
  - [ ] If no: apply `encode()` before generating BibTeX
- [ ] Create `BibTeXUnicodeConverterTest` with 50+ test cases
- [ ] Update existing test data to include accented characters
- [ ] Test with real-world examples from PapersDB

**Acceptance Criteria:**
- ✅ All BibTeX escape sequences converted to Unicode on import
- ✅ 50+ escape sequences supported
- ✅ Names display correctly: "Guasch-Ferré" not "Guasch-Ferr{\'e}"
- ✅ Round-trip conversion preserves accented characters
- ✅ Test coverage for all common escape sequences
- ✅ Works with examples from professor's PapersDB files

**Resources:**
- LaTeX special characters: https://en.wikibooks.org/wiki/LaTeX/Special_Characters
- BibTeX documentation
- JBibTeX source code (may have utils)

**Example Mapping:**
```java
private static final Map<String, String> ESCAPE_MAP = Map.ofEntries(
    entry("{\\'e}", "é"),
    entry("{\\'E}", "É"),
    entry("{\\`a}", "à"),
    // ... 50+ more
);
```

---

### FIX-03: Turtle as Default Serialization

**Priority:** P1 - HIGH
**Estimated Hours:** 4-6 hours

**Problem:**
- Currently using RDF/XML by default (verbose, less readable)
- RDF/XML output shows distributed descriptions (same subject in multiple blocks)
- Professor prefers Turtle format with optimal settings

**Target Turtle Output:**
```turtle
@prefix bibo: <http://purl.org/ontology/bibo/> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .

<http://example.org/bibo/pub123> a bibo:Article, bibo:Document ;
    dcterms:title "The Mediterranean diet and health: a comprehensive overview" ;
    dcterms:identifier "guasch2021mediterranean" ;
    bibo:authorList (
        [ a foaf:Person ;
          foaf:name "Guasch-Ferré, Marta" ;
          foaf:givenName "Marta" ;
          foaf:familyName "Guasch-Ferré"
        ]
        [ a foaf:Person ;
          foaf:name "Willett, Walter C" ;
          foaf:givenName "Walter C" ;
          foaf:familyName "Willett"
        ]
    ) ;
    dcterms:issued "2021"^^xsd:gYear ;
    dcterms:publisher "Wiley Online Library" ;
    bibo:volume "290" ;
    bibo:issue "3" ;
    bibo:pages "549--566" .
```

**Tasks:**
- [ ] Add Turtle serialization utility methods to `BiboDocument`:
  - [ ] `String toTurtle()` - default pretty-print format
  - [ ] `void writeTurtle(OutputStream out)`
  - [ ] `void writeTurtle(Writer writer)`
- [ ] Configure Turtle writer with optimal settings:
  ```java
  TurtleWriter writer = new TurtleWriter(out);
  WriterConfig config = writer.getWriterConfig();
  config.set(BasicWriterSettings.PRETTY_PRINT, true);
  config.set(BasicWriterSettings.INLINE_BLANK_NODES, true);
  config.set(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true);
  config.set(BasicWriterSettings.RDF_LANGSTRING_TO_LANG_LITERAL, true);
  ```
- [ ] Keep RDF/XML support but not as default:
  - [ ] `String toRdfXml()`
  - [ ] `void writeRdfXml(OutputStream out)`
- [ ] Add format parameter to export methods:
  - [ ] `void write(OutputStream out, RDFFormat format)`
  - [ ] Support: TURTLE (default), RDFXML, JSONLD, NTRIPLES
- [ ] Update `SampleConversion` example to output Turtle
- [ ] Update `ReverseConversion` example to read Turtle
- [ ] Convert all test data in `test-data/bibo/` from RDF/XML to Turtle
- [ ] Update documentation to show Turtle examples

**Acceptance Criteria:**
- ✅ Turtle is default output format
- ✅ Turtle output is pretty-printed and readable
- ✅ Blank nodes inlined in Turtle output
- ✅ RDF/XML still available as option
- ✅ All test data converted to Turtle
- ✅ Examples use Turtle format
- ✅ Documentation updated

**Resources:**
- RDF4J Turtle Writer: https://rdf4j.org/javadoc/latest/org/eclipse/rdf4j/rio/turtle/TurtleWriter.html
- RDF4J Writer Config: https://rdf4j.org/javadoc/latest/org/eclipse/rdf4j/rio/helpers/BasicWriterSettings.html

---

### FIX-04: InProceedings and Proceedings Support

**Priority:** P0 - CRITICAL (for thesis)
**Estimated Hours:** 12-16 hours

**Problem:**
- Missing support for `@inproceedings` (conference paper)
- Missing support for `@proceedings` (entire conference proceedings)
- Missing support for `@incollection` (chapter in edited collection)
- These are VERY common in academic publishing

**Professor's Context:**
- Same paper can be modeled as `@inproceedings` OR `@incollection`
  - `@inproceedings`: emphasizes it's a conference paper
  - `@incollection`: emphasizes the proceedings as a book volume
- BIBO allows full reification: separate resources for paper AND proceedings
- BibTeX is compact: everything in one entry

**BibTeX Examples:**
```bibtex
@inproceedings{key,
  author = {Smith, John},
  title = {My Conference Paper},
  booktitle = {Proceedings of XYZ 2024},
  year = {2024},
  editor = {Editor, Jane},
  pages = {100--110},
  publisher = {ACM},
  series = {LNCS},
  volume = {12345}
}

@proceedings{key,
  title = {Proceedings of XYZ 2024},
  year = {2024},
  editor = {Editor, Jane},
  publisher = {ACM},
  series = {LNCS},
  volume = {12345},
  isbn = {978-1-234567-89-0}
}

@incollection{key,
  author = {Smith, John},
  title = {My Chapter},
  booktitle = {Edited Book Title},
  editor = {Editor, Jane},
  publisher = {Springer},
  year = {2024},
  pages = {50--75},
  chapter = {3}
}
```

**BIBO Modeling:**
```turtle
# The conference paper
ex:paper1 a bibo:Article, bibo:Document ;
    dcterms:title "My Conference Paper" ;
    bibo:authorList ( [ a foaf:Person ; foaf:name "John Smith" ] ) ;
    dcterms:isPartOf ex:proceedings1 ;
    bibo:pages "100--110" .

# The proceedings (separate resource)
ex:proceedings1 a bibo:Proceedings, bibo:Document ;
    dcterms:title "Proceedings of XYZ 2024" ;
    bibo:editor [ a foaf:Person ; foaf:name "Jane Editor" ] ;
    dcterms:publisher "ACM" ;
    bibo:series "LNCS" ;
    bibo:volume "12345" ;
    dcterms:issued "2024"^^xsd:gYear .
```

**Tasks:**
- [ ] Add to `BiboDocumentType`:
  - [ ] `PROCEEDINGS` (maps to `bibo:Proceedings`)
  - [ ] Verify `CONFERENCE_PAPER` already exists (it does)
  - [ ] `BOOK_SECTION` / `CHAPTER` (maps to `bibo:Chapter` or keep as `BOOK_SECTION`)
- [ ] Add to `BiboVocabulary`:
  - [ ] `PROCEEDINGS = iri("Proceedings")`
  - [ ] Verify `CHAPTER` already exists (it does)
- [ ] Update `BibTeXBibliographicConverter.mapEntryType()`:
  - [ ] `inproceedings` → `CONFERENCE_PAPER`
  - [ ] `conference` → `CONFERENCE_PAPER` (alias)
  - [ ] `proceedings` → `PROCEEDINGS`
  - [ ] `incollection` → `BOOK_SECTION`
  - [ ] `inbook` → `BOOK_SECTION`
  - [ ] `collection` → `BOOK` (or new COLLECTION type?)
- [ ] Handle `booktitle` field:
  - [ ] For `@inproceedings`: booktitle is proceedings title
  - [ ] Store as `containerTitle` in BiboDocument
  - [ ] In RDF, create separate proceedings resource if needed
  - [ ] Link via `dcterms:isPartOf`
- [ ] Handle proceedings-specific fields:
  - [ ] `editor` - already supported
  - [ ] `series` - add to BiboDocument (from US-03)
  - [ ] `volume` - already supported
  - [ ] `organization` - add to BiboDocument
- [ ] Update reverse conversion:
  - [ ] Map `PROCEEDINGS` → `@proceedings`
  - [ ] Map `CONFERENCE_PAPER` → `@inproceedings`
  - [ ] Extract booktitle from `isPartOf` relationship
- [ ] Copy professor's test files:
  - [ ] `test-data/professor-examples/PapersDB_MIUR.bib`
  - [ ] `test-data/professor-examples/PapersDB.bib`
- [ ] Create comprehensive tests:
  - [ ] `testInProceedingsConversion()`
  - [ ] `testProceedingsConversion()`
  - [ ] `testInCollectionConversion()`
  - [ ] `testProceedingsAsBook()` (with series, volume, ISBN)
- [ ] Test with actual entries from PapersDB files
- [ ] Document modeling decisions in `FIELD_MAPPING.md`

**Acceptance Criteria:**
- ✅ `@inproceedings` entries convert successfully
- ✅ `@proceedings` entries convert successfully
- ✅ `@incollection` entries convert successfully
- ✅ `booktitle` correctly becomes container/proceedings title
- ✅ Proceedings with series/volume/ISBN handled correctly
- ✅ Round-trip conversion works for all new types
- ✅ Tests pass with PapersDB examples
- ✅ Documentation explains modeling approach

**Complexity:**
This is the most complex fix because:
- Requires modeling proceedings as separate resources
- Need to handle relationship between paper and proceedings
- BibTeX is compact (one entry), BIBO is reified (multiple resources)
- Need to decide: always create separate proceedings resource, or inline?

**Design Decision Needed:**
For `@inproceedings`, should we:
1. **Option A:** Always create separate proceedings resource
   - Pro: Full reification, allows proceedings to be referenced by multiple papers
   - Con: More complex, may create duplicate proceedings resources
2. **Option B:** Inline proceedings info in paper resource
   - Pro: Simpler, matches BibTeX structure
   - Con: Can't share proceedings across papers, less semantic
3. **Option C:** Hybrid - create separate resource only if proceedings has rich metadata
   - Pro: Balances simplicity and semantics
   - Con: Inconsistent modeling

**Recommendation:** Start with Option A (always separate), can optimize later.

---

## Additional Tasks

### T-01: Update Test Data
**Estimated Hours:** 2-4 hours

- [ ] Convert all `test-data/bibo/*.rdf` files from RDF/XML to Turtle
- [ ] Update tests to expect new RDF structure (Lists instead of sequence)
- [ ] Add test files for InProceedings
- [ ] Copy professor's examples to `test-data/professor-examples/`

### T-02: Update Documentation
**Estimated Hours:** 2-3 hours

- [ ] Update CLAUDE.md:
  - [ ] Remove references to bibo:sequence
  - [ ] Document RDF List approach for authors
  - [ ] Document Turtle as default format
  - [ ] Document Unicode handling
  - [ ] Document InProceedings modeling
- [ ] Update README with corrected examples
- [ ] Update example outputs to show Turtle

### T-03: Code Cleanup
**Estimated Hours:** 1-2 hours

- [ ] Remove `BiboVocabulary.ORDER` constant
- [ ] Remove all references to "sequence" in comments/docs
- [ ] Add JavaDoc to new utility classes
- [ ] Run linter/formatter

---

## Definition of Done

For this sprint to be complete:

- ✅ All FIX-01 to FIX-04 tasks completed
- ✅ Zero references to `bibo:sequence` in code or output
- ✅ Authors stored in RDF Lists with order preserved
- ✅ BibTeX escapes converted to Unicode
- ✅ Turtle is default serialization format
- ✅ InProceedings, Proceedings, InCollection supported
- ✅ Round-trip tests pass at 100%
- ✅ New tests added for all fixes (20+ new tests)
- ✅ All existing tests updated and passing
- ✅ Test data converted to Turtle format
- ✅ Documentation updated
- ✅ Code reviewed and cleaned up
- ✅ Build passes: `mvn clean package`
- ✅ Professor reviews and approves changes

---

## Sprint Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| RDF Lists API complex | Medium | High | Study RDF4J docs carefully, start with simple example |
| Breaking existing tests | High | High | Update tests incrementally, keep old versions for comparison |
| Unicode map incomplete | Medium | Medium | Research thoroughly, test with real examples, can add more later |
| InProceedings modeling too complex | Medium | High | Start simple (Option A), get professor feedback early |
| Time estimate too low | Medium | High | Prioritize FIX-01 and FIX-02, FIX-04 can continue into Sprint 01 if needed |

---

## Sprint Backlog Priority

1. **FIX-01** - bibo:sequence removal (BLOCKING everything else)
2. **FIX-02** - Unicode conversion (HIGH visibility issue)
3. **FIX-03** - Turtle format (improves readability for testing)
4. **FIX-04** - InProceedings support (can start in parallel with FIX-03)

---

## Success Criteria

This sprint is successful when:

1. ✅ Professor reviews output and confirms:
   - RDF Lists are used correctly
   - Unicode characters display properly
   - Turtle output is readable and correct
   - InProceedings modeling is appropriate

2. ✅ All tests pass with 100% success rate

3. ✅ Round-trip conversion still maintains 100% accuracy

4. ✅ No invented BIBO properties in output

5. ✅ Ready to proceed with Sprint 01

---

## Notes

- This sprint takes priority over Sprint 01
- Sprint 01 start date pushed back by 1 week
- These fixes are **architectural** - must be done right
- Don't rush - correctness is more important than speed
- Get professor feedback as soon as FIX-01 is done
- Can parallelize FIX-02 and FIX-03 (independent)
- FIX-04 may extend into Sprint 01 if time runs out

---

**Created:** 2025-11-07
**Status:** READY TO START
**Next Step:** Begin FIX-01 (RDF Lists implementation)
