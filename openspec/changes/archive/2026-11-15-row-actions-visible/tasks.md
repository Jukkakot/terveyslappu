## 1. Visible row actions

- [x] 1.1 Ruoka (`ui/FoodScreen.FoodRow`): pencil (opens the item editor) and red trash (deletes with the existing Kumoa flow) at the row's end after kcal; hidden in Järjestä mode; row tap still opens the editor
- [x] 1.2 Ruokakirjaus (`ui/ProductsPage`): pencil (product editor) and red trash (same removal + Kumoa as Poista tuote)
- [x] 1.3 AI preview (`ui/AiImportPreview.PreviewRow`): pencil before the existing trash
- [x] 1.4 Reuse the sheet's icon button look (`ic_edit`, trash in `colorScheme.error`, 48 dp); content descriptions in strings
- [x] 1.5 Ohje (`help_food_body`): mention the pencil and trash on Ruoka rows in a few words
- [x] 1.6 One smoke test per screen that the buttons exist and act (Ruoka delete shows Kumoa); bump `appVersion` to 1.37; `./gradlew test lint assembleDebug`
