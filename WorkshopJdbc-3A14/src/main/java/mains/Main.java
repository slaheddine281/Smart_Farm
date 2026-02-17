package mains;

import entities.Animals;
import services.Serviceanimals;

import java.sql.SQLException;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Serviceanimals servicePersonne = new Serviceanimals();

        // Créer un animal SANS ID (pour insertion dans la BDD)
        Animals animal1 = new Animals(
                "Vache",                        // type
                "Holstein",                     // breed
                LocalDate.of(2023, 1, 15),     // birthDate
                "Bonne santé"                   // healthStatus
        );

        // Créer un animal AVEC ID (pour mise à jour ou test)
        Animals animal2 = new Animals(
                1,                              // id
                "Chèvre",                       // type
                "Alpine",                       // breed
                LocalDate.of(2022, 6, 10),     // birthDate
                "Excellente"                    // healthStatus
        );

        // Autre exemple : Mouton
        Animals animal3 = new Animals(
                "Mouton",                       // type
                "Mérinos",                      // breed
                LocalDate.of(2024, 3, 20),     // birthDate
                "Sous surveillance"             // healthStatus
        );


    }
}