# HonorMC Temiz Konsol

HonorMC'nin temiz konsol hedefi baslaticida log saklamak degil, gorunen metinleri cekirdekte Turkcelestirmek ve gereksiz gurultuyu kaynakta azaltmaktir.

Bu asamada cekirdege tasinan yuzeyler:

- Bootstrap Java ve cekirdek yukleme mesajlari.
- Eklenti baslatma ve eklenti listeleme mesajlari.
- Dunya tasima uyarilari.
- Legacy Material uyumluluk uyarisi.
- Moonrise is parcacigi loglari.
- `/plugins` ve `/version` komutlarinin kullaniciya gorunen metinleri.
- Paketlenen `Honor-<surum>.jar` icindeki Paperclip baslatma satiri.
- Terminal konsolunda seviye bazli renkli HonorMC log duzeni.

Baslaticilar sadece JVM'i dogru argumanlarla calistirir. `BASLAT.ps1` ve `BASLAT.bat` cikti satirlarini cevirmez veya saklamaz.

## JVM Uyari Azaltma

Java 26 gibi yeni runtime'larda bazi native/Unsafe uyarilari JVM tarafindan uretilebilir. HonorMC baslaticilari bunlari azaltmak icin su argumanlari verir:

```text
--enable-native-access=ALL-UNNAMED
--illegal-native-access=allow
--sun-misc-unsafe-memory-access=allow
```

Bu argumanlar metin filtresi degildir; JVM'in izin politikasini acikca ayarlar.

## Renkli Konsol

HonorMC terminal konsolu seviye etiketlerini Turkce ve renkli gosterir:

- `BILGI`: yesil.
- `UYARI`: sari ve kalin.
- `HATA`: kirmizi ve kalin.
- `KRITIK`: parlak kirmizi ve kalin.
- `DEBUG` / `TRACE`: daha sakin teknik renkler.

Saat bolumu koyu gri, kaynak/logger bolumu mavi gosterilir. `logs/latest.log` gibi dosya loglarinda ANSI renk kodu yazilmaz; dosyalar panel ve araclar icin temiz tutulur.

## Sonraki Cekirdek Hedefleri

- Minecraft kaynaklarindan gelen datapack, recipe, advancement ve environment mesajlarini ayri patchlerle Turkcelestirmek.
- `honormc.yml` uzerinden cekirdek icinde `sade`, `normal`, `detayli` log profilleri saglamak.
- Panel kullanicilari icin ayni temiz konsol davranisini baslaticiya bagimli olmadan sunmak.
