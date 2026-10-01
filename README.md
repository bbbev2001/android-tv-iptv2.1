# Türk Dizi TV — Android TV

Android TV için Türkçe bir dizi/film servis kataloğu. Uygulama içerik kopyalamaz; kartlara basınca ilgili resmi servis açılır.

## Başlangıç servisleri
- tabii — https://www.tabii.com/tr/
- Netflix Türkiye — https://www.netflix.com/tr/
- Disney+ Türkiye — https://www.disneyplus.com/tr-tr/

## Özellikler
- Android TV / Google TV kumandasıyla D-pad navigasyonu
- Favoriler
- Son kullanılan servisler
- Uygulama içinden yeni site ekleme
- Site silme
- Site adresi güncelleme
- Kalıcı kayıt (SharedPreferences)
- Türkçe arayüz

## APK oluşturma
Android Studio ile klasörü açın ve Gradle Sync sonrası `app > build > outputs > apk > debug` altından APK oluşturun.

Not: Bu çalışma ortamında Android SDK/build-tools bulunmadığı için burada ikili APK derlenemedi; proje kaynakları hazırdır.
