# HonorMC Surum Sayfasi Tasarimi

Bu dokuman, GitHub Releases ve Tags sayfalarinda gorulecek formatlari tarif eder.

## Hedef

- Release sayfasi: her tagin su sayfada "Ne Geldi / Ne Gitti / Ne Duzenlendi" olarak gormesi.
- Tag sayfasi: tag notlari ayni formatta olur ve kisaca surum ozeti gorulur.

## 1) Release Sayfasi Bloklari

- `## Indirme Secenekleri (...)`
- `## + Ne Geldi`
- `## - Ne Gitti`
- `## ~ Ne Duzenlendi`
- `## Gecis Karsilastirmasi`
- `## Paket Notu`

## 2) Tag Sayfasi Tasarimi

Tag listesinde asagidaki gorunum hedeflenir:

- Tag ismi: `Honor-26.2`
- Tag mesaji: surumun ozeti (release formatindan alinmis)
- Linkler: release sayfasina yonlendirir
- Bu sekilde hem `releases` hem `tags` sayfasi ayni bilgiyi tuketen sunar.

## Ornek Release + Tag Bloklari

```text
# HonorMC 26.2

## Indirme Secenekleri (Honor-26.1 .. Honor-26.2)

### 1) Sadece jar + baslatma isteyenlere
- [Jar](https://github.com/HonorMC-TR/honor-sunucucekirdek/releases/download/Honor-26.2/Honor-26.2.jar) (`Honor-26.2.jar`)
- [Baslatma BAT](https://github.com/HonorMC-TR/honor-sunucucekirdek/releases/download/Honor-26.2/Honor-26.2-baslat.bat) (`Honor-26.2-baslat.bat`)

### 2) Tum kurulum paketini isteyenlere
- [ZIP](https://github.com/HonorMC-TR/honor-sunucucekirdek/releases/download/Honor-26.2/Honor-26.2-dagitim.zip) (`Honor-26.2-dagitim.zip`)

## + Ne Geldi
- + Yeni Turkce konsol mesajlari eklendi.

## - Ne Gitti
- - Eski yabanci log sabitleri azaltildi.

## ~ Ne Duzenlendi
- ~ Sürüm sayfasi ve karşılaştırma bloklari genisletildi.

## Gecis Karsilastirmasi
- Karsilastirma: https://github.com/HonorMC-TR/honor-sunucucekirdek/compare/Honor-26.1...Honor-26.2
- Commit Sayisi: 3
- + Ne Geldi sayisi: 1
- - Ne Gitti sayisi: 1
- ~ Ne Duzenlendi sayisi: 1

## Paket Notu
- JAR dosyasi: Honor-26.2.jar
- Baslatma BAT: Honor-26.2-baslat.bat
- Tam Dagitim: Honor-26.2-dagitim.zip
```

## Not

- Satirlar su sekilde baslar: `+` (yeni ozellik), `-` (cikarilan), `~` (duzenleme)
- Tag basligi ayni formatta tutulur, bu da Tags sayfasinda da surum ozeti okunur hale getirir.
