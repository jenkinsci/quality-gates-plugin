package quality.gates.sonar.api;

import quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import quality.gates.jenkins.plugin.JobConfigData;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;


public class QualityGatesProviderTest {

    @InjectMocks
    QualityGatesProvider qualityGatesProvider;

    @Mock
    QualityGateResponseParser qualityGateResponseParser;

    @Mock
    SonarHttpRequester sonarHttpRequester;

    @Mock
    JobConfigData jobConfigData;

    @Mock
    GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @Mock
    SonarInstanceValidationService sonarInstanceValidationService;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        qualityGatesProvider = new QualityGatesProvider(qualityGateResponseParser, sonarHttpRequester, sonarInstanceValidationService);
    }

    @Test
    public void testGetAPIResultsForQualityGatesNewAPI() throws JSONException {
        QualityGatesStatus qualityGatesStatus = QualityGatesStatus.GREEN;
        doReturn("").when(globalConfigDataForSonarInstance).getName();
        doReturn("").when(globalConfigDataForSonarInstance).getUsername();
        doReturn("").when(globalConfigDataForSonarInstance).getPass();
        doReturn("").when(globalConfigDataForSonarInstance).getSonarUrl();
        doReturn("").when(jobConfigData).getProjectKey();
        doReturn("").when(sonarHttpRequester).getAPIInfo(any(JobConfigData.class), any(GlobalConfigDataForSonarInstance.class));
        doReturn(globalConfigDataForSonarInstance).when(sonarInstanceValidationService).validateData(globalConfigDataForSonarInstance);
        doReturn(qualityGatesStatus).when(qualityGateResponseParser).getQualityGateResultFromJSON(anyString(), Boolean.valueOf(anyString()));

        assertEquals(qualityGatesStatus, qualityGatesProvider.getAPIResultsForQualityGates(jobConfigData, globalConfigDataForSonarInstance));
    }
    
    @Test
    public void testGetAPIResultsForQualityGatesOldAPI() throws JSONException {
        QualityGatesStatus qualityGatesStatus = QualityGatesStatus.GREEN;
        doReturn("").when(globalConfigDataForSonarInstance).getName();
        doReturn("").when(globalConfigDataForSonarInstance).getUsername();
        doReturn("").when(globalConfigDataForSonarInstance).getPass();
        doReturn("").when(globalConfigDataForSonarInstance).getSonarUrl();
        doReturn("").when(jobConfigData).getProjectKey();
        doReturn("").when(sonarHttpRequester).getAPIInfo(any(JobConfigData.class), any(GlobalConfigDataForSonarInstance.class));
        doReturn(globalConfigDataForSonarInstance).when(sonarInstanceValidationService).validateData(globalConfigDataForSonarInstance);
        doReturn(qualityGatesStatus).when(qualityGateResponseParser).getQualityGateResultFromJSON(anyString(), Boolean.valueOf(anyString()));

        assertEquals(qualityGatesStatus, qualityGatesProvider.getAPIResultsForQualityGates(jobConfigData, globalConfigDataForSonarInstance));
    }

    @Test
    public void testGetAPIResultsForQualityGatesNotEqualStatuses() throws JSONException {
        QualityGatesStatus qualityGatesStatus = QualityGatesStatus.GREEN;
        doReturn("").when(jobConfigData).getProjectKey();
        doReturn("").when(sonarHttpRequester).getAPIInfo(any(JobConfigData.class), any(GlobalConfigDataForSonarInstance.class));
        doReturn(globalConfigDataForSonarInstance).when(sonarInstanceValidationService).validateData(globalConfigDataForSonarInstance);
        doReturn(QualityGatesStatus.RED).when(qualityGateResponseParser).getQualityGateResultFromJSON(anyString(), Boolean.valueOf(anyString()));

        assertNotEquals(qualityGatesStatus, qualityGatesProvider.getAPIResultsForQualityGates(jobConfigData, globalConfigDataForSonarInstance));
    }
}