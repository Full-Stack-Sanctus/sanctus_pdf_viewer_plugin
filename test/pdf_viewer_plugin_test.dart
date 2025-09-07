import 'package:flutter_test/flutter_test.dart';
import 'package:pdf_viewer_plugin/pdf_viewer_plugin.dart';
import 'package:pdf_viewer_plugin/pdf_viewer_plugin_platform_interface.dart';
import 'package:pdf_viewer_plugin/pdf_viewer_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockPdfViewerPluginPlatform
    with MockPlatformInterfaceMixin
    implements PdfViewerPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final PdfViewerPluginPlatform initialPlatform = PdfViewerPluginPlatform.instance;

  test('$MethodChannelPdfViewerPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelPdfViewerPlugin>());
  });

  test('getPlatformVersion', () async {
    PdfViewerPlugin pdfViewerPlugin = PdfViewerPlugin();
    MockPdfViewerPluginPlatform fakePlatform = MockPdfViewerPluginPlatform();
    PdfViewerPluginPlatform.instance = fakePlatform;

    expect(await pdfViewerPlugin.getPlatformVersion(), '42');
  });
}
