# HonorMC Proje Sayfasi

HonorMC, Minecraft sunuculari icin tamamen Turkce odakli, performansli ve temiz bir cekirdek/ekosistem projesidir.

- Kod adi: **HonorMC**
- Dagitim dosyasi: **Honor-<surum>.jar**
- Dagitim paketi: **Honor-<surum>-dagitim.zip**
- Dagitim ve kurulum aciklayicisi: https://github.com/HonorMC-TR/honor-sunucucekirdek/releases/latest
- Komut kontrolu: `/version` ve `/surum` komutlarinda proje kimligi ve repo bilgisi gorulur.

## Indirme Secenekleri

- `Honor-<surum>.jar`  (sunucu jar'i)
- `Honor-<surum>-baslat.bat` (hafif baslatma scripti)
- `Honor-<surum>-dagitim.zip` (tam dagitim paketi: jar + ayar + baslatici)

## Surum notu sayfa duzeni

Release sayfasinda asagidaki bölümler kullanilir:

- `## Indirme Secenekleri`
- `## + Ne Geldi`
- `## - Ne Gitti`
- `## ~ Ne Duzenlendi`
- `## Paket Notu`

Detayli örnek: [docs/HONORMC-SURUM-SAYFA-TASARIMI.md](docs/HONORMC-SURUM-SAYFA-TASARIMI.md)

## Yukleme Notu

Yayimlama asamalari:

1. `git tag Honor-26.2`
2. `git push origin Honor-26.2`
3. GitHub Action otomatik olarak su assetleri yayinlar:
   - `Honor-26.2.jar`
   - `Honor-26.2-baslat.bat`
   - `Honor-26.2-dagitim.zip`

Not: Bu format repo etiketleriyle birlikte `Honor-*` etiketi takibi icin hazirdir.
