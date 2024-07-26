package com.example.cp670_multilingual_ocr;

public interface TextRecognitionCallback {
  void onTextRecognized(String recognizedText);
  void onError(Exception e);
}
