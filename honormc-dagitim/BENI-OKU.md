# HonorMC Sunucu Paketi

Bu paket HonorMC'nin sade Turkce dagitim duzenidir. Sunucuyu `BASLAT.ps1` veya `BASLAT.bat` ile baslat.

## Klasorler

- `cekirdek/`: `Honor-<surum>.jar` burada durur.
- `altyapi/`: HonorMC'nin zorunlu altyapi kutuphaneleri burada tutulur.
- `eklentiler/`: Plugin jar dosyalarini buraya koy.
- `dunyalar/`: Dunya dosyalari burada tutulur.
- `ayarlar/`: HonorMC, Bukkit/Paper/Purpur uyumluluk ve sunucu ayarlari burada tutulur.
- `ayarlar/oyuncular/`: OP, beyaz liste, ban listeleri ve oyuncu onbellegi burada tutulur.
- `kayitlar/`: Konsol ve HTML kayitlari burada tutulur. HonorMC Baslatici canli kaydi `kayitlar/canli-konsol.html` dosyasina yazar.
- `yedekler/`: Elle veya ileride baslatici ile alinacak yedekler icin.

## EULA

EULA onayi `ayarlar/eula.txt` dosyasindadir. Ana dizinde yalniz HonorMC'nin okunur baslatma dosyalari ve klasorleri kalacak sekilde duzenlenmistir.

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

## bStats ve spark

HonorMC temiz baslangic icin bStats telemetrisini ve spark profiler'i varsayilan kapali getirir.

- bStats zorunlu degildir; acik olursa kullanim istatistigi gonderir.
- spark zorunlu degildir; performans sorunu incelerken acilacak profiler aracidir.

Sunucu icinden `/telemetri` veya `/honor telemetri` ile mevcut durumu gorebilirsin.
