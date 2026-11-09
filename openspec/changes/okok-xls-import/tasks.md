# Tasks

## 1. Reader

- [x] 1.1 Add Apache POI (`org.apache.poi:poi`, latest stable; HSSF only) and read the first sheet
  of an `.xls` into a table of strings (cells as displayed). Check it runs on Android (min SDK 34,
  R8/lint, APK size noted). Verify: unit test on a small made-up `.xls` fixture made with a
  spreadsheet tool or a script (never the user's file, NFR-1), plus a damaged-file test.
- [x] 1.2 OKOK layout: skip title rows, header columns, year from the range line with the new-year
  rollover, values as text; feed the existing parser's rows so preview, duplicates and log stay
  the same; every other column and the profile go to the raw-data archive with source `okok`. Verify: unit tests for the scenarios (made-up dates and weights).

## 2. Picker

- [x] 2.1 Import accepts `application/vnd.ms-excel` and `.xls`; content sniffing (compound-document
  signature) picks the reader, so a text file named `.xls` still reads as text; `import.read` logs
  `format=xls`. Verify: `./gradlew test lint assembleDebug` passes.

## 3. Context

- [x] 3.1 Close Q2 in questions.md with the format found; product.md note on the OKOK layout
  (generic, no real values). Verify: files read correctly.
