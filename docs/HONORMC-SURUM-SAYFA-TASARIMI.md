# HonorMC Surum Sayfasi Tasarimi

Bu dokuman, GitHub release notlarinda kullanilan sayfa formatini tarif eder.

## 1) Genel Dusunce

- Baslik: `HonorMC <surum>`
- Alt baslikta hangi versiyon/etiket araligi kaldigini göster:
  - ornek: `Indirme Secenekleri (Honor-26.1..Honor-26.2)`

## 2) Indirme Secenekleri

Ilk blok asagidaki gibi olmalidir:

- `Jar + Baslatma BAT`
- `Tam Dagitim (ZIP)`
- Her iki secenek de dogrudan asset linki icermeli

## 3) Degisiklik Ozeti

Ne geldigini acikca gozukmesi icin 3 bolum:

- `## + Ne Geldi`
- `## - Ne Gitti`
- `## ~ Ne Duzenlendi`
- `## Gecis Karsilastirmasi`

## 4) Paket Notu

Son blokta birden fazla dosyanin verildigi net sekilde yazilir.

## Ornek Release Body

```text
# HonorMC 26.2

## Indirme Secenekleri (Honor-26.1..Honor-26.2)

### 1) Sadece jar + baslatma isteyenler
- [Jar](https://github.com/HonorMC-TR/honor-sunucucekirdek/releases/download/Honor-26.2/Honor-26.2.jar) (`Honor-26.2.jar`)
- [Baslatma BAT](https://github.com/HonorMC-TR/honor-sunucucekirdek/releases/download/Honor-26.2/Honor-26.2-baslat.bat) (`Honor-26.2-baslat.bat`)

### 2) Tum kurulum paketini isteyenler
- [ZIP](https://github.com/HonorMC-TR/honor-sunucucekirdek/releases/download/Honor-26.2/Honor-26.2-dagitim.zip) (`Honor-26.2-dagitim.zip`)

## + Ne Geldi
- + [abc12345](https://github.com/HonorMC-TR/honor-sunucucekirdek/commit/abc12345) Yeni konsol mesajlari Turkceye cevrildi.

## - Ne Gitti
- - [def67890](https://github.com/HonorMC-TR/honor-sunucucekirdek/commit/def67890) Gereksiz yabanci log sabitleri kaldirildi.

## ~ Ne Duzenlendi
- ~ [hij98765](https://github.com/HonorMC-TR/honor-sunucucekirdek/commit/hij98765) Release cikti formati daha okunur hale getirildi.

## Gecis Karsilastirmasi
- Karsilastirma: https://github.com/HonorMC-TR/honor-sunucucekirdek/compare/Honor-26.1...Honor-26.2
- Commit Sayisi: 12
- + Ne Geldi sayisi: 3
- - Ne Gitti sayisi: 1
- ~ Ne Duzenlendi sayisi: 8

## Paket Notu
- JAR dosyasi: Honor-26.2.jar
- Baslatma BAT: Honor-26.2-baslat.bat
- Dagitim ZIP: Honor-26.2-dagitim.zip
```

## Not

Her satir + / - / ~ on ekiyle baslamali. `-` sadece silinen/çıkarılan davranışı, `+` yeni ozellikleri, `~` ise duzenleme/stabilite değişikliklerini temsil eder.
