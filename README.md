# pdf_viewer_plugin

A Flutter plugin for displaying PDF files on **Android** and **iOS**.  
This plugin provides a simple way to integrate PDF viewing functionality directly into your Flutter applications. Uses Jetpack Security (a.k.a. AndroidX Security Crypto) — a library from Google that makes it easy to securely store files and data on Android devices.

---

## ✨ Features
- View PDF files from local assets, pdf downloadable links or device storage  
- Support for both Android and iOS  
- Easy to use widget integration  

---

## 📦 Installation

Add the plugin to your `pubspec.yaml`:

```yaml
dependencies:
  pdf_viewer_plugin:
    git:
      url: https://github.com/Full-Stack-Sanctus/sanctus_pdf_viewer_plugin.git
```yaml


## 🚀 Usage

```dart
import 'package:pdf_viewer_plugin/pdf_viewer_plugin.dart';

@override
Widget build(BuildContext context) {
  return PdfViewer(
    url: "https://assets/sample.pdf",
  );
}
