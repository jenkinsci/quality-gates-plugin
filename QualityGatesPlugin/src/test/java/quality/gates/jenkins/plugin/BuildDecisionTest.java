package quality.gates.jenkins.plugin;

import quality.gates.sonar.api.QualityGatesProvider;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import quality.gates.sonar.api.QualityGatesStatus;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class BuildDecisionTest {

    private BuildDecision buildDecision;

    @Mock
    QualityGatesProvider qualityGatesProvider;

    @Mock
    QualityGatesStatus qualityGatesStatus;

    @Mock
    JobConfigData jobConfigData;

    @Mock
    GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @Mock
    GlobalConfig globalConfig;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        buildDecision = new BuildDecision(qualityGatesProvider);
    }

    @Test
    public void testGetStatusTrue() throws JSONException {
        doReturn(qualityGatesStatus).when(qualityGatesProvider).getAPIResultsForQualityGates(any(JobConfigData.class), any(GlobalConfigDataForSonarInstance.class));
        doReturn(true).when(qualityGatesStatus).hasStatusGreen();
        assertTrue(buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData));
    }

    @Test
    public void testGetStatusFalse() throws JSONException {
        doReturn(qualityGatesStatus).when(qualityGatesProvider).getAPIResultsForQualityGates(any(JobConfigData.class), any(GlobalConfigDataForSonarInstance.class));
        doReturn(false).when(qualityGatesStatus).hasStatusGreen();
        assertFalse(buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData));
    }

    @Test(expected = QGException.class)
    public void testGetStatusThrowJSONException() throws JSONException {
        JSONException jsonException = Mockito.mock(JSONException.class);
        when(qualityGatesProvider.getAPIResultsForQualityGates(any(JobConfigData.class), any(GlobalConfigDataForSonarInstance.class))).thenThrow(jsonException);
        buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData);
    }

    @Test
    public void testChooseSonarInstanceIfListIsEmpty() {
        String emptyString = "";
        List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchListOfGlobalConfigData();
        GlobalConfigDataForSonarInstance returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertTrue(returnedInstance.getName().equals(emptyString));
        assertTrue(returnedInstance.getPass().equals(emptyString));
        assertTrue(returnedInstance.getSonarUrl().equals(emptyString));
        assertTrue(returnedInstance.getUsername().equals(emptyString));
    }

    @Test
    public void testChooseSonarInstanceIfListHasOneInstance() {
        List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        GlobalConfigDataForSonarInstance singleInstance = new GlobalConfigDataForSonarInstance("TestName", "TestUrl", "TestUsername", "TestPass");
        globalConfigDataForSonarInstanceList.add(singleInstance);
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchListOfGlobalConfigData();
        GlobalConfigDataForSonarInstance returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertTrue(returnedInstance.getName().equals("TestName"));
        assertTrue(returnedInstance.getSonarUrl().equals("TestUrl"));
        assertTrue(returnedInstance.getUsername().equals("TestUsername"));
    }

    @Test
    public void testChooseSonarInstanceIfListHasMultipleInstancesAndNameMatches() {
        List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        GlobalConfigDataForSonarInstance firstInstance = new GlobalConfigDataForSonarInstance("TestName", "TestUrl", "TestUsername", "TestPass");
        GlobalConfigDataForSonarInstance secondInstance = new GlobalConfigDataForSonarInstance("TestName1", "TestUrl1", "TestUsername1", "TestPass1");
        globalConfigDataForSonarInstanceList.add(firstInstance);
        globalConfigDataForSonarInstanceList.add(secondInstance);
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchListOfGlobalConfigData();
        doReturn(secondInstance).when(globalConfig).getSonarInstanceByName("TestName1");
        doReturn("TestName1").when(jobConfigData).getSonarInstanceName();
        GlobalConfigDataForSonarInstance returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertTrue(returnedInstance.getName().equals("TestName1"));
        assertTrue(returnedInstance.getSonarUrl().equals("TestUrl1"));
        assertTrue(returnedInstance.getUsername().equals("TestUsername1"));
    }

    @Test
    public void testChooseSonarInstanceIfListHasMultipleInstancesAndNameDoesNotMatch() {
        List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        GlobalConfigDataForSonarInstance firstInstance = new GlobalConfigDataForSonarInstance("TestName", "TestUrl", "TestUsername", "TestPass");
        GlobalConfigDataForSonarInstance secondInstance = new GlobalConfigDataForSonarInstance("TestName1", "TestUrl1", "TestUsername1", "TestPass1");
        globalConfigDataForSonarInstanceList.add(firstInstance);
        globalConfigDataForSonarInstanceList.add(secondInstance);
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchListOfGlobalConfigData();
        doReturn(null).when(globalConfig).getSonarInstanceByName("RandomName");
        jobConfigData.setSonarInstanceName("RandomName");
        GlobalConfigDataForSonarInstance returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertTrue(returnedInstance == null);
    }
}