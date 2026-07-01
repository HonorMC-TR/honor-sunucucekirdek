# HonorMC Baslatici

HonorMC Baslatici, HonorMC sunucusunu Turkce konsol ve basit profil ayarlariyla calistiran ilk MVP aracidir.

## Build

```powershell
./gradlew :honormc-baslatici:jar
```

Cikti:

- `honormc-baslatici/build/libs/HonorMC-Baslatici-<surum>.jar`

Dagitim paketine otomatik eklenir:

- `build/honormc-dagitim/baslatici/HonorMC-Baslatici.jar`

## Kullanim

Dagitim klasorunde:

```powershell
java -jar baslatici/HonorMC-Baslatici.jar --ayar ayarlar/baslatici.properties
```

Kisayollar:

- `HONORMC-BASLATICI.bat`
- `HONORMC-BASLATICI.ps1`

## Konsol Komutlari

- `:yardim` baslatici komutlarini gosterir.
- `:filtre` aktif log filtrelerini gosterir.
- `:dur` sunucuya `stop` komutu gonderir.

Basinda `:` olmayan her satir dogrudan Minecraft sunucu konsoluna iletilir.

## Ilk MVP Ozellikleri

- `Honor-<surum>.jar` yolunu ayardan okur; `Honor-*.jar` desenini otomatik bulabilir.
- RAM araligini ayardan okur.
- Java komutunu ayardan okur.
- Calisma dizinini ayardan okur.
- Turkce kategori renkleriyle konsol ciktisi verir.
- Eksik eklenti bagimliligi, port baglama hatasi ve sunucu yetisememe sinyallerini analiz eder.
- `BILGI`, `UYARI`, `HATA`, `OYUNCU`, `EKLENTI` filtrelerini destekler.
