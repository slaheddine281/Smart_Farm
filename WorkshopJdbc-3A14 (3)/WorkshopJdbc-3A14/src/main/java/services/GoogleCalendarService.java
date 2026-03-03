package services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Smart Farm Calendar";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    private Calendar service;

    public GoogleCalendarService() {
        try {
            this.service = getCalendarService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Crée un événement dans Google Calendar
     */
    public Event createEvent(String summary, String description, LocalDateTime startDateTime,
                             LocalDateTime endDateTime, String location) throws IOException {

        Event event = new Event()
                .setSummary(summary)
                .setDescription(description)
                .setLocation(location);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(
                        java.util.Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant())))
                .setTimeZone("Africa/Tunis");

        EventDateTime end = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(
                        java.util.Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant())))
                .setTimeZone("Africa/Tunis");

        event.setStart(start);
        event.setEnd(end);

        return service.events().insert("primary", event).execute();
    }

    /**
     * Crée un événement pour un soin animalier
     */
    public Event createAnimalCareEvent(String animalName, String careType, LocalDate date, String notes)
            throws IOException {

        String summary = "🐾 Soin : " + animalName + " - " + careType;
        String description = "Type de soin : " + careType + "\n" +
                "Animal : " + animalName + "\n" +
                "Notes : " + notes;

        LocalDateTime start = date.atTime(9, 0); // 9h00
        LocalDateTime end = date.atTime(10, 0);  // 10h00

        return createEvent(summary, description, start, end, "Smart Farm - Zone Animale");
    }

    /**
     * Crée un événement pour un traitement vétérinaire
     */
    public Event createVetTreatmentEvent(String animalName, String treatment, LocalDate date,
                                         String vetName) throws IOException {

        String summary = "🏥 Traitement Vétérinaire : " + animalName;
        String description = "Animal : " + animalName + "\n" +
                "Traitement : " + treatment + "\n" +
                "Vétérinaire : " + vetName + "\n" +
                "⚠️ RAPPEL IMPORTANT";

        LocalDateTime start = date.atTime(14, 0); // 14h00
        LocalDateTime end = date.atTime(15, 0);   // 15h00

        return createEvent(summary, description, start, end, "Clinique Vétérinaire");
    }

    /**
     * Crée un événement pour le planning d'un employé
     */
    public Event createEmployeeShiftEvent(String employeeName, String position,
                                          LocalDateTime startDateTime, LocalDateTime endDateTime)
            throws IOException {

        String summary = "👷 Shift : " + employeeName + " (" + position + ")";
        String description = "Employé : " + employeeName + "\n" +
                "Poste : " + position + "\n" +
                "Smart Farm - Planning des employés";

        return createEvent(summary, description, startDateTime, endDateTime, "Smart Farm");
    }

    /**
     * Crée un événement pour une tâche agricole
     */
    public Event createAgriculturalTaskEvent(String taskDescription, LocalDate date,
                                             String assignedTo) throws IOException {

        String summary = "🌾 Tâche : " + taskDescription.substring(0, Math.min(30, taskDescription.length()));
        String description = "Tâche : " + taskDescription + "\n" +
                "Assigné à : " + assignedTo + "\n" +
                "Date prévue : " + date;

        LocalDateTime start = date.atTime(8, 0);  // 8h00
        LocalDateTime end = date.atTime(12, 0);   // 12h00

        return createEvent(summary, description, start, end, "Smart Farm - Champs");
    }

    /**
     * Authentification et création du service Calendar
     */
    private Calendar getCalendarService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Ressource non trouvée : " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}