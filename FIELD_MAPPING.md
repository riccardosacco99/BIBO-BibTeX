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
| `note` | Notes | - | Free-text notes |
| `abstract` | Abstract | `dcterms:abstract` | Summary text |
| `language` | Language | `dcterms:language` | ISO language code |

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
| `series` | - | Proceedings series name |
| `volume` | `bibo:volume` | Proceedings volume (if multi-volume) |
| `pages` | `bibo:pages` | Paper page range |
| `organization` | - | Sponsoring organization |
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
| `series` | - | Conference series |
| `volume` | `bibo:volume` | Volume in series |
| `publisher` | `dcterms:publisher` | Publisher |
| `organization` | - | Organizing body |
| `isbn` | `bibo:isbn13` | Proceedings ISBN |

### Book-Specific Fields

| BibTeX Field | BIBO Property | Notes |
|--------------|---------------|-------|
| `author` or `editor` | `bibo:authorList` / `bibo:editorList` | Books can have authors or editors |
| `edition` | `bibo:edition` | Edition number/description |
| `series` | - | Book series name |
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

## Round-Trip Considerations

### Lossy Conversions

Some BibTeX fields don't have direct BIBO equivalents:

**Fields that may be lost:**
- `crossref` (removed in Sprint 00 - problematic)
- `key` (sorting key)
- `howpublished` (publication method)
- `organization` (no standard BIBO property)
- `series` (no standard BIBO property)

**Fields stored as notes/literals:**
- `address` → Stored but not semantically typed
- `note` → Preserved as free text

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

**Document Version:** Sprint 00 Complete
**Last Updated:** November 2025
**Status:** Reflects current implementation
