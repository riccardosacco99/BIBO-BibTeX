# Release Notes

## Version 0.1.0 (Upcoming)

**Release Date:** TBD

This is the initial release of the BIBO-BibTeX Converter, providing bidirectional conversion between BibTeX bibliographic entries and BIBO (Bibliographic Ontology) RDF format.

### Features

#### Core Conversion
- **Bidirectional conversion** between BibTeX and BIBO RDF
- **All 14 standard BibTeX types** supported:
  - @article, @book, @inbook, @incollection
  - @inproceedings, @proceedings, @conference
  - @phdthesis, @mastersthesis
  - @techreport, @manual, @booklet
  - @unpublished, @misc
- **BibTeX.com field conventions** implemented (context-aware field semantics)
- **Round-trip preservation** of bibliographic data

#### Name Parsing
- Advanced BibTeX name parsing following Patashnik's grammar
- Support for name particles (von, van, de, della, etc.)
- Proper handling of suffixes (Jr., Sr., III, etc.)
- Unicode/international name support

#### Identifiers
- DOI, ISBN-10, ISBN-13, ISSN validation with checksums
- Handle and URI support
- URL validation and normalization

#### Date Handling
- Multiple date format support
- Leap year validation
- Historical and circa date support

#### Batch Processing
- Efficient batch conversion (1000+ entries in < 10 seconds)
- Parallel conversion support
- Streaming API for large files
- Progress reporting callbacks
- Conversion statistics and reports

#### VocBench Integration
- RDF4J repository integration
- Native Store persistence
- Transaction support
- Configurable via properties file

### Technical Details

- **Java Version:** 21+
- **RDF4J Version:** 5.1.5
- **JBibTeX Version:** 1.0.18

### Known Limitations

- BIBO → BibTeX conversion may lose some metadata (see LIMITATIONS.md)
- BOOKLET type uses generic bibo:Document IRI
- VocBench UI integration planned for future release

### Dependencies

| Library | Version | License |
|---------|---------|---------|
| RDF4J | 5.1.5 | BSD-3-Clause |
| JBibTeX | 1.0.18 | BSD-3-Clause |
| SLF4J | 2.0.12 | MIT |
| Logback | 1.5.3 | LGPL-2.1 |

### Installation

See [INSTALLATION.md](INSTALLATION.md) for detailed installation instructions.

### Documentation

- [README.md](README.md) - Project overview
- [FIELD_MAPPING.md](FIELD_MAPPING.md) - Complete field mapping reference
- [LIMITATIONS.md](LIMITATIONS.md) - Known limitations and BIBO→BibTeX gaps
- [INSTALLATION.md](INSTALLATION.md) - Installation guide

---

## Future Releases

### Planned for 0.2.0
- VocBench UI integration (import/export dialogs)
- Duplicate detection
- Conflict resolution
- Custom field mapping configuration
- Multi-format RDF export (Turtle, JSON-LD, N-Triples)

### Planned for 1.0.0
- Full VocBench plugin with UI
- Comprehensive integration tests
- Production deployment hardening
- Security audit completion
