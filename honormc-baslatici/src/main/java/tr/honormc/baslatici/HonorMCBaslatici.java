package tr.honormc.baslatici;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HonorMCBaslatici {
    private static final String RESET = "\u001B[0m";
    private static final String YESIL = "\u001B[32m";
    private static final String SARI = "\u001B[33m";
    private static final String KIRMIZI = "\u001B[31m";
    private static final String MAVI = "\u001B[36m";
    private static final String CAMGOBEGI_KALIN = "\u001B[36;1m";
    private static final String YESIL_KALIN = "\u001B[32;1m";
    private static final String SARI_KALIN = "\u001B[33;1m";
    private static final String KIRMIZI_KALIN = "\u001B[31;1m";
    private static final String MAVI_KALIN = "\u001B[36;1m";
    private static final String MOR = "\u001B[35m";
    private static final String MOR_KALIN = "\u001B[35;1m";
    private static final String BEYAZ = "\u001B[37m";
    private static final String BEYAZ_KALIN = "\u001B[37;1m";
    private static final String SOLUK = "\u001B[90m";
    private static final Pattern ANSI_KODU = Pattern.compile("\u001B\\[[;?0-9]*[ -/]*[@-~]");
    private static final Pattern LOG_SATIRI = Pattern.compile("^\\[(\\d{2}:\\d{2}:\\d{2})\\] \\[([^\\]]+)\\](?: \\[([^\\]]+)\\])?: ?(.*)$");
    private static final Pattern KOMUT_VURGUSU = Pattern.compile("(?<!\\S)/[A-Za-z0-9_:\\-?]+(?:\\s+[^\\s]+)?");

    private HonorMCBaslatici() {
    }

    public static void main(final String[] args) throws Exception {
        final BaslaticiAyar ayar = BaslaticiAyar.yukle(args);

        if (ayar.yardim) {
            yardimYaz();
            return;
        }

        if (ayar.hazirla) {
            ayar.varsayilanDosyaOlustur();
            bilgi("Varsayilan ayar dosyasi hazir: " + ayar.ayarDosyasi.toAbsolutePath());
            return;
        }

        ayar.dogrula();
        baslat(ayar);
    }

    private static void baslat(final BaslaticiAyar ayar) throws Exception {
        Files.createDirectories(ayar.calismaDizini);
        Files.createDirectories(ayar.calismaDizini.resolve("kayitlar"));
        Files.createDirectories(ayar.calismaDizini.resolve("kayitlar").resolve("html"));
        Files.createDirectories(ayar.calismaDizini.resolve("yedekler"));
        Files.createDirectories(ayar.calismaDizini.resolve("ayarlar"));
        Files.createDirectories(ayar.calismaDizini.resolve("ayarlar").resolve("oyuncular"));
        Files.createDirectories(ayar.calismaDizini.resolve("eklentiler"));
        Files.createDirectories(ayar.calismaDizini.resolve("dunyalar"));
        Files.createDirectories(ayar.calismaDizini.resolve("altyapi").resolve("bundler"));

        final List<String> komut = new ArrayList<>();
        komut.add(ayar.javaKomutu);
        komut.add("-Xms" + ayar.minRam);
        komut.add("-Xmx" + ayar.maxRam);
        komut.add("-Dfile.encoding=UTF-8");
        komut.add("-Duser.language=tr");
        komut.add("-Duser.country=TR");
        komut.addAll(ayar.jvmEkleri);
        komut.add("-jar");
        komut.add(ayar.jarDosyasi);
        if (ayar.nogui) {
            komut.add("--nogui");
        }
        komut.addAll(ayar.sunucuEkleri);

        baslik("HonorMC Baslatici");
        bilgi("Profil: " + ayar.profil);
        bilgi("RAM: " + ayar.minRam + " - " + ayar.maxRam);
        bilgi("Java: " + ayar.javaKomutu);
        bilgi("Cekirdek: " + ayar.jarDosyasi);
        bilgi("Calisma dizini: " + ayar.calismaDizini.toAbsolutePath());
        bilgi("Komut modlari: :yardim, :dur, :filtre");

        final ProcessBuilder builder = new ProcessBuilder(komut);
        builder.directory(ayar.calismaDizini.toFile());
        builder.redirectErrorStream(true);

        final Process surec = builder.start();
        final BufferedWriter sunucuGirdisi = new BufferedWriter(new OutputStreamWriter(surec.getOutputStream(), StandardCharsets.UTF_8));
        final HtmlKayit htmlKayit = HtmlKayit.ac(ayar);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (surec.isAlive()) {
                try {
                    sunucuGirdisi.write("stop");
                    sunucuGirdisi.newLine();
                    sunucuGirdisi.flush();
                    surec.waitFor(15, TimeUnit.SECONDS);
                } catch (final Exception ignored) {
                }
                if (surec.isAlive()) {
                    surec.destroy();
                }
            }
            htmlKayit.close();
        }, "honormc-kapanis"));

        final Thread cikti = new Thread(() -> ciktiOku(surec, ayar, htmlKayit), "honormc-konsol");
        cikti.setDaemon(true);
        cikti.start();

        try (BufferedReader kullanici = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String satir;
            while (surec.isAlive() && (satir = kullanici.readLine()) != null) {
                if (satir.startsWith(":")) {
                    if (baslaticiKomutu(satir, ayar, surec, sunucuGirdisi)) {
                        break;
                    }
                    continue;
                }

                if (!surec.isAlive()) {
                    break;
                }
                komutGirdisiYaz(satir, htmlKayit);
                try {
                    sunucuGirdisi.write(satir);
                    sunucuGirdisi.newLine();
                    sunucuGirdisi.flush();
                } catch (final IOException ex) {
                    if (!surec.isAlive()) {
                        break;
                    }
                    throw ex;
                }
                if ("stop".equalsIgnoreCase(satir.trim())) {
                    break;
                }
            }
        }

        final int kod = surec.waitFor();
        if (kod == 0) {
            bilgi("Sunucu kapandi.");
        } else {
            hata("Sunucu " + kod + " cikis koduyla kapandi.");
        }
        htmlKayit.close();
    }

    private static boolean baslaticiKomutu(
        final String satir,
        final BaslaticiAyar ayar,
        final Process surec,
        final BufferedWriter sunucuGirdisi
    ) throws IOException {
        final String komut = satir.trim().toLowerCase(Locale.ROOT);
        if (":yardim".equals(komut)) {
            bilgi(":dur sunucuyu guvenli kapatir");
            bilgi(":filtre aktif filtre ayarlarini gosterir");
            bilgi("Basinda : olmayan her satir sunucu konsoluna gonderilir.");
            return false;
        }
        if (":filtre".equals(komut)) {
            bilgi("Filtreler: bilgi=" + ayar.bilgi + ", uyari=" + ayar.uyari + ", hata=" + ayar.hata
                + ", oyuncu=" + ayar.oyuncu + ", eklenti=" + ayar.eklenti);
            return false;
        }
        if (":dur".equals(komut) || ":stop".equals(komut)) {
            uyari("Sunucuya stop komutu gonderiliyor...");
            sunucuGirdisi.write("stop");
            sunucuGirdisi.newLine();
            sunucuGirdisi.flush();
            return true;
        }
        uyari("Bilinmeyen baslatici komutu: " + satir);
        return false;
    }

    private static void ciktiOku(final Process surec, final BaslaticiAyar ayar, final HtmlKayit htmlKayit) {
        try (BufferedReader okuyucu = new BufferedReader(new InputStreamReader(surec.getInputStream(), StandardCharsets.UTF_8))) {
            String satir;
            while ((satir = okuyucu.readLine()) != null) {
                final String temizSatir = ansiTemizle(satir);
                if (temizSatir.isBlank() || temizSatir.trim().startsWith(">")) {
                    continue;
                }
                final KonsolSatiri konsolSatiri = KonsolSatiri.coz(temizSatir);
                final Kategori kategori = konsolSatiri.kategori;
                if (!ayar.goster(kategori)) {
                    continue;
                }

                System.out.println(konsolSatiri.ansi());
                htmlKayit.yaz(konsolSatiri);
                analizEt(temizSatir);
            }
        } catch (final IOException ex) {
            hata("Konsol cikti akisi okunamadi: " + ex.getMessage());
        }
    }

    private static void komutGirdisiYaz(final String satir, final HtmlKayit htmlKayit) {
        if (satir.isBlank()) {
            return;
        }
        final KonsolSatiri konsolSatiri = KonsolSatiri.komut(satir);
        System.out.println(konsolSatiri.ansi());
        htmlKayit.yaz(konsolSatiri);
    }

    private static void analizEt(final String satir) {
        final String kucuk = satir.toLowerCase(Locale.ROOT);
        if (kucuk.contains("noclassdeffounderror") || kucuk.contains("classnotfoundexception")) {
            System.out.println(KIRMIZI + "[HONORMC ANALIZ] Eksik eklenti bagimliligi algilandi. Ilgili eklentinin zorunlu kutuphanesini kontrol et." + RESET);
        } else if (kucuk.contains("can't keep up") || kucuk.contains("overloaded")) {
            System.out.println(SARI + "[HONORMC ANALIZ] Sunucu yetisemiyor. MSPT, gorus mesafesi, entity sayisi ve eklenti maliyetleri kontrol edilmeli." + RESET);
        } else if (kucuk.contains("failed to bind to port")) {
            System.out.println(KIRMIZI + "[HONORMC ANALIZ] Port zaten kullaniliyor olabilir. sunucu.properties icindeki server-port degerini kontrol et." + RESET);
        }
    }

    private static String ansiTemizle(final String metin) {
        return ANSI_KODU.matcher(metin).replaceAll("");
    }

    private static void yardimYaz() {
        baslik("HonorMC Baslatici Yardim");
        bilgi("Kullanim: java -jar HonorMC-Baslatici.jar [secenekler]");
        bilgi("--hazirla                    Varsayilan ayar dosyasi olusturur");
        bilgi("--ayar <dosya>               Ayar dosyasi yolu");
        bilgi("--jar <dosya>                Honor-*.jar veya Honor-26.2.jar yolu");
        bilgi("--ram <min:max>              Ornek: 2G:8G");
        bilgi("--profil <ad>                Profil adi");
        bilgi("--calisma-dizini <dizin>     Sunucu klasoru");
        bilgi("--java <komut>               Java komutu");
    }

    private static void baslik(final String metin) {
        System.out.println(MAVI + "==== " + metin + " ====" + RESET);
    }

    private static void bilgi(final String metin) {
        System.out.println(YESIL + "[BASLATICI] " + metin + RESET);
    }

    private static void uyari(final String metin) {
        System.out.println(SARI + "[BASLATICI] " + metin + RESET);
    }

    private static void hata(final String metin) {
        System.err.println(KIRMIZI + "[BASLATICI] " + metin + RESET);
    }

    enum Kategori {
        HATA(KIRMIZI),
        UYARI(SARI),
        OYUNCU(MAVI),
        EKLENTI(MOR),
        KOMUT("\u001B[36;1m"),
        CEKIRDEK(YESIL),
        BILGI(YESIL),
        DIGER(SOLUK);

        final String renk;

        Kategori(final String renk) {
            this.renk = renk;
        }

        String etiket() {
            return switch (this) {
                case HATA -> "HATA";
                case UYARI -> "UYARI";
                case OYUNCU -> "OYUNCU";
                case EKLENTI -> "EKLENTI";
                case KOMUT -> "KOMUT";
                case CEKIRDEK -> "CEKIRDEK";
                case BILGI -> "BILGI";
                case DIGER -> "DIGER";
            };
        }

        static Kategori bul(final String satir) {
            final String kucuk = satir.toLowerCase(Locale.ROOT);
            if (satir.startsWith("WARNING:")) {
                return UYARI;
            }
            if (satir.contains("[HATA]") || kucuk.contains("exception") || kucuk.contains("error") || kucuk.contains("hata")) {
                return HATA;
            }
            if (satir.contains("[UYARI]") || kucuk.contains("warn")) {
                return UYARI;
            }
            if (satir.contains("[bootstrap]") || satir.contains("HonorMC") || satir.contains("[BASLATICI]")) {
                return CEKIRDEK;
            }
            if (satir.contains("[OYUNCU]") || kucuk.contains("oyuna katildi") || kucuk.contains("komutu kullandi") || kucuk.contains("issued server command")) {
                return OYUNCU;
            }
            final String kaynak = kaynakBul(satir);
            if (kaynak != null && !cekirdekKaynagi(kaynak)) {
                return EKLENTI;
            }
            if (satir.contains("[BILGI]")) {
                return BILGI;
            }
            return DIGER;
        }

        String onEk(final String satir) {
            if (this != EKLENTI) {
                return satir;
            }

            final String kaynak = kaynakBul(satir);
            if (kaynak != null && !kaynak.isBlank() && !cekirdekKaynagi(kaynak)) {
                return "[Eklenti - " + kaynak + "] " + satir;
            }
            return satir;
        }

        private static String kaynakBul(final String satir) {
            final int baslikSonu = satir.indexOf(':');
            final String baslik = baslikSonu >= 0 ? satir.substring(0, baslikSonu) : satir;
            int arama = 0;
            int sayac = 0;
            while (arama < baslik.length()) {
                final int bas = baslik.indexOf('[', arama);
                if (bas < 0) {
                    return null;
                }
                final int son = baslik.indexOf(']', bas + 1);
                if (son < 0) {
                    return null;
                }
                sayac++;
                if (sayac == 3) {
                    return baslik.substring(bas + 1, son);
                }
                arama = son + 1;
            }
            return null;
        }

        private static boolean cekirdekKaynagi(final String kaynak) {
            final String kucuk = kaynak.toLowerCase(Locale.ROOT);
            return kucuk.isBlank()
                || "bilgi".equals(kucuk)
                || "uyari".equals(kucuk)
                || "hata".equals(kucuk)
                || "bootstrap".equals(kucuk)
                || "minecraft".equals(kucuk)
                || "plugininitializermanager".equals(kucuk)
                || "worldfoldermigration".equals(kucuk)
                || "chunkholdermanager".equals(kucuk)
                || kucuk.startsWith("moonrise")
                || kucuk.startsWith("net.minecraft")
                || kucuk.startsWith("org.bukkit")
                || kucuk.startsWith("org.spigotmc")
                || kucuk.startsWith("io.papermc")
                || kucuk.startsWith("org.purpurmc")
                || kucuk.startsWith("tr.honormc");
        }
    }

    static final class KonsolSatiri {
        private static final DateTimeFormatter SAAT = DateTimeFormatter.ofPattern("HH:mm:ss");

        final Kategori kategori;
        final String zaman;
        final String kaynak;
        final String mesaj;
        final String ham;

        private KonsolSatiri(
            final Kategori kategori,
            final String zaman,
            final String kaynak,
            final String mesaj,
            final String ham
        ) {
            this.kategori = kategori;
            this.zaman = zaman;
            this.kaynak = kaynak;
            this.mesaj = mesaj;
            this.ham = ham;
        }

        static KonsolSatiri coz(final String ham) {
            if (ham.startsWith("WARNING:")) {
                return new KonsolSatiri(Kategori.UYARI, simdi(), "JVM", ham.substring("WARNING:".length()).trim(), ham);
            }

            final Matcher eslesme = LOG_SATIRI.matcher(ham);
            if (eslesme.matches()) {
                final String zaman = eslesme.group(1);
                final String logger = eslesme.group(3);
                final String mesaj = mesajTurkcelestir(eslesme.group(4));
                final Kategori kategori = Kategori.bul(ham);
                return new KonsolSatiri(kategori, zaman, kaynakEtiketi(kategori, logger, mesaj), mesaj, ham);
            }

            final Kategori kategori = Kategori.bul(ham);
            final String mesaj = mesajTurkcelestir(ham);
            return new KonsolSatiri(kategori, simdi(), kaynakEtiketi(kategori, null, mesaj), mesaj, ham);
        }

        static KonsolSatiri komut(final String komut) {
            return new KonsolSatiri(Kategori.KOMUT, simdi(), "KONSOL", "> " + komut, "[KOMUT] > " + komut);
        }

        String ansi() {
            final String seviye = sabit("[" + this.kategori.etiket() + "]", 10);
            final String kaynakBolumu = "[" + this.kaynak + "]";
            return SOLUK + "[" + this.zaman + "] "
                + this.kategori.renk + seviye + RESET + " "
                + kaynakRengi() + sabit(kaynakBolumu, 22) + RESET + " "
                + mesajRenkli();
        }

        private String mesajRenkli() {
            String sonuc = this.mesaj;
            sonuc = komutVurgula(sonuc);
            sonuc = operatorVurgula(sonuc);
            if (this.kategori == Kategori.HATA) {
                return KIRMIZI_KALIN + sonuc + RESET;
            }
            if (this.kategori == Kategori.UYARI) {
                return SARI_KALIN + sonuc + RESET;
            }
            if (this.kategori == Kategori.OYUNCU) {
                return MAVI_KALIN + oyuncuVurgula(sonuc) + RESET;
            }
            if (this.kategori == Kategori.EKLENTI) {
                return MOR + sonuc + RESET;
            }
            if (this.kategori == Kategori.KOMUT) {
                return CAMGOBEGI_KALIN + sonuc + RESET;
            }
            if (this.kategori == Kategori.CEKIRDEK) {
                return YESIL + sonuc + RESET;
            }
            return BEYAZ + sonuc + RESET;
        }

        private String kaynakRengi() {
            return switch (this.kategori) {
                case HATA -> KIRMIZI_KALIN;
                case UYARI -> SARI_KALIN;
                case OYUNCU -> MAVI_KALIN;
                case EKLENTI -> MOR_KALIN;
                case KOMUT -> CAMGOBEGI_KALIN;
                case CEKIRDEK -> YESIL_KALIN;
                case BILGI -> YESIL;
                case DIGER -> SOLUK;
            };
        }

        private static String kaynakEtiketi(final Kategori kategori, final String logger, final String mesaj) {
            if (kategori == Kategori.OYUNCU) {
                final String oyuncu = oyuncuAdi(mesaj);
                return oyuncu == null ? "OYUNCU" : "OYUNCU/" + oyuncu;
            }
            if (kategori == Kategori.EKLENTI) {
                final String ad = logger == null || logger.isBlank() ? "BILINMIYOR" : logger;
                return "EKLENTI - " + ad;
            }
            if (kategori == Kategori.KOMUT) {
                return "KONSOL";
            }
            if (logger != null && !logger.isBlank()) {
                if ("bootstrap".equalsIgnoreCase(logger) || logger.startsWith("tr.honormc")) {
                    return "CEKIRDEK/" + logger;
                }
                if (logger.startsWith("net.minecraft") || logger.startsWith("Minecraft")) {
                    return "MINECRAFT";
                }
                return logger;
            }
            return switch (kategori) {
                case HATA -> "HATA";
                case UYARI -> "UYARI";
                case CEKIRDEK -> "CEKIRDEK";
                case BILGI -> "SUNUCU";
                default -> "SISTEM";
            };
        }

        private static String oyuncuAdi(final String mesaj) {
            final int ipBas = mesaj.indexOf('[');
            if (ipBas > 0) {
                return mesaj.substring(0, ipBas).trim();
            }
            final int bosluk = mesaj.indexOf(' ');
            if (bosluk > 0) {
                return mesaj.substring(0, bosluk).trim();
            }
            return null;
        }

        private static String oyuncuVurgula(final String mesaj) {
            final String oyuncu = oyuncuAdi(mesaj);
            if (oyuncu == null || oyuncu.isBlank() || !mesaj.startsWith(oyuncu)) {
                return mesaj;
            }
            return BEYAZ_KALIN + oyuncu + RESET + MAVI_KALIN + mesaj.substring(oyuncu.length());
        }

        private static String komutVurgula(final String mesaj) {
            final Matcher eslesme = KOMUT_VURGUSU.matcher(mesaj);
            final StringBuilder sonuc = new StringBuilder();
            while (eslesme.find()) {
                eslesme.appendReplacement(sonuc, Matcher.quoteReplacement(CAMGOBEGI_KALIN + eslesme.group() + RESET + BEYAZ));
            }
            eslesme.appendTail(sonuc);
            return sonuc.toString();
        }

        private static String operatorVurgula(final String mesaj) {
            return mesaj
                .replace("server operator", SARI_KALIN + "server operator" + RESET + BEYAZ)
                .replace("yonetici", SARI_KALIN + "yonetici" + RESET + BEYAZ)
                .replace("OP", SARI_KALIN + "OP" + RESET + BEYAZ);
        }

        private static String mesajTurkcelestir(final String mesaj) {
            if ("Stopping the server".equals(mesaj)) {
                return "Sunucu durduruluyor";
            }
            if (mesaj.startsWith("Waiting 60s for chunk system to halt for world ")) {
                return "Chunk sistemi durduruluyor: " + mesaj.substring("Waiting 60s for chunk system to halt for world ".length());
            }
            if (mesaj.startsWith("Halted chunk system for world ")) {
                return "Chunk sistemi durdu: " + mesaj.substring("Halted chunk system for world ".length());
            }
            if (mesaj.startsWith("Saving all chunkholders for world ")) {
                return "Chunk tutuculari kaydediliyor: " + mesaj.substring("Saving all chunkholders for world ".length());
            }
            if (mesaj.startsWith("Waiting 60s for chunk I/O to halt for world ")) {
                return "Chunk I/O durduruluyor: " + mesaj.substring("Waiting 60s for chunk I/O to halt for world ".length());
            }
            if (mesaj.startsWith("Halted I/O scheduler for world ")) {
                return "I/O zamanlayicisi durdu: " + mesaj.substring("Halted I/O scheduler for world ".length());
            }
            if (mesaj.startsWith("Saved ") && mesaj.contains(" chunks in world ")) {
                return mesaj
                    .replace("Saved", "Kaydedildi:")
                    .replace(" block chunks, ", " blok chunk, ")
                    .replace(" entity chunks, ", " entity chunk, ")
                    .replace(" poi chunks in world ", " poi chunk / dunya ");
            }
            if (mesaj.contains("https://docs.papermc.io/paper/next-steps")) {
                return "Kurulum notlari: BENI-OKU.md";
            }
            return mesaj;
        }

        private static String sabit(final String metin, final int uzunluk) {
            if (metin.length() >= uzunluk) {
                return metin;
            }
            return metin + " ".repeat(uzunluk - metin.length());
        }

        private static String simdi() {
            return LocalDateTime.now().format(SAAT);
        }
    }

    static final class HtmlKayit implements Closeable {
        private final BufferedWriter writer;
        private boolean kapandi;

        private HtmlKayit(final BufferedWriter writer) {
            this.writer = writer;
        }

        static HtmlKayit ac(final BaslaticiAyar ayar) throws IOException {
            if (!ayar.htmlKayit) {
                return new HtmlKayit(null);
            }

            final Path dosya = ayar.calismaDizini.resolve(ayar.htmlKayitDosyasi).normalize();
            Files.createDirectories(dosya.toAbsolutePath().getParent());
            final BufferedWriter writer = Files.newBufferedWriter(dosya, StandardCharsets.UTF_8);
            writer.write("""
                <!doctype html>
                <html lang="tr">
                <head>
                  <meta charset="utf-8">
                  <title>HonorMC Konsol Kaydi</title>
                  <style>
                    body{margin:0;background:#0f131a;color:#d7dde8;font:14px Consolas,monospace}
                    header{position:sticky;top:0;background:#171d26;padding:12px 16px;border-bottom:1px solid #2a3443}
                    main{padding:12px 16px;white-space:pre-wrap}
                    .satir{display:grid;grid-template-columns:88px 96px 240px 1fr;gap:10px;line-height:1.5}
                    .saat{color:#8b949e}.seviye,.kaynak{font-weight:700}.mesaj{color:#d7dde8}
                    .BILGI .seviye,.BILGI .kaynak,.CEKIRDEK .seviye,.CEKIRDEK .kaynak{color:#7ee787}
                    .UYARI .seviye,.UYARI .kaynak{color:#ffd166}.HATA .seviye,.HATA .kaynak,.HATA .mesaj{color:#ff6b6b;font-weight:700}
                    .OYUNCU .seviye,.OYUNCU .kaynak{color:#5cc8ff}.EKLENTI .seviye,.EKLENTI .kaynak{color:#d2a8ff}
                    .KOMUT .seviye,.KOMUT .kaynak,.KOMUT .mesaj{color:#39d0d8;font-weight:700}.DIGER{color:#9aa7b7}
                  </style>
                </head>
                <body>
                <header>HonorMC canli konsol kaydi - metin secilebilir, kopyalanabilir ve kategori renkleri korunur.</header>
                <main>
                """);
            writer.flush();
            return new HtmlKayit(writer);
        }

        synchronized void yaz(final KonsolSatiri satir) {
            if (this.writer == null) {
                return;
            }
            try {
                this.writer.write("<span class=\"satir " + satir.kategori.name() + "\">");
                this.writer.write("<span class=\"saat\">[" + escape(satir.zaman) + "]</span>");
                this.writer.write("<span class=\"seviye\">[" + escape(satir.kategori.etiket()) + "]</span>");
                this.writer.write("<span class=\"kaynak\">[" + escape(satir.kaynak) + "]</span>");
                this.writer.write("<span class=\"mesaj\">" + escape(satir.mesaj) + "</span>");
                this.writer.write("</span>");
                this.writer.newLine();
                this.writer.flush();
            } catch (final IOException ignored) {
            }
        }

        @Override
        public synchronized void close() {
            if (this.writer == null || this.kapandi) {
                return;
            }
            this.kapandi = true;
            try {
                this.writer.write("</main></body></html>");
                this.writer.newLine();
                this.writer.close();
            } catch (final IOException ignored) {
            }
        }

        private static String escape(final String metin) {
            return metin
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
        }
    }

    static final class BaslaticiAyar {
        Path ayarDosyasi = Path.of("ayarlar", "baslatici.properties");
        String jarDosyasi = "cekirdek/Honor-*.jar";
        Path calismaDizini = Path.of(".");
        String javaKomutu = "java";
        String minRam = "1G";
        String maxRam = "4G";
        String profil = "varsayilan";
        boolean nogui = true;
        boolean yardim;
        boolean hazirla;
        boolean bilgi = true;
        boolean uyari = true;
        boolean hata = true;
        boolean oyuncu = true;
        boolean eklenti = true;
        boolean htmlKayit = true;
        Path htmlKayitDosyasi = Path.of("kayitlar", "canli-konsol.html");
        final List<String> jvmEkleri = new ArrayList<>();
        final List<String> sunucuEkleri = new ArrayList<>();

        static BaslaticiAyar yukle(final String[] args) throws IOException {
            final BaslaticiAyar ayar = new BaslaticiAyar();
            ayar.jvmEkleri.add("--enable-native-access=ALL-UNNAMED");
            ayar.jvmEkleri.add("--illegal-native-access=allow");
            ayar.jvmEkleri.add("--sun-misc-unsafe-memory-access=allow");
            ayar.jvmEkleri.add("-DbundlerRepoDir=altyapi/bundler");
            for (int i = 0; i < args.length; i++) {
                if ("--ayar".equals(args[i]) && i + 1 < args.length) {
                    ayar.ayarDosyasi = Path.of(args[++i]);
                }
            }

            if (Files.isRegularFile(ayar.ayarDosyasi)) {
                ayar.dosyadanOku();
            }

            for (int i = 0; i < args.length; i++) {
                final String arg = args[i];
                switch (arg) {
                    case "--yardim", "-h" -> ayar.yardim = true;
                    case "--hazirla" -> ayar.hazirla = true;
                    case "--jar" -> ayar.jarDosyasi = args[++i];
                    case "--calisma-dizini" -> ayar.calismaDizini = Path.of(args[++i]);
                    case "--java" -> ayar.javaKomutu = args[++i];
                    case "--profil" -> ayar.profil = args[++i];
                    case "--ram" -> {
                        final String[] parcalar = args[++i].split(":", 2);
                        ayar.minRam = parcalar[0];
                        ayar.maxRam = parcalar.length == 2 ? parcalar[1] : parcalar[0];
                    }
                    case "--ayar" -> i++;
                    default -> {
                        if (arg.startsWith("-D") || arg.startsWith("-XX:")) {
                            ayar.jvmEkleri.add(arg);
                        } else {
                            ayar.sunucuEkleri.add(arg);
                        }
                    }
                }
            }
            return ayar;
        }

        void dosyadanOku() throws IOException {
            final Properties p = new Properties();
            try (var in = Files.newInputStream(this.ayarDosyasi)) {
                p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
            this.jarDosyasi = p.getProperty("jar", this.jarDosyasi);
            this.calismaDizini = Path.of(p.getProperty("calismaDizini", this.calismaDizini.toString()));
            this.javaKomutu = p.getProperty("java", this.javaKomutu);
            this.minRam = p.getProperty("minRam", this.minRam);
            this.maxRam = p.getProperty("maxRam", this.maxRam);
            this.profil = p.getProperty("profil", this.profil);
            this.nogui = Boolean.parseBoolean(p.getProperty("nogui", Boolean.toString(this.nogui)));
            this.bilgi = Boolean.parseBoolean(p.getProperty("log.bilgi", Boolean.toString(this.bilgi)));
            this.uyari = Boolean.parseBoolean(p.getProperty("log.uyari", Boolean.toString(this.uyari)));
            this.hata = Boolean.parseBoolean(p.getProperty("log.hata", Boolean.toString(this.hata)));
            this.oyuncu = Boolean.parseBoolean(p.getProperty("log.oyuncu", Boolean.toString(this.oyuncu)));
            this.eklenti = Boolean.parseBoolean(p.getProperty("log.eklenti", Boolean.toString(this.eklenti)));
            this.htmlKayit = Boolean.parseBoolean(p.getProperty("log.html", Boolean.toString(this.htmlKayit)));
            this.htmlKayitDosyasi = Path.of(p.getProperty("log.htmlDosyasi", this.htmlKayitDosyasi.toString()));
            listeOku(p.getProperty("jvmEkleri", ""), this.jvmEkleri);
            listeOku(p.getProperty("sunucuEkleri", ""), this.sunucuEkleri);
        }

        void varsayilanDosyaOlustur() throws IOException {
            Files.createDirectories(this.ayarDosyasi.toAbsolutePath().getParent());
            final String icerik = """
                # HonorMC Baslatici ayarlari
                jar=cekirdek/Honor-*.jar
                calismaDizini=.
                java=java
                minRam=1G
                maxRam=4G
                profil=varsayilan
                nogui=true
                log.bilgi=true
                log.uyari=true
                log.hata=true
                log.oyuncu=true
                log.eklenti=true
                log.html=true
                log.htmlDosyasi=kayitlar/canli-konsol.html
                jvmEkleri=
                sunucuEkleri=--config ayarlar/sunucu.properties --plugins eklentiler --world-dir dunyalar --world ana-dunya --bukkit-settings ayarlar/bukkit-uyumluluk.yml --spigot-settings ayarlar/spigot-uyumluluk.yml --paper-settings-directory ayarlar/paper --paper-settings ayarlar/paper-eski-uyumluluk.yml --purpur-settings ayarlar/purpur-uyumluluk.yml --commands-settings ayarlar/komutlar.yml
                """;
            Files.writeString(this.ayarDosyasi, icerik, StandardCharsets.UTF_8);
        }

        void dogrula() throws IOException {
            final Path gercekJar = jarBul();
            if (!Files.isRegularFile(gercekJar)) {
                throw new IOException("HonorMC jar bulunamadi: " + gercekJar.toAbsolutePath());
            }
            this.jarDosyasi = gercekJar.toAbsolutePath().normalize().toString();
            final Path eula = this.calismaDizini.resolve("ayarlar").resolve("eula.txt");
            if (!Files.isRegularFile(eula)) {
                uyari("ayarlar/eula.txt bulunamadi. Minecraft EULA onayi gereklidir.");
            }
        }

        private Path jarBul() throws IOException {
            final String jarMetni = this.jarDosyasi;
            if (!jarMetni.contains("*") && !jarMetni.contains("?")) {
                return this.calismaDizini.resolve(Path.of(jarMetni)).normalize();
            }

            final String temizJarMetni = jarMetni.replace('\\', '/');
            final int ayirac = temizJarMetni.lastIndexOf('/');
            final String ustDizin = ayirac >= 0 ? temizJarMetni.substring(0, ayirac) : ".";
            final String dosyaDeseni = ayirac >= 0 ? temizJarMetni.substring(ayirac + 1) : temizJarMetni;
            final Path aramaDizini = this.calismaDizini
                .resolve(ustDizin)
                .normalize();
            if (!Files.isDirectory(aramaDizini)) {
                return aramaDizini.resolve(dosyaDeseni).normalize();
            }

            final PathMatcher esleyici = FileSystems.getDefault().getPathMatcher("glob:" + dosyaDeseni);
            try (var akim = Files.list(aramaDizini)) {
                return akim
                    .filter(Files::isRegularFile)
                    .filter(path -> esleyici.matches(path.getFileName()))
                    .sorted(Comparator.comparing((Path path) -> path.getFileName().toString()).reversed())
                    .findFirst()
                    .orElseGet(() -> aramaDizini.resolve(dosyaDeseni))
                    .normalize();
            }
        }

        boolean goster(final Kategori kategori) {
            return switch (kategori) {
                case BILGI -> this.bilgi;
                case UYARI -> this.uyari;
                case HATA -> this.hata;
                case OYUNCU -> this.oyuncu;
                case EKLENTI -> this.eklenti;
                case KOMUT -> true;
                case CEKIRDEK -> true;
                case DIGER -> true;
            };
        }

        private static void listeOku(final String deger, final List<String> hedef) {
            if (deger == null || deger.isBlank()) {
                return;
            }
            for (final String parca : deger.split("\\s+")) {
                if (!parca.isBlank()) {
                    hedef.add(parca.trim());
                }
            }
        }
    }
}
