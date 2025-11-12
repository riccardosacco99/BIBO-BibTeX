# Known Limitations

- Citation keys are regenerated when exporting from BIBO to BibTeX: if a BIBO document does not carry a valid BibTeX key, the converter derives one from the title/authors and the original identifier is not preserved.
- Cross-references (`crossref`) and nested BibTeX dependencies are ignored during conversion; entries must be fully expanded before invoking the converter.
- Organization-level metadata such as the `organization` field on `@inproceedings` is currently dropped because there is no canonical BIBO target yet; the round-trip tests document this lossy mapping.
- Informal publication hints such as `howpublished` on `@misc` are also omitted: the BIBO model does not capture them, so they cannot be reconstructed when exporting back to BibTeX.
