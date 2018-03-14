package gov.ca.cwds.neutron.launch;

import gov.ca.cwds.data.std.ApiMarker;

/**
 * Intrinsic, immutable rocket specifications for a given rocket type. A rocket's specs define
 * built-in settings that drive flight path and behavior.
 * 
 * @author CWDS API Team
 */
public class RocketSpecification implements ApiMarker {

  private static final long serialVersionUID = 1L;

  private String initialLoadView;

  private String initialLoadMQT;

  private String prepLastChangeSQL;

  private boolean initialLoadUseJdbc;

  private boolean initialLoadUseTransformThread;

  private boolean viewNormalizer;

  private boolean deleteLimitedAccessRecords;

  private String nestedElementName;

  private boolean documentRoot;

  private boolean validateDocument;

  private String primaryKeyColumn;

  private String legacySourceTable;

  private Class<?> denormalizedClass;

  private Class<?> normalizedClass;

  public String getInitialLoadView() {
    return initialLoadView;
  }

  public void setInitialLoadView(String initialLoadView) {
    this.initialLoadView = initialLoadView;
  }

  public String getInitialLoadMQT() {
    return initialLoadMQT;
  }

  public void setInitialLoadMQT(String initialLoadMQT) {
    this.initialLoadMQT = initialLoadMQT;
  }

  public String getPrepLastChangeSQL() {
    return prepLastChangeSQL;
  }

  public void setPrepLastChangeSQL(String prepLastChangeSQL) {
    this.prepLastChangeSQL = prepLastChangeSQL;
  }

  public boolean isInitialLoadUseJdbc() {
    return initialLoadUseJdbc;
  }

  public void setInitialLoadUseJdbc(boolean initialLoadUseJdbc) {
    this.initialLoadUseJdbc = initialLoadUseJdbc;
  }

  public boolean isInitialLoadUseTransformThread() {
    return initialLoadUseTransformThread;
  }

  public void setInitialLoadUseTransformThread(boolean initialLoadUseTransformThread) {
    this.initialLoadUseTransformThread = initialLoadUseTransformThread;
  }

  public boolean isViewNormalizer() {
    return viewNormalizer;
  }

  public void setViewNormalizer(boolean viewNormalizer) {
    this.viewNormalizer = viewNormalizer;
  }

  public boolean isDeleteLimitedAccessRecords() {
    return deleteLimitedAccessRecords;
  }

  public void setDeleteLimitedAccessRecords(boolean deleteLimitedAccessRecords) {
    this.deleteLimitedAccessRecords = deleteLimitedAccessRecords;
  }

  public String getNestedElementName() {
    return nestedElementName;
  }

  public void setNestedElementName(String nestedElementName) {
    this.nestedElementName = nestedElementName;
  }

  public boolean isDocumentRoot() {
    return documentRoot;
  }

  public void setDocumentRoot(boolean documentRoot) {
    this.documentRoot = documentRoot;
  }

  public boolean isValidateDocument() {
    return validateDocument;
  }

  public void setValidateDocument(boolean validateDocument) {
    this.validateDocument = validateDocument;
  }

  public String getPrimaryKeyColumn() {
    return primaryKeyColumn;
  }

  public void setPrimaryKeyColumn(String primaryKeyColumn) {
    this.primaryKeyColumn = primaryKeyColumn;
  }

  public String getLegacySourceTable() {
    return legacySourceTable;
  }

  public void setLegacySourceTable(String legacySourceTable) {
    this.legacySourceTable = legacySourceTable;
  }

  public Class<?> getDenormalizedClass() {
    return denormalizedClass;
  }

  public void setDenormalizedClass(Class<?> denormalizedClass) {
    this.denormalizedClass = denormalizedClass;
  }

  public Class<?> getNormalizedClass() {
    return normalizedClass;
  }

  public void setNormalizedClass(Class<?> normalizedClass) {
    this.normalizedClass = normalizedClass;
  }

}
