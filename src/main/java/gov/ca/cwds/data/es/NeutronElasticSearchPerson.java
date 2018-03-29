package gov.ca.cwds.data.es;

import java.util.List;

public class NeutronElasticSearchPerson extends ElasticSearchPerson {

  private static final long serialVersionUID = 1L;

  private boolean oldAddressType;

  public NeutronElasticSearchPerson() {
    super();
  }

  public NeutronElasticSearchPerson(String id, String firstName, String lastName, String middleName,
      String nameSuffix, String gender, String birthDate, String ssn, String sourceType,
      String sourceJson, String highlight, List<ElasticSearchPersonAddress> addresses,
      List<ElasticSearchPersonPhone> phones, List<ElasticSearchPersonLanguage> languages,
      List<ElasticSearchPersonScreening> screenings) {
    super(id, firstName, lastName, middleName, nameSuffix, gender, birthDate, ssn, sourceType,
        sourceJson, highlight, addresses, phones, languages, screenings);
    // TODO Auto-generated constructor stub
  }

}
