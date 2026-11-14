## MODIFIED Requirements

### Requirement: Ruoka day screen
The app SHALL have a Ruoka screen for one day, opening on today, with arrows to earlier days (none
after today). It SHALL list the day's items in eating order, each with its name, amount and kcal,
and, for an item logged on the day itself, its logging time in small muted text. On top it SHALL
show the day's total kcal with the treats' share apart, and the number of items without kcal
("1 760 kcal · herkkuja 230 · 1 ilman kcal"). A day with nothing logged SHALL show only a short
line on how to add, without any warning, count or judgement. The screen SHALL show no budget,
target, remaining calories, streak or meal names. The top bar SHALL have a "Viikko" link that opens
Historia on Energia for 1 vk; back from there SHALL return to the Ruoka screen.

#### Scenario: A day in order
- **GIVEN** today the user logged Rahka 200 g at 7.10, Pullo 1 kpl at 10.05 and Pizza (no kcal) at 12.30
- **WHEN** the Ruoka screen opens
- **THEN** the three items are listed in that order with their times, and the top reads the total
  of Rahka and Pullo and "1 ilman kcal"

#### Scenario: Missed day
- **WHEN** the user moves to a day with nothing logged
- **THEN** only the how-to-add line is shown

#### Scenario: From the day to the week
- **WHEN** the user taps Viikko on the Ruoka screen
- **THEN** Historia opens on Energia with 1 vk, and tapping a day's bar there opens that day on Ruoka

### Requirement: Food from an AI chat
The app SHALL take a day's food as JSON text, whatever app wrote it, in two ways: "Tuo
ruoka-annokset" in the Lisää annos sheet reads the clipboard, and sharing text from another app
(such as an AI chat) to the app opens the same preview, titled "Tuo ruoka-annokset". The JSON SHALL
be found also inside a code block or surrounded by other text. Text with no usable JSON SHALL show
a short reason and add nothing. The import's buttons, titles and messages SHALL NOT name an AI as
the source.

#### Scenario: Shared from the chat
- **GIVEN** the AI chat's answer holds a code block with items
- **WHEN** the user shares the answer to the app
- **THEN** the app opens the preview of those items

#### Scenario: Pasted
- **WHEN** the user copies the answer and taps Tuo ruoka-annokset
- **THEN** the same preview opens

#### Scenario: No JSON
- **GIVEN** the text holds only chat text
- **THEN** a short reason is shown and nothing is added

### Requirement: Clipboard offer
When the front page, the Ruoka screen or the Lisää annos sheet opens or the app resumes on one of
them, and the clipboard holds text copied after the last offer, the app SHALL read it once, and
when it holds usable food JSON SHALL show a line "Leikepöydällä ruoka-annoksia: <N> · Näytä ·
Ohita" (on the front page above the cards, with the other notices). Näytä SHALL open the preview;
Ohita SHALL hide the line. The same clipboard content SHALL be offered only once, whatever the
answer and on whichever screen, and clipboard text without food JSON SHALL show nothing. The app
SHALL NOT read the clipboard anywhere else or in the background.

#### Scenario: Copied in the chat
- **GIVEN** the user copied the chat's answer with food JSON
- **WHEN** they open the Ruoka screen
- **THEN** the line offers the 6 portions, and Näytä opens the preview

#### Scenario: Offered on the front page
- **GIVEN** the user copied food JSON with 4 items
- **WHEN** they open the app on the front page
- **THEN** the line "Leikepöydällä ruoka-annoksia: 4" is shown above the cards, and Näytä opens the preview

#### Scenario: Offered once
- **GIVEN** the user tapped Ohita on the offer
- **WHEN** they open the Lisää annos sheet without copying anything new
- **THEN** no offer is shown

#### Scenario: Other text
- **GIVEN** the clipboard holds a web address
- **WHEN** the Ruoka screen opens
- **THEN** no offer is shown

### Requirement: AI food JSON shape
The JSON SHALL be an object with an `items` list (what was eaten) and/or a `products` list
(products to keep), or a bare list of items. An item SHALL
have `name` and MAY have `amount` (> 0), `unit` (`g` or `kpl`, default `g` with an amount), `kcal`
(the item's total, >= 0) and `herkku` (true/false). A product SHALL have `name` and `kcal` and MAY
have `per` (`100g`, `kpl` or `annos`; `annos` is kept as a piece), `amount`/`unit` (its usual
amount) and `herkku`. Unknown keys (also a date) SHALL be ignored; an item or product without a name or with
unusable numbers SHALL be skipped and counted in the preview. "Kopioi tuontiohje" in the
Lisää annos sheet SHALL copy the Finnish instruction for the writer of the JSON (such as an AI
chat), which states this shape, the purpose of every field and an example. The instruction SHALL
ask a product's energy for the dish alone, without side dishes, so the product can be logged later
with other sides.

#### Scenario: Items and products
- **GIVEN** the text holds
  `{"items":[{"name":"Kaurapuuro","amount":300,"unit":"g","kcal":210}],"products":[{"name":"Lohikeitto","kcal":380,"per":"annos"}]}`
- **THEN** the preview shows Kaurapuuro 300 g 210 kcal and the product Lohikeitto 380 kcal / kpl

#### Scenario: Item without kcal
- **WHEN** an item `{"name":"Kahvi"}` is added
- **THEN** Kahvi is logged without kcal and counted as "1 ilman kcal"
