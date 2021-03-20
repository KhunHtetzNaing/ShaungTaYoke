package com.htetznaing.tester;

public class MyClass {

    public static void main(String[] args) {
        for (int i=689;i<=700;i++){
            String exampleBarcode = i+"12";
            System.out.println(exampleBarcode+" => "+isChinaProducts(exampleBarcode));
        }
    }

    private static boolean isChinaProducts(String barcodeNumber){
        // China products barcode prefix = 690-699
        // Ref => https://en.wikipedia.org/wiki/List_of_GS1_country_codes
        String regex = "^(69[0-9]\\d).*";
        return barcodeNumber.matches(regex);
    }
}