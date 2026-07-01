# HonorMC Sunucu Paketi

Bu paket HonorMC'nin sade Turkce dagitim duzenidir. Sunucuyu `BASLAT.ps1` veya `BASLAT.bat` ile baslat.

## Klasorler

- `cekirdek/`: `Honor-<surum>.jar` burada durur.
- `eklentiler/`: Plugin jar dosyalarini buraya koy.
- `dunyalar/`: Dunya dosyalari burada tutulur.
- `ayarlar/`: HonorMC, Bukkit/Paper/Purpur uyumluluk ve sunucu ayarlari burada tutulur.
- `kayitlar/`: HonorMC tarafindan ayrilan kayit klasoru. Cekirdek log sistemi su an bazi loglari `logs/` altinda da uretebilir.
- `yedekler/`: Elle veya ileride baslatici ile alinacak yedekler icin.

## Neden bazi ayar anahtarlari Ingilizce?

Minecraft, Bukkit, Spigot, Paper ve Purpur ile uyumlu kalmak icin bazi dosya anahtarlari Ingilizce olmak zorunda. HonorMC bunlari Turkce aciklama, Turkce klasor ve Turkce komut katmani ile sade hale getirir.

## Lisans Notu

HonorMC, Paper/Purpur uyumlu turev cekirdek olarak ilerler. Detay icin `LISANS-NOTU.md` dosyasini oku.

## RAM Ayari

PowerShell veya CMD icinde su ortam degiskenleriyle RAM ayarlanabilir:

```powershell
$env:HONORMC_MIN_RAM="1G"
$env:HONORMC_MAX_RAM="4G"
.\BASLAT.ps1
```

Varsayilan degerler: minimum `1G`, maksimum `4G`.

## Temiz Konsol

Varsayilan log profili `sade` moddur. Bu mod bilinen yabanci bootstrap/JVM gurultusunu saklar veya Turkce ozetler.

Ham loglari gormek istersen:

```powershell
$env:HONORMC_LOG="detayli"
.\BASLAT.ps1
```
