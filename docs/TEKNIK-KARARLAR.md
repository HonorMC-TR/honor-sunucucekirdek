# HonorMC Teknik Kararlar

## 1. Taban

HonorMC ilk release icin Purpur `26.2` tabanindan ilerler. Bunun nedeni Paper/Purpur eklenti uyumlulugunu koruyarak pazara hizli ve guvenilir girebilmek.

Bu karar "sifirdan Minecraft serveri yazmiyoruz" anlamina gelir. HonorMC, Paper/Purpur ekosistemiyle uyumlu bir fork olarak baslar; kendi markasi, varsayilanlari, log politikasi, performans profilleri ve ilerleyen donemde cekirdek patchleriyle ayrisir.

## 2. Uyumluluk

Birinci oncelik piyasadaki Paper, Purpur, Spigot ve Bukkit eklentilerinin calismasidir.

Uyumlulugu bozabilecek degisiklikler:

- Varsayilan olarak kapali gelir.
- Ayrica belgelenir.
- Sunucu sahibi tarafindan profil veya config uzerinden acilir.

## 3. Coklu Surum Hedefi

Ilk hedef tek ve en guncel tabandir. Coklu surum destegi iki hatta ayrilir:

- Protokol uyumlulugu: Oyuncu istemcilerinin belirli araliklarda baglanabilmesi.
- API/cekirdek uyumlulugu: Farkli Minecraft surumleri icin ayri HonorMC dallari.

Tek jar icinde genis surum araligi hedefi guzel bir vizyon ama cekirdek davranisini karmasiklastirir. Ilk stabil release sonrasinda, bu hedef ayrica tasarlanacak.

## 4. Log Politikasi

HonorMC'nin ayirt edici islerinden biri konsol temizligi olacak. Sunucu sahibi su profilleri secebilecek:

- Quiet: Sadece uyari, hata ve kritik olaylar.
- Balanced: Gunluk kullanim icin temiz INFO akisi.
- Diagnostic: Sorun cozumunde ayrintili log.

Ilk asamada bu profil sistemi dokuman ve config sozlesmesi olarak baslar. Sonraki asamada Log4j filtreleri ve HonorMC config sinifi ile cekirdege baglanir.

## 5. Release

CI ciktisi `build/honormc/Honor-<minecraft-surumu>.jar` olarak uretilir. GitHub Actions artifact adi `HonorMC` olarak sabitlenir.
