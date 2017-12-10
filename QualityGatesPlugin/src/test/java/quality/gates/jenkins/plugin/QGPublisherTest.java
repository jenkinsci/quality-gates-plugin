package quality.gates.jenkins.plugin;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class QGPublisherTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    public static final String POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED = "PostBuild-Step: Quality Gates plugin build passed: ";
    public static final String POST_BUILD_STEP_QUALITY_GATES_PLUGIN_PREVIOUS_STEP_FAILED = "Previous steps failed the build.\nResult is: ";
    private QGPublisher publisher;
    private FilePath filePath;

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
    private Run<?, ?> abstractBuild;

    @Mock
    private Launcher launcher;

    @Mock
    private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @Mock
    JobConfigurationService jobConfigurationService;

    @Mock
    List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstances;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        publisher = new QGPublisher(jobConfigData, buildDecision, jobExecutionService, jobConfigurationService, globalConfigDataForSonarInstance);
        File dummyFile = tempFolder.newFolder();
        filePath = new FilePath(dummyFile);
        doReturn(printStream).when(buildListener).getLogger();
        doReturn(printWriter).when(buildListener).error(anyString(), anyObject());
    }

    @Test
    public void testPrebuildShouldFail() {
        doReturn(null).when(buildDecision).chooseSonarInstance(any(GlobalConfig.class), any(JobConfigData.class));
        doReturn("TestInstanceName").when(jobConfigData).getSonarInstanceName();
        assertFalse(publisher.retrieveGlobalConfig(abstractBuild, buildListener));
        verify(buildListener).error(JobExecutionService.GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR, "TestInstanceName");
    }

    @Test
    public void testPrebuildShouldPassBecauseGlobalConfigDataIsFound() {
        doReturn(globalConfigDataForSonarInstance).when(buildDecision).chooseSonarInstance(any(GlobalConfig.class), any(JobConfigData.class));
        assertTrue(publisher.retrieveGlobalConfig(abstractBuild, buildListener));
        verify(buildListener, never()).error(JobExecutionService.GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR, "TestInstanceName");
    }

    @Test
    public void testPerformBuildResultFail() throws IOException, InterruptedException {
        QGPublisher publisherSpy = Mockito.spy(publisher);
        Mockito.doReturn(true).when(publisherSpy).retrieveGlobalConfig(abstractBuild, buildListener);

        setBuildResult(Result.FAILURE);
        buildDecisionShouldBe(Result.FAILURE);
        publisherSpy.perform(abstractBuild, filePath, launcher, buildListener);
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_PREVIOUS_STEP_FAILED + "FAILURE");
        verifyZeroInteractions(buildDecision);
    }

    @Test
    public void testPerformBuildResultSuccessWithWarningForDefaultInstance() throws QGException, IOException, InterruptedException {
        QGPublisher publisherSpy = Mockito.spy(publisher);
        Mockito.doReturn(true).when(publisherSpy).retrieveGlobalConfig(abstractBuild, buildListener);

        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(Result.SUCCESS);
        doReturn("").when(jobConfigData).getSonarInstanceName();
        publisherSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(buildListener, times(2)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "TRUE");
    }

    @Test
    public void testPerformBuildResultSuccessWithNoWarning() throws QGException, IOException, InterruptedException {
        QGPublisher publisherSpy = Mockito.spy(publisher);
        Mockito.doReturn(true).when(publisherSpy).retrieveGlobalConfig(abstractBuild, buildListener);

        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(Result.SUCCESS);
        doReturn("SomeName").when(globalConfigDataForSonarInstance).getName();
        doReturn("someName").when(jobConfigData).getSonarInstanceName();
        publisherSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(buildListener, times(1)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "TRUE");
    }

    @Test
    public void testPerformBuildResultUnstableWithWarningForDefaultInstance() throws QGException, IOException, InterruptedException {
        QGPublisher publisherSpy = Mockito.spy(publisher);
        Mockito.doReturn(true).when(publisherSpy).retrieveGlobalConfig(abstractBuild, buildListener);

        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(Result.UNSTABLE);
        doReturn("").when(jobConfigData).getSonarInstanceName();
        doReturn(true).when(jobConfigData).getIgnoreWarnings();
        publisherSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(buildListener, times(2)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "TRUE");
    }

    @Test
    public void testPerformBuildResultUnstableWithNoWarning() throws QGException, IOException, InterruptedException {
        QGPublisher publisherSpy = Mockito.spy(publisher);
        Mockito.doReturn(true).when(publisherSpy).retrieveGlobalConfig(abstractBuild, buildListener);

        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(Result.UNSTABLE);
        doReturn("SomeName").when(globalConfigDataForSonarInstance).getName();
        doReturn("someName").when(jobConfigData).getSonarInstanceName();
        doReturn(true).when(jobConfigData).getIgnoreWarnings();
        publisherSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(buildListener, times(1)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "TRUE");
    }

    @Test
    public void testPerformBuildResultFailWithWarningForDefaultInstance() throws QGException, IOException, InterruptedException {
        QGPublisher publisherSpy = Mockito.spy(publisher);
        Mockito.doReturn(true).when(publisherSpy).retrieveGlobalConfig(abstractBuild, buildListener);

        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(Result.FAILURE);
        when(jobConfigData.getSonarInstanceName()).thenReturn("");
        publisherSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(buildListener, times(2)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "FALSE");
    }

    @Test
    public void testPerformBuildResultFailWithNoWarning() throws QGException, IOException, InterruptedException {
        QGPublisher publisherSpy = Mockito.spy(publisher);
        Mockito.doReturn(true).when(publisherSpy).retrieveGlobalConfig(abstractBuild, buildListener);

        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(Result.FAILURE);
        doReturn("SomeName").when(globalConfigDataForSonarInstance).getName();
        publisherSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(buildListener, times(1)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "FALSE");
    }

    @Test
    public void testPerformThrowsException() throws QGException, IOException, InterruptedException {
        QGPublisher publisherSpy = Mockito.spy(publisher);
        Mockito.doReturn(true).when(publisherSpy).retrieveGlobalConfig(abstractBuild, buildListener);

        setBuildResult(Result.SUCCESS);
        QGException exception = mock(QGException.class);
        doThrow(exception).when(buildDecision).getStatus(any(GlobalConfigDataForSonarInstance.class), any(JobConfigData.class));
        publisherSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(exception, times(1)).printStackTrace(printStream);
    }
    
    private void buildDecisionShouldBe(Result toBeReturned) throws QGException {
        doReturn(toBeReturned).when(buildDecision).getStatus(any(GlobalConfigDataForSonarInstance.class), any(JobConfigData.class));
    }

    private void setBuildResult(Result result) {
        when(abstractBuild.getResult()).thenReturn(result);
    }
}
