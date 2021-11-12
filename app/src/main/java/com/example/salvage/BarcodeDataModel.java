package com.example.salvage;

public class BarcodeDataModel {

   //data members for barcode model
    public String barcodeImagePath;
    public String contents;
    public String formatName;

     //Default constructor
    public BarcodeDataModel(){

    }
    // data members declared for the object to be called in MainActivity
    public BarcodeDataModel(String barcodeImagePath, String contents, String formatName) {

        this.barcodeImagePath = barcodeImagePath;
        this.contents = contents;
        this.formatName = formatName;

    }

}
