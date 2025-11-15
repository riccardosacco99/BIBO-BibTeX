# BIBO-BibTeX Field Mapping Guide

This document describes the mapping decisions between BibTeX entry types/fields and BIBO ontology properties.

## Document Type Mappings

### BibTeX → BIBO Document Types

| BibTeX Entry Type | BIBO Type | Notes |
|-------------------|-----------|-------|
| `@article` | `bibo:Article` | Journal or magazine article |
| `@book` | `bibo:Book` | Published book |
| `@inbook` | `bibo:Chapter` | Section or chapter in a book |
| `@incollection` | `bibo:Chapter` | Chapter in edited collection |
| `@inproceedings` | `bibo:ConferencePaper` | Paper published in conference proceedings |
| `@conference` | `bibo:ConferencePaper` | Legacy alias for `@inproceedings` |
| `@proceedings` | `bibo:Proceedings` | Entire conference proceedings volume |
| `@phdthesis` | `bibo:Thesis` | PhD dissertation |
| `@mastersthesis` | `bibo:Thesis` | Master's thesis |
| `@techreport` | `bibo:Report` | Technical report |
| `@manual` | `bibo:Manual` | Technical manual or documentation |
| `@misc` | `bibo:Document` | Miscellaneous/unclassified |
| `@unpublished` | `bibo:Document` | Unpublished work |

## Field Mappings by Entry Type

### Common Fields (All Types)

| BibTeX Field | BIBO/DC Property | RDF Predicate | Notes |
|--------------|------------------|---------------|-------|
| `title` | Title | `dcterms:title` | Main title |
| `subtitle` | Subtitle | `bibo:subtitle` | Optional subtitle |
| `author` | Authors | `bibo:authorList` | **RDF List** (ordered) |
| `editor` | Editors | `bibo:editorList` | **RDF List** (ordered) |
| `year` | Publication Date | `dcterms:issued` | xsd:gYear |
| `month` | Publication Date | `dcterms:issued` | Combined with year |
| `day` | Publication Date | `dcterms:issued` | Combined with year+month |
| `publisher` | Publisher | `dcterms:publisher` | Literal string |
| `address` | Place of Publication | - | No standard BIBO property (stored as literal) |
| `doi` | DOI Identifier | `bibo:doi` | Digital Object Identifier |
| `isbn` | ISBN Identifier | `bibo:isbn10` or `bibo:isbn13` | Auto-classified by length |
| `issn` | ISSN Identifier | `bibo:issn` | Serial number |
| `url` | URL | - | Web address (literal) |
| `note` | Notes | `rdfs:comment` | Free-text notes |
| `abstract` | Abstract | `dcterms:abstract` | Summary text |
| `language` | Language | `dcterms:language` | ISO language code |
| `keywords` | Keywords | `dcterms:subject` | Multi-value, comma/semicolon separated |
| `series` | Series | `bibo:series` | Book or proceedings series name |
| `edition` | Edition | `bibo:edition` | Edition description (e.g., "3rd", "Second Edition") |

## Context-Dependent Field Conventions (BibTeX.com)

Following [BibTeX.com](https://www.bibtex.com/e/entry-types/) conventions, **some fields have different meanings based on entry type**. The converter implements context-aware resolution:

### Address Field Convention

The `address` field semantics depend on the entry type:

| Entry Type | `address` Meaning | BIBO Mapping | Example |
|------------|-------------------|--------------|---------|
| `@inproceedings` | **Conference location** | Container `dcterms:spatial` | "Berlin, Germany" |
| `@proceedings` | **Conference location** | Container `dcterms:spatial` | "Paris, France" |
| `@book` | **Publisher location** | `dcterms:spatial` | "Reading, MA" |
| `@article` | **Publisher location** | `dcterms:spatial` | "New York, NY" |
| `@phdthesis` | **University location** | `dcterms:spatial` | "Cambridge, MA" |
| `@mastersthesis` | **University location** | `dcterms:spatial` | "Stanford, CA" |
| `@techreport` | **Institution location** | `dcterms:spatial` | "Houston, TX" |

**Implementation:** The converter uses `resolveAddress()` to determine whether `address` should map to:
- `BiboDocument.conferenceLocation()` for conference papers/proceedings
- `BiboDocument.placeOfPublication()` for all other types

### Organization Field Convention

The `organization` field also has context-dependent semantics:

| Entry Type | `organization` Meaning | BIBO Mapping | Example |
|------------|------------------------|--------------|---------|
| `@inproceedings` | **Conference organizer** | Container `bibo:organizer` | "ACM" |
| `@proceedings` | **Conference organizer** | Container `bibo:organizer` | "IEEE" |
| `@manual` | **Publisher** | `dcterms:publisher` | "Free Software Foundation" |

**Implementation:** The converter uses `resolveOrganization()` to map to:
- `BiboDocument.conferenceOrganizer()` for conference entries
- `BiboDocument.publisher()` for manuals (via `inferPublisher()`)

### Type Field Convention (Theses)

For thesis entries, the `type` field specifies the degree type:

| Entry Type | Default Type | Custom Type Field | BIBO Mapping |
|------------|--------------|-------------------|--------------|
| `@phdthesis` | "PhD dissertation" | Overrides default | `bibo:degree` |
| `@mastersthesis` | "Master's thesis" | Overrides default | `bibo:degree` |

**Implementation:** The `resolveDegreeType()` method:
1. Checks for explicit `type` field value
2. Falls back to entry type inference
3. Maps to `BiboDocument.degreeType()`

### Roundtrip Preservation

These conventions are preserved in **BibTeX → BIBO → BibTeX** roundtrips:

- **@inproceedings with address**: `address` → `conferenceLocation` → container spatial → `address`
- **@proceedings with organization**: `organization` → `conferenceOrganizer` → container organizer → `organization`
- **@phdthesis**: Entry type → `degreeType="PhD dissertation"` → `@phdthesis`
- **@mastersthesis**: Entry type → `degreeType="Master's thesis"` → `@mastersthesis`

### Article-Specific Fields

| BibTeX Field | BIBO Property | Notes |
|--------------|---------------|-------|
| `journal` | `dcterms:isPartOf` | Journal name (container title) |
| `volume` | `bibo:volume` | Journal volume number |
| `number` | `bibo:issue` | Issue number within volume |
| `pages` | `bibo:pages` | Page range (e.g., "10--20") |

### InProceedings-Specific Fields

| BibTeX Field | BIBO Property | Modeling Decision |
|--------------|---------------|-------------------|
| `booktitle` | `dcterms:isPartOf` | **Proceedings title** (container) |
| `series` | `bibo:series` | Proceedings series name |
| `volume` | `bibo:volume` | Proceedings volume (if multi-volume) |
| `pages` | `bibo:pages` | Paper page range |
| `organization` | Container `bibo:organizer` | **Conference organizer** (context-aware) |
| `publisher` | `dcterms:publisher` | Proceedings publisher |

**Modeling Note:** `@inproceedings` entries represent a **paper** published in proceedings. The `booktitle` field contains the proceedings title and is mapped to the container relationship. In BIBO, this can be modeled as:
- **Simple approach**: Store booktitle as container title (current implementation)
- **Full reification**: Create separate `bibo:Proceedings` resource and link via `dcterms:isPartOf`

The current implementation uses the simple approach for pragmatism. Full reification can be added in future sprints if needed.

### Proceedings-Specific Fields

| BibTeX Field | BIBO Property | Notes |
|--------------|---------------|-------|
| `title` | `dcterms:title` | Proceedings title |
| `editor` | `bibo:editorList` | Proceedings editors (RDF List) |
| `series` | `bibo:series` | Conference series |
| `volume` | `bibo:volume` | Volume in series |
| `publisher` | `dcterms:publisher` | Publisher |
| `organization` | `bibo:organizer` | **Conference organizer** (context-aware) |
| `isbn` | `bibo:isbn13` | Proceedings ISBN |

### Book-Specific Fields

| BibTeX Field | BIBO Property | Notes |
|--------------|---------------|-------|
| `author` or `editor` | `bibo:authorList` / `bibo:editorList` | Books can have authors or editors |
| `edition` | `bibo:edition` | Edition number/description |
| `series` | `bibo:series` | Book series name |
| `volume` | `bibo:volume` | Volume in series |
| `isbn` | `bibo:isbn10` or `bibo:isbn13` | Book identifier |

### Thesis-Specific Fields

| BibTeX Field | BIBO Property | Alternative Field |
|--------------|---------------|-------------------|
| `school` | `dcterms:publisher` | Granting institution |
| `type` | - | Thesis type (PhD, Master's, etc.) - encoded in document type |
| `address` | - | Institution location |

### TechReport-Specific Fields

| BibTeX Field | BIBO Property | Alternative Field |
|--------------|---------------|-------------------|
| `institution` | `dcterms:publisher` | Issuing institution |
| `number` | `bibo:number` | Report number |
| `type` | - | Report type |

## Special Handling

### Author/Editor Ordering (Sprint 00 - FIX-01)

**Problem:** BibTeX maintains author order, and citation styles depend on it (first author vs. "et al.").

**Solution:** Use **RDF Lists** (W3C standard for ordered collections):

```turtle
:document a bibo:Article ;
    bibo:authorList (
        [ a foaf:Person ; foaf:name "First, Alice" ]
        [ a foaf:Person ; foaf:name "Second, Bob" ]
        [ a foaf:Person ; foaf:name "Third, Carol" ]
    ) .
```

**RDF Structure:**
- Uses `rdf:first`, `rdf:rest`, `rdf:nil` predicates
- Ordering preserved through serialization
- Properly round-trips: BibTeX → BIBO → BibTeX

**Note:** This project does **NOT** use `bibo:sequence` (invented property, not part of BIBO ontology).

### Unicode Handling (Sprint 00 - FIX-02)

BibTeX uses LaTeX escape sequences for special characters. These are converted to Unicode:

| BibTeX Escape | Unicode | Character Name |
|---------------|---------|----------------|
| `{\\'e}` | é | e with acute accent |
| `{\\`a}` | à | a with grave accent |
| `{\\\"u}` | ü | u with umlaut |
| `{\\^o}` | ô | o with circumflex |
| `{\\~n}` | ñ | n with tilde |
| `{\\c{c}}` | ç | c with cedilla |
| `{\\aa}` | å | a with ring above |
| `{\\o}` | ø | o with stroke |
| `{\\ss}` | ß | German sharp s |
| `{\\ae}` | æ | ae ligature |

**Full list:** 100+ mappings in `BibTeXUnicodeConverter.java`

**Conversion occurs:**
- **Import (BibTeX → BIBO):** Escape sequences → Unicode (automatic)
- **Export (BIBO → BibTeX):** Unicode → Escape sequences (via `fromUnicode()`)

### Multiple Values

Some BibTeX fields can contain multiple values:

| Field | Separator | Example | Handling |
|-------|-----------|---------|----------|
| `author` | ` and ` | `Smith, A and Doe, B` | Split into list of contributors |
| `editor` | ` and ` | `Editor, E and Editor, F` | Split into list of editors |
| `isbn` | `,` or `;` | `978-1-234,978-5-678` | Multiple identifier objects |
| `issn` | `,` or `;` | `1234-5678,9876-5432` | Multiple identifier objects |

### Container Relationships

BibTeX uses different fields for containers depending on type:

| Entry Type | Container Field | Maps To |
|------------|-----------------|---------|
| `@article` | `journal` | `dcterms:isPartOf` (Journal) |
| `@inproceedings` | `booktitle` | `dcterms:isPartOf` (Proceedings) |
| `@incollection` | `booktitle` | `dcterms:isPartOf` (Book) |
| `@inbook` | `booktitle` | `dcterms:isPartOf` (Book) |

**Stored as:** Container title string in BIBO (simple approach).

**Future enhancement:** Could create separate resources for journals/proceedings and link via IRI.

### Publisher Field Variations

Different entry types use different fields for publisher:

| Entry Type | BibTeX Field | BIBO Property |
|------------|--------------|---------------|
| `@book` | `publisher` | `dcterms:publisher` |
| `@article` | `publisher` (rare) | `dcterms:publisher` |
| `@inproceedings` | `publisher` | `dcterms:publisher` |
| `@phdthesis` | `school` | `dcterms:publisher` |
| `@mastersthesis` | `school` | `dcterms:publisher` |
| `@techreport` | `institution` | `dcterms:publisher` |

**Converter handles this automatically** via `fieldForPublisher()` method.

## BibTeX.com Field Conventions (Sprint 02 - US-24)

### Context-Dependent Field Semantics

**Important:** BibTeX field meanings are **context-dependent** on entry type. The same field name can have different semantic meanings depending on the document type. This follows conventions documented at [BibTeX.com](https://www.bibtex.com/).

The converter implements these conventions to ensure semantically correct mappings.

### Address Field Context

The `address` field has **different meanings** in different entry types:

| Entry Type | Semantic Meaning | BIBO Mapping | Example |
|------------|------------------|--------------|---------|
| `@book` | **Publisher's location** | `dcterms:spatial` (publisher address) | `address = {New York, NY}` |
| `@inproceedings` | **Conference location** | `dcterms:spatial` (conference venue) | `address = {Berlin, Germany}` |
| `@proceedings` | **Conference location** | `dcterms:spatial` (conference venue) | `address = {Paris, France}` |
| `@manual` | **Organization location** | `dcterms:spatial` (publisher address) | `address = {Cambridge, MA}` |
| `@phdthesis` | **University location** | `dcterms:spatial` (institution address) | `address = {Stanford, CA}` |
| `@mastersthesis` | **University location** | `dcterms:spatial` (institution address) | `address = {Oxford, UK}` |
| `@techreport` | **Institution location** | `dcterms:spatial` (institution address) | `address = {Berkeley, CA}` |

**Implementation Note:** The converter applies these semantics when converting BIBO→BibTeX, selecting the appropriate address field based on document type.

**Example:**
```turtle
# BIBO Conference Paper
:paper a bibo:ConferencePaper ;
    dcterms:isPartOf :conference .

:conference a bibo:Conference ;
    dcterms:spatial "Berlin, Germany" .
```
↓ converts to ↓
```bibtex
@inproceedings{paper,
  address = {Berlin, Germany}  % Conference location (NOT publisher)
}
```

### Organization Field Context

The `organization` field semantics vary by entry type:

| Entry Type | Semantic Meaning | BIBO Mapping | Example |
|------------|------------------|--------------|---------|
| `@manual` | **Publishing organization** | `dcterms:publisher` | `organization = {GNU Project}` |
| `@proceedings` | **Conference sponsor** (optional) | `bibo:organizer` or `note` | `organization = {ACM}` |
| `@inproceedings` | **Conference sponsor** (rare) | `bibo:organizer` or `note` | `organization = {IEEE}` |

**Implementation Note:** For `@manual`, `organization` is the primary publisher field (preferred over `publisher`). For conference types, it represents sponsoring organizations.

**Example:**
```bibtex
@manual{gnu_make,
  title = {GNU Make Manual},
  organization = {Free Software Foundation},
  address = {Boston, MA},
  year = {2024}
}
```
↓ converts to ↓
```turtle
:gnu_make a bibo:Manual ;
    dcterms:title "GNU Make Manual" ;
    dcterms:publisher "Free Software Foundation" ;
    dcterms:spatial "Boston, MA" ;
    dcterms:issued "2024"^^xsd:gYear .
```

### Institution Field Context

The `institution` field appears only in specific entry types:

| Entry Type | Semantic Meaning | BIBO Mapping | Example |
|------------|------------------|--------------|---------|
| `@techreport` | **Issuing institution** | `dcterms:publisher` | `institution = {MIT Computer Science Lab}` |
| `@phdthesis` | Not used | N/A | Use `school` instead |
| `@mastersthesis` | Not used | N/A | Use `school` instead |

**Note:** For theses, use `school` field, not `institution`.

### Type Field Context

The `type` field provides additional type information:

| Entry Type | Semantic Meaning | BIBO Handling | Example |
|------------|------------------|---------------|---------|
| `@phdthesis` | Thesis type override | Encoded in `rdf:type` | `type = {PhD dissertation}` |
| `@mastersthesis` | Thesis type override | Encoded in `rdf:type` | `type = {Master's thesis}` |
| `@techreport` | Report type | `note` field | `type = {Technical Report}` |
| `@inbook` | Chapter/section type | `note` field | `type = {Chapter}` |

**Default Values:**
- `@phdthesis`: `type = {PhD thesis}` (implied)
- `@mastersthesis`: `type = {Master's thesis}` (implied)

### School Field Context

The `school` field is specific to thesis entry types:

| Entry Type | Semantic Meaning | BIBO Mapping | Example |
|------------|------------------|--------------|---------|
| `@phdthesis` | **Granting university** | `dcterms:publisher` | `school = {Stanford University}` |
| `@mastersthesis` | **Granting university** | `dcterms:publisher` | `school = {MIT}` |

**Implementation Note:** `school` is the **preferred** field for theses. If `publisher` also appears, `school` takes precedence.

### Booktitle Field Context

The `booktitle` field represents different container types:

| Entry Type | Container Type | BIBO Mapping | Example |
|------------|----------------|--------------|---------|
| `@inproceedings` | **Proceedings title** | `dcterms:isPartOf` (Proceedings) | `booktitle = {Proc. ICML 2024}` |
| `@incollection` | **Anthology/book title** | `dcterms:isPartOf` (Book) | `booktitle = {Handbook of AI}` |
| `@inbook` | **Book title** | `dcterms:isPartOf` (Book) | `booktitle = {The RDF Primer}` |

**Note:** `@article` uses `journal` instead of `booktitle`.

### Convention Summary Table

Quick reference for context-dependent field mappings:

| Field | @book | @inproceedings | @proceedings | @manual | @thesis | @techreport |
|-------|-------|----------------|--------------|---------|---------|-------------|
| `address` | Publisher loc | Conference loc | Conference loc | Org loc | University loc | Institution loc |
| `publisher` | Publisher | Proc publisher | Publisher | (optional) | (optional) | (optional) |
| `organization` | N/A | Sponsor | Sponsor | **Publisher** | N/A | N/A |
| `institution` | N/A | N/A | N/A | N/A | N/A | **Publisher** |
| `school` | N/A | N/A | N/A | N/A | **Publisher** | N/A |
| `booktitle` | N/A | Proceedings | N/A | N/A | N/A | N/A |
| `journal` | N/A | N/A | N/A | N/A | N/A | N/A |

**Legend:**
- **Bold** = Primary/preferred field for publisher information
- N/A = Not applicable for this entry type

### Implementation Status

- ✅ **Sprint 00-01:** Basic field mappings implemented
- 🔄 **Sprint 02 (US-24):** Context-aware semantics implementation in progress
- 📋 **Future:** Full validation against BibTeX.com reference documentation

## Round-Trip Considerations

### Lossy Conversions

Some BibTeX fields don't have direct BIBO equivalents:

**Fields that are lost:**
- `crossref` (removed in Sprint 00 - problematic)
- `key` (sorting key - not semantically meaningful)
- `howpublished` (publication method - no BIBO equivalent)
- `organization` (sponsoring organization - no standard BIBO property)

**Fields stored as literals (partial semantic loss):**
- `address` → Stored via `dcterms:spatial` but not semantically typed as location

**Fields fully preserved (Sprint 01):**
- `series` → `bibo:series` (bidirectional)
- `edition` → `bibo:edition` (bidirectional)
- `keywords` → `dcterms:subject` (multi-value, bidirectional)
- `note` → `rdfs:comment` (bidirectional)

**For comprehensive gap analysis:** See [docs/LIMITATIONS.md](docs/LIMITATIONS.md) for detailed information loss scenarios, heuristic strategies, and BIBO vs BibTeX comparison tables.

### Citation Key Generation

When exporting BIBO → BibTeX, if no ID is present:

```
citation_key = sanitize(first_author_family_name + year + title_words)
```

Example: `Smith2024ArtificialIntelligence`

Sanitization removes:
- Diacritics: é → e, ü → u
- Non-alphanumeric characters (except `-`)
- Spaces

## Compliance with Standards

### BIBO Ontology

- **Namespace:** `http://purl.org/ontology/bibo/`
- **Version:** Compatible with BIBO 1.3
- **Properties used:** Only standard BIBO/Dublin Core/FOAF properties
- **No custom properties** (Sprint 00 removed `bibo:sequence`)

### RDF Standards

- **RDF Lists:** W3C RDF 1.1 specification
- **Serialization:** Turtle (default), RDF/XML, JSON-LD
- **Vocabularies:** BIBO, Dublin Core Terms, FOAF, RDF, RDFS

### BibTeX Compatibility

- **Parser:** JBibTeX 1.0.18
- **Character encoding:** UTF-8 (with LaTeX escape support)
- **Entry types:** All standard BibTeX types supported
- **Extensions:** Compatible with BibLaTeX extensions

## Future Enhancements

Potential improvements for future sprints:

1. **Full Proceedings Reification:** Create separate `bibo:Proceedings` resources for `@inproceedings` entries
2. **Journal Resources:** Model journals as separate entities with ISSN
3. **Person Entities:** Create `foaf:Person` resources with ORCIDs
4. **Controlled Vocabularies:** Use standard subject classifications (ACM, MSC, etc.)
5. **Citation Links:** Model `cites`/`citedBy` relationships
6. **Version Control:** Track document versions and revisions

---

**Document Version:** Sprint 02 (US-24 Conventions Added)
**Last Updated:** 2025-11-15
**Status:** Extended fields and BibTeX.com conventions documented
