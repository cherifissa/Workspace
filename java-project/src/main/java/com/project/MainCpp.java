package com.project;

public class MainCpp {

    public static void main(String[] args) throws Exception {
        ReportService.USE_CPP_ECARTTYPE = true;

        System.out.println("=== Question 4 : ECARTTYPE (C++) ===");
        ReportService.afficherMoyenneEtEcartType();
    }
}
