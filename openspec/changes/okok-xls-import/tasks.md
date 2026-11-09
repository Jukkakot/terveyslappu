# Tasks

## 1. Reader

- [ ] 1.1 Pure binary Excel reader: compound-document container (sectors, FAT, directory, the
  `Workbook` stream) and BIFF8 records for the first sheet (SST, LABELSST, LABEL, NUMBER, RK,
  CONTINUE) into a table of strings. Verify: unit test on a small made-up `.xls` fixture made with a
  spreadsheet tool (never the user's file, NFR-1), plus a damaged-file test.
- [ ] 1.2 OKOK layout: skip title rows, header columns, year from the range line with the new-year
  rollover, values as text; feed the existing parser's rows so preview, duplicates and log stay
  the same. Verify: unit tests for the scenarios (made-up dates and weights).

## 2. Picker

- [ ] 2.1 Import accepts `application/vnd.ms-excel` and `.xls`; content sniffing (compound-document
  signature) picks the reader, so a text file named `.xls` still reads as text; `import.read` logs
  `format=xls`. Verify: `./gradlew test lint assembleDebug` passes.

## 3. Context

- [ ] 3.1 Close Q2 in questions.md with the format found; product.md note on the OKOK layout
  (generic, no real values). Verify: files read correctly.
