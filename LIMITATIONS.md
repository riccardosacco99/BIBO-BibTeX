# Known Limitations (Quick Reference)

**For comprehensive gap analysis, see:** [docs/LIMITATIONS.md](docs/LIMITATIONS.md)

## Quick Summary

The conversion between BIBO and BibTeX is **inherently lossy** in certain directions due to fundamental differences in expressiveness:

- **BibTeX → BIBO**: Generally lossless (BIBO is more expressive)
- **BIBO → BibTeX**: Lossy (BibTeX cannot represent rich semantic metadata)

## Main Limitations

- **Citation keys**: Regenerated when exporting from BIBO to BibTeX if not explicitly stored. Derived from title/authors/year.
- **Cross-references** (`crossref`): Ignored during conversion. Entries must be fully expanded.
- **Organization metadata**: `organization` field on `@inproceedings` may be lost (no canonical BIBO mapping).
- **Informal fields**: `howpublished` on `@misc` omitted (BIBO doesn't model this).
- **Author affiliations**: BIBO supports rich affiliation metadata, BibTeX only stores names.
- **Multiple identifiers**: BIBO supports multiple typed identifiers (DOI, ISBN, ISSN, PMID, arXiv). BibTeX has limited identifier fields.
- **Structured dates**: BIBO uses ISO 8601 full dates. BibTeX only has `year` and `month`.
- **Conference metadata**: BIBO models conferences as separate entities with organizers, location, series. BibTeX flattens to `booktitle` and `address`.

## Detailed Documentation

For comprehensive analysis including:
- 5 detailed information loss scenarios with RDF/BibTeX examples
- Heuristic mapping strategies
- BIBO vs BibTeX comparison tables
- Best practices for users and developers
- Thesis material (Italian)

**See:** [docs/LIMITATIONS.md](docs/LIMITATIONS.md)
