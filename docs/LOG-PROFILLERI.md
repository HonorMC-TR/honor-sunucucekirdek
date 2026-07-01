# HonorMC Log Profilleri

## Quiet

Sunucuyu actiginda akan gereksiz satirlari sevmeyen kullanicilar icin.

- Konsolda minimum seviye: WARN
- Plugin enable/disable satirlari: Kapali
- Chunk ve world yukleme ayrintilari: Kapali
- Hata, crash, watchdog, guvenlik ve veri kaybi riski: Her zaman acik

## Balanced

Varsayilan profil.

- Konsolda minimum seviye: INFO
- Plugin enable/disable satirlari: Acik
- Chunk ve world yukleme ayrintilari: Kapali
- Timings/spark onerileri: Sadece gerektiginde

## Diagnostic

Sorun cozmek icin.

- Konsolda minimum seviye: DEBUG
- Plugin enable/disable satirlari: Acik
- Chunk ve world yukleme ayrintilari: Acik
- Ek debug loggerlari: Acilabilir

## Degismez Guvenlik Kurali

Log profili ne olursa olsun su olaylar saklanmaz:

- Crash
- Exception stacktrace
- Watchdog kilitlenmesi
- Veri kaybi riski
- Guvenlik/izin hatalari
- EULA veya lisans uyari gerektiren durumlar
