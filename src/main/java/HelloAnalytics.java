import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.Accounts;
import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Webproperties;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.io.IOException;


/**
 * A simple example of how to access the Google Analytics API using a service
 * account.
 */
public class HelloAnalytics {


  private static final String APPLICATION_NAME = "Hello Analytics";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final String KEY_FILE_LOCATION = "C:\\Users\\xing.e.liu\\Desktop\\google_Analytics\\src\\main\\java\\client_secrets.json";
  public static void main(String[] args) {
    try {
//        System.out.println("I've reached try");
      Analytics analytics = initializeAnalytics();
//      System.out.println("I've initialized analytics");
      String profile = getFirstProfileId(analytics);
      System.out.println("First Profile Id: "+ profile);
      printResults(getResults(analytics, profile));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Initializes an Analytics service object.
   *
   * @return An authorized Analytics service object.
   * @throws IOException
   * @throws GeneralSecurityException
   */
  private static Analytics initializeAnalytics() throws GeneralSecurityException, IOException {

    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    GoogleCredential credential = GoogleCredential
        .fromStream(new FileInputStream(KEY_FILE_LOCATION))
        .createScoped(AnalyticsScopes.all());

    // Construct the Analytics service object.
//    return new AnalyticsReporting(httpTransport, JSON_FACTORY, credential)
    return new Analytics.Builder(httpTransport, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME).build();
  }


  private static String getFirstProfileId(Analytics analytics) throws IOException {
    // Get the first view (profile) ID for the authorized user.
    String profileId = null;
//    System.out.println("getting first profile id");
    // Query for the list of all accounts associated with the service account.
    Accounts accounts = analytics.management().accounts().list().execute();

    if (accounts.getItems().isEmpty()) {
      System.err.println("No accounts found");
    } else {
      String firstAccountId = accounts.getItems().get(0).getId();

      // Query for the list of properties associated with the first account.
      Webproperties properties = analytics.management().webproperties()
          .list(firstAccountId).execute();

      if (properties.getItems().isEmpty()) {
        System.err.println("No Webproperties found");
      } else {
        String firstWebpropertyId = properties.getItems().get(0).getId();

        // Query for the list views (profiles) associated with the property.
        Profiles profiles = analytics.management().profiles()
            .list(firstAccountId, firstWebpropertyId).execute();

        if (profiles.getItems().isEmpty()) {
          System.err.println("No views (profiles) found");
        } else {
          // Return the first (view) profile associated with the property.
          profileId = profiles.getItems().get(0).getId();
        }
      }
    }
    return profileId;
  }

  private static GaData getResults(Analytics analytics, String profileId) throws IOException {
    // Query the Core Reporting API for the number of sessions
    // in the past seven days.
    return analytics.data().ga()
        .get("ga:" + profileId, "today", "today", "ga:sessions")
        .execute();
  }

  private static void printResults(GaData results) {
    // Parse the response from the Core Reporting API for
    // the profile name and number of sessions.
//      System.out.println("is results empty??  " + results.isEmpty());
//      System.out.println("what is inside results column headers?? " + results.getColumnHeaders());
//    if (results != null && !results.getRows().isEmpty()) {
      if (results != null && !results.isEmpty()) {
      System.out.println("View (Profile) Name: "
        + results.getProfileInfo().getProfileName());
//      System.out.println("Total Sessions: " + results.getRows().get(0).get(0));
        System.out.println("Total Sessions: " + results.getTotalsForAllResults().get("ga:sessions"));
    } else {
      System.out.println("No results found");
    }
  }
}
