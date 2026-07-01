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
- `## Gecis Karsilastirmasi`
- `## Paket Notu`

Detayli örnek: [docs/HONORMC-SURUM-SAYFA-TASARIMI.md](docs/HONORMC-SURUM-SAYFA-TASARIMI.md)

## Yukleme Notu

Yayimlama asamalari (ornek akis):

1. `powershell -ExecutionPolicy Bypass -File ./tools/honormc-yayin-hazirla.ps1 -Version Honor-26.2`
2. Script, tag'i `Honor-26.2` olarak olusturur ve tag aciklamasini release formatinda hazirlar.
3. Tag push'i otomatik olarak release akisini tetikler.
4. GitHub Action otomatik olarak su assetleri yayinlar:
   - `Honor-26.2.jar`
   - `Honor-26.2-baslat.bat`
   - `Honor-26.2-dagitim.zip`

Not: Tag mesaji artık su sayfasinda gorunen release ozeti ile uyumlu olacak sekilde ayarlanir; hem `releases` hem `tags` sayfalarinda aynı surum ozeti gorulur.
