# HonorMC Ekosistem Stratejisi

HonorMC'nin uzun vadeli hedefi yalnizca Purpur/Paper uzerine Turkce bir dagitim yapmak degil, kendi araclari, kendi API yuzeyi, kendi konsol deneyimi ve zamanla kendi cekirdek modulleri olan bir Minecraft sunucu ekosistemi kurmaktir.

## Temel Karar

HonorMC iki hatta ilerler:

- HonorMC Legacy: Bugun calisan, Paper/Purpur eklenti uyumlulugunu koruyan cekirdek.
- HonorMC Native: HonorMC'ya ait API, baslatici, konsol, ayar sistemi, profil sistemi, performans olcumleri ve ileride temiz cekirdek modulleri.

Bu ayrim bilincli tutulur. Legacy hat piyasadaki eklentileri calistirir. Native hat, HonorMC'nin gercek ekosistemidir.

## Kirmizi Cizgi

"Sadece dunya olusturma, redstone, oyuncu hareketi gibi mekanikleri hazir alalim" fikri pratikte cekirdegin en kritik ve en riskli kismini almak anlamina gelir. Bu kodlari alip yeni projeye tasimak telif/lisans yukunu ortadan kaldirmaz.

Temiz HonorMC cekirdegi icin iki guvenli yol vardir:

- Clean-room uygulama: davranisi testlerden, protokol gozlemlerinden ve belgelenmis davranistan yeniden yazariz; kaynak kod kopyalamayiz.
- Uygun lisansli altyapi: Mojang kodu icermedigini belirten, lisansi uygun bir sunucu kutuphanesi veya motoru uzerinde HonorMC Native prototipi kurariz.

## Mimari Katmanlar

1. HonorMC Baslatici
   - Turkce GUI/CLI.
   - RAM, Java, jar, eklenti, yedek, profil ve guncelleme kontrolu.
   - Log filtreleme, hata vurgulama, eklenti kaynakli mesajlari ayirma.

2. HonorMC Platform
   - `honormc-api`: Turkce dokumantasyonlu native API.
   - `honormc-plugin`: HonorMC eklenti tanimi, yasam dongusu ve izin modeli.
   - `honormc-config`: temiz ayar semasi ve profil sistemi.
   - `honormc-observability`: TPS/MSPT, chunk IO, entity tick, bellek, GC ve eklenti maliyetleri.

3. HonorMC Uyumluluk Kopruleri
   - `honormc-compat-bukkit`: Bukkit/Paper eklentileri icin kopru.
   - `honormc-compat-paper`: Paper'a ozel API davranislarini kademeli destekleme.
   - Bu kopruler Native cekirdegi kirletmez; ayri modul olarak tutulur.

4. HonorMC Clean Core
   - `honormc-protocol`: el sikisma, durum ping, login, packet hattinin temiz uygulamasi.
   - `honormc-world`: chunk, bolge dosyasi, dunya yukleme/kaydetme.
   - `honormc-entity`: tick, hareket, carpisma, oyuncu giris/cikis.
   - `honormc-redstone`: redstone davranisi ve test takimi.
   - `honormc-bench`: karsilastirmali benchmarklar.

## Yol Haritasi

### M0 - Bugunku Dagitim

HonorMC Legacy cekirdegi temizlenir:

- F3 ve marka yuzeyi HonorMC olur.
- Konsol Turkce, renkli ve filtrelenebilir olur.
- Dagitim klasorleri sade olur.
- Performans profilleri secilebilir olur.

### M1 - HonorMC Baslatici

Purpur/Paper cekirdegi degismese bile baslatici tamamen HonorMC'ya ait olur:

- Tek tusla baslat/durdur/yeniden baslat.
- Java surumu ve RAM dogrulama.
- Eklenti hata tespiti.
- Log kategorileri: CEKIRDEK, OYUNCU, EKLENTI, HATA, UYARI, PERFORMANS.
- GitHub surum kontrolu.

### M2 - HonorMC Native Platform

Mevcut cekirdek uzerinde HonorMC API katmani baslar:

- Native eklenti manifesti.
- Turkce dokumantasyon.
- API karar kayitlari.
- Bukkit/Paper eklentileri yaninda HonorMC eklentileri.

### M3 - Temiz Cekirdek Prototipi

Mojang/Paper/Purpur uygulama kodu kopyalanmadan ayri bir prototip baslar:

- Sunucu listesinde gorunme.
- Login ve oyuncu baglantisi.
- Basit dunya/chunk gosterimi.
- Hareket ve chat.
- Eklenti uyumlulugu hedeflenmez; once motor dogrulanir.

### M4 - Mekanik Modulleri

Davranis testleriyle mekanikler yazilir:

- Chunk yukleme/kaydetme.
- Oyuncu hareket dogrulama.
- Temel entity tick.
- Redstone test dunyalari.
- Vanilla davranis fark raporlari.

### M5 - Uyumluluk Koprusu

Native cekirdek olgunlasinca Bukkit/Paper koprusu ayrica denenir:

- Oncelik her eklenti degil, en cok kullanilan eklenti siniflari.
- Plugin API farklari acik raporlanir.
- Uyumluluk, performansi bozmayacak sekilde secilebilir olur.

## Performans Ilkesi

HonorMC "daha hizli" oldugunu iddia etmeden once olcer:

- Baslangic suresi.
- Ortalama ve p95 MSPT.
- Chunk yukleme/kaydetme suresi.
- Entity tick maliyeti.
- Eklenti basina sure.
- Bellek ve GC davranisi.

Her optimizasyon profili olculebilir, geri alinabilir ve kullanici tarafindan secilebilir olmalidir.

## Lisans ve Kaynak Notu

Bu belge hukuki danismanlik degildir. Uygulama kodu kopyalanmadan ilerlemek HonorMC'nin uzun vadeli sagligi icin temel prensiptir.

- Minecraft EULA, Mojang/Microsoft'un yaptiklarini izinsiz dagitmamayi sart kosar.
- Paper lisansi upstream projelerden gelen GPLv3 yukunu tasir.
- Purpur kendi patchleri icin MIT belirtir, ancak Paper/Paperweight kaynaklarinin lisansina da atif yapar.
- Minestom gibi alternatifler Mojang kodu icermeden sunucu yazma yaklasimina ornek olabilir; Bukkit/Paper API'lerini birebir uygulamadigini kendisi de belirtir.

## Kisa Sonuc

HonorMC'nin dogru yolu:

1. Bugunku HonorMC Legacy cekirdegini piyasaya cikacak kadar temiz ve guvenilir tut.
2. Paralelde HonorMC Native platformunu yaz.
3. Temiz cekirdegi ayri modullerle, test ve benchmark uzerinden buyut.
4. Uyumlulugu kopru olarak ekle; cekirdegin kalbine karistirma.
