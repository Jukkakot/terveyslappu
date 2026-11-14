# Terveyslappu

Pieni Android-sovellus painon kirjaamiseen mahdollisimman vähällä vaivalla.

Napauta kotinäytön widgetiä tai pika-asetusten ruutua, astu Bluetooth-vaa'alle, ja paino tallentuu
Health Connectiin ilman että sovellusta tarvitsee avata. Sovelluksessa näet viimeisimmän painon,
kehityksen kaaviona ja historian, ja painon voi lisätä myös käsin tai tuoda vanhasta tiedostosta.

Kevyt ruokakirjaus kulkee mukana: lisää syömäsi annos parilla napautuksella, niin sovellus oppii
tavalliset ruokasi ja näyttää päivän kalorit sekä herkkujen osuuden painon rinnalla.

## Periaatteet

- **Health Connect on varasto.** Painot ja ruokakirjaukset menevät puhelimen omaan Health Connectiin, josta muut
  terveyssovellukset näkevät ne. Sovelluksella ei ole omaa palvelinta, eikä mitään lähetetä
  minnekään.
- **Vähän napautuksia.** Punnitus onnistuu yhdellä napautuksella ja vaa'alle astumalla.
- **Oma käyttö edellä.** Tämä on harrastusprojekti yhden ihmisen tarpeisiin, ja se kasvaa pala
  kerrallaan.

## Asennus

Sovellusta ei ole Play-kaupassa. Asenna se [Obtainiumilla](https://obtainium.imranr.dev/):
Add app → `https://github.com/Jukkakot/terveyslappu`. Jokainen päivitys julkaistaan tämän repon
Releases-sivulle, ja Obtainium tarjoaa sen automaattisesti.

Tarvitset Android-puhelimen, jossa on Health Connect, ja punnitukseen Bluetooth-vaa'an, joka
lähettää painon mainospaketeissa (testattu yhdellä edullisella mallilla).

## Tekniikka

Kotlin, Jetpack Compose ja Material 3, Health Connect, Vico-kaaviot. GitHub Actions rakentaa ja
allekirjoittaa APK:n jokaisesta muutoksesta.
