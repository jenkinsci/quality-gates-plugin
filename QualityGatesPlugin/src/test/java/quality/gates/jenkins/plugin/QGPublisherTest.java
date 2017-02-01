package quality.gates.jenkins.plugin;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class QGPublisherTest {

    public static final String POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED = "PostBuild-Step: Quality Gates plugin build passed: ";
    private QGPublisher publisher;

    @Mock
    private BuildDecision buildDecision;

    @Mock
    private JobConfigData jobConfigData;

    @Mock
    private JobExecutionService jobExecutionService;

    @Mock
    private BuildListener buildListener;

    @Mock
    private PrintStream printStream;

    @Mock
    private PrintWriter printWriter;

    @Mock
    private AbstractBuild abstractBuild;

    @Mock
    private Launcher launcher;

    @Mock
    private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @Mock
    JobConfigurationService jobConfigurationService;

    @Mock
    List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstances;

    @Mock
    private BuildListener listener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        publisher = new QGPublisher(jobConfigData, buildDecision, jobExecutionService, jobConfigurationService, globalConfigDataForSonarInstance);
        doReturn(printStream).when(buildListener).getLogger();
        doReturn(printWriter).when(buildListener).error(anyString(), anyObject());
    }

    @Test
    public void testPrebuildShouldFail() {
        doReturn(null).when(buildDecision).chooseSonarInstance(any(GlobalConfig.class), any(JobConfigData.class));
        doReturn("TestInstanceName").when(jobConfigData).getSonarInstanceName();
        assertFalse(publisher.prebuild(abstractBuild, buildListener));
        verify(buildListener).error(JobExecutionService.GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR, "TestInstanceName");
    }

    @Test
    public void testPrebuildShouldPassBecauseGlobalConfigDataIsFound() {
        doReturn(globalConfigDataForSonarInstance).when(buildDecision).chooseSonarInstance(any(GlobalConfig.class), any(JobConfigData.class));
        assertTrue(publisher.prebuild(abstractBuild, buildListener));
        verifyZeroInteractions(buildListener);
    }

    @Test
    public void testPerformBuildResultFail() {
        setBuildResult(Result.FAILURE);
        buildDecisionShouldBe(false);
        assertFalse(publisher.perform(abstractBuild, launcher, buildListener));
        verifyZeroInteractions(buildDecision);
    }

    @Test
    public void testPerformBuildResultSuccessWithWarningForDefaultInstance() throws QGException {
        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(true);
        doReturn("").when(jobConfigData).getSonarInstanceName();
        doReturn(true).when(buildDecision).getStatus(any(GlobalConfigDataForSonarInstance.class), any(JobConfigData.class), any(BuildListener.class));
        assertTrue(publisher.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(2)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "TRUE");
    }

    @Test
    public void testPerformBuildResultSuccessWithNoWarning() throws QGException {
        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(true);
        doReturn("SomeName").when(globalConfigDataForSonarInstance).getName();
        doReturn("someName").when(jobConfigData).getSonarInstanceName();
        doReturn(true).when(buildDecision).getStatus(any(GlobalConfigDataForSonarInstance.class), any(JobConfigData.class), any(BuildListener.class));
        assertTrue(publisher.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(1)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "TRUE");
    }

    @Test
    public void testPerformBuildResultFailWithWarningForDefaultInstance() throws QGException {
        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(false);
        when(jobConfigData.getSonarInstanceName()).thenReturn("");
        assertFalse(publisher.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(2)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "FALSE");
    }

    @Test
    public void testPerformBuildResultFailWithNoWarning() throws QGException {
        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(false);
        doReturn("SomeName").when(globalConfigDataForSonarInstance).getName();
        assertFalse(publisher.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(1)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "FALSE");
    }

    @Test
    public void testPerformThrowsException() throws QGException {
        setBuildResult(Result.SUCCESS);
        QGException exception = mock(QGException.class);
        doThrow(exception).when(buildDecision).getStatus(any(GlobalConfigDataForSonarInstance.class), any(JobConfigData.class), any(BuildListener.class));
        assertFalse(publisher.perform(abstractBuild, launcher, buildListener));
        verify(exception, times(1)).printStackTrace(printStream);
    }

    private void buildDecisionShouldBe(boolean toBeReturned) throws QGException {
        when(buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData, listener)).thenReturn(toBeReturned);
    }

    private void setBuildResult(Result result) {
        when(abstractBuild.getResult()).thenReturn(result);
    }
}
