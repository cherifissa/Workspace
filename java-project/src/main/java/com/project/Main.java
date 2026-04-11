package com.project;

import com.project.proto.FilterRequestOuterClass;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Question 2: MOYENNE Java ===");
        ReportService.afficherMoyenneJava();
        System.out.println();

        System.out.println("=== Question 3: MOYENNE + ECARTTYPE (Python/Numpy) ===");
        ReportService.afficherMoyenneEtEcartTypeJava();
    }

    public static void afficherMoyenneEtEcartType() throws Exception {
        ReportService.afficherMoyenneEtEcartType();
    }

    public static void afficherMoyenneEtEcartType2(FilterRequestOuterClass.FilterRequest request) throws Exception {
        ReportService.afficherMoyenneEtEcartType2(request);
    }
}