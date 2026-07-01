package tr.honormc.baslatici;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

public final class HonorMCBaslatici {
    private static final String RESET = "\u001B[0m";
    private static final String YESIL = "\u001B[32m";
    private static final String SARI = "\u001B[33m";
    private static final String KIRMIZI = "\u001B[31m";
    private static final String MAVI = "\u001B[36m";
    private static final String MOR = "\u001B[35m";
    private static final String SOLUK = "\u001B[90m";

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
        Files.createDirectories(ayar.calismaDizini.resolve("yedekler"));

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
        }, "honormc-kapanis"));

        final Thread cikti = new Thread(() -> ciktiOku(surec, ayar), "honormc-konsol");
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

                sunucuGirdisi.write(satir);
                sunucuGirdisi.newLine();
                sunucuGirdisi.flush();
            }
        }

        final int kod = surec.waitFor();
        if (kod == 0) {
            bilgi("Sunucu kapandi.");
        } else {
            hata("Sunucu " + kod + " cikis koduyla kapandi.");
        }
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

    private static void ciktiOku(final Process surec, final BaslaticiAyar ayar) {
        try (BufferedReader okuyucu = new BufferedReader(new InputStreamReader(surec.getInputStream(), StandardCharsets.UTF_8))) {
            String satir;
            while ((satir = okuyucu.readLine()) != null) {
                final Kategori kategori = Kategori.bul(satir);
                if (!ayar.goster(kategori)) {
                    continue;
                }

                System.out.println(kategori.renk + satir + RESET);
                analizEt(satir);
            }
        } catch (final IOException ex) {
            hata("Konsol cikti akisi okunamadi: " + ex.getMessage());
        }
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
        BILGI(YESIL),
        DIGER(SOLUK);

        final String renk;

        Kategori(final String renk) {
            this.renk = renk;
        }

        static Kategori bul(final String satir) {
            final String kucuk = satir.toLowerCase(Locale.ROOT);
            if (satir.contains("[HATA]") || kucuk.contains("exception") || kucuk.contains("error")) {
                return HATA;
            }
            if (satir.contains("[UYARI]") || kucuk.contains("warn")) {
                return UYARI;
            }
            if (satir.contains("[OYUNCU]") || kucuk.contains("oyuna katildi") || kucuk.contains("komutu kullandi")) {
                return OYUNCU;
            }
            if (satir.matches(".*\\[[^\\]]+].*\\[[^\\]]+].*") && !satir.contains("[bootstrap]")) {
                return EKLENTI;
            }
            if (satir.contains("[BILGI]")) {
                return BILGI;
            }
            return DIGER;
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
        final List<String> jvmEkleri = new ArrayList<>();
        final List<String> sunucuEkleri = new ArrayList<>();

        static BaslaticiAyar yukle(final String[] args) throws IOException {
            final BaslaticiAyar ayar = new BaslaticiAyar();
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
                jvmEkleri=
                sunucuEkleri=
                """;
            Files.writeString(this.ayarDosyasi, icerik, StandardCharsets.UTF_8);
        }

        void dogrula() throws IOException {
            final Path gercekJar = jarBul();
            if (!Files.isRegularFile(gercekJar)) {
                throw new IOException("HonorMC jar bulunamadi: " + gercekJar.toAbsolutePath());
            }
            this.jarDosyasi = gercekJar.toAbsolutePath().normalize().toString();
            final Path eula = this.calismaDizini.resolve("eula.txt");
            if (!Files.isRegularFile(eula)) {
                uyari("eula.txt bulunamadi. Minecraft EULA onayi gereklidir.");
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
                    .orElse(aramaDizini.resolve(dosyaDeseni))
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
