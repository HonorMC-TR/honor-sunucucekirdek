# HonorMC Guncelleme Stratejisi

## Kaynaklar

- Purpur API: `https://api.purpurmc.org/v2/purpur/<version>`
- Paper API: `https://fill.papermc.io/v3/projects/paper/versions/<version>/builds`
- Git upstream: `https://github.com/PurpurMC/Purpur`

## Kontrol

`tools/check-upstream.ps1` hedef surum icin en yeni Purpur buildini ve Paper build kanalini raporlar.

Yerel calistirma:

```powershell
powershell -ExecutionPolicy Bypass -File ./tools/check-upstream.ps1 -Version 26.2
```

GitHub Actions icinde bu kontrol 6 saatte bir calisir.

## Release Karari

Yeni upstream build cikmasi otomatik release sebebi degildir. HonorMC release karari icin:

- Upstream build kanali kontrol edilir.
- Patchlerin temiz uygulanmasi gerekir.
- `packageHonorMCJar` basarili olmalidir.
- Kisa smoke test gecmelidir.
