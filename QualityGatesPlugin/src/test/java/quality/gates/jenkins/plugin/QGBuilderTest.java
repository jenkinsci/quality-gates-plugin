package quality.gates.jenkins.plugin;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class QGBuilderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    public static final String BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED = "Build-Step: Quality Gates plugin build passed: ";
    private QGBuilder builder;
    private FilePath filePath;

    @Mock
    private BuildDecision buildDecision;

    @Mock
    private JobConfigData jobConfigData;

    @Mock
    private JobExecutionService jobExecutionService;

    @Mock
    private TaskListener buildListener;

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
    private JobConfigurationService jobConfigurationService;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        builder = new QGBuilder(jobConfigData, buildDecision, jobExecutionService, jobConfigurationService, globalConfigDataForSonarInstance);
        File dummyFile = tempFolder.newFolder();
        filePath = new FilePath(dummyFile);
        doReturn(printStream).when(buildListener).getLogger();
        doReturn(printWriter).when(buildListener).error(anyString(), anyObject());
    }

    @Test
    public void testPrebuildShouldFailGlobalConfigDataInstanceIsNull() {
        doReturn(null).when(buildDecision).chooseSonarInstance(any(GlobalConfig.class), any(JobConfigData.class));
        doReturn("TestInstanceName").when(jobConfigData).getSonarInstanceName();
        assertFalse(builder.prebuild(abstractBuild, buildListener));
        verify(buildListener).error(JobExecutionService.GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR, "TestInstanceName");
    }

    @Test
    public void testPrebuildShouldPassBecauseGlobalConfigDataIsFound() {
        doReturn(globalConfigDataForSonarInstance).when(buildDecision).chooseSonarInstance(any(GlobalConfig.class), any(JobConfigData.class));
        assertTrue(builder.prebuild(abstractBuild, buildListener));
        verify(buildListener, never()).error(JobExecutionService.GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR, "TestInstanceName");
    }

    @Test
    public void testPerformShouldPassWithNoWarning() throws QGException, IOException, InterruptedException {
        QGBuilder builderSpy = Mockito.spy(builder);
        Mockito.doReturn(true).when(builderSpy).prebuild(abstractBuild, buildListener);

        String stringWithName = "Name";
        buildDecisionShouldBe(Result.SUCCESS);
        when(jobConfigData.getSonarInstanceName()).thenReturn(stringWithName);
        builderSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(buildListener, times(1)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "TRUE");
    }

    @Test
    public void testPerformShouldPassWithWarning() throws QGException, IOException, InterruptedException {
        QGBuilder builderSpy = Mockito.spy(builder);
        Mockito.doReturn(true).when(builderSpy).prebuild(abstractBuild, buildListener);

        String emptyString = "";
        buildDecisionShouldBe(Result.SUCCESS);
        when(jobConfigData.getSonarInstanceName()).thenReturn(emptyString);
        builderSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(buildListener, times(2)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
        verify(stream).println(BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "TRUE");
    }

    @Test
    public void testPerformShouldPassUnstableWithNoWarning() throws QGException, IOException, InterruptedException {
        QGBuilder builderSpy = Mockito.spy(builder);
        Mockito.doReturn(true).when(builderSpy).prebuild(abstractBuild, buildListener);

        String stringWithName = "Name";
        buildDecisionShouldBe(Result.UNSTABLE);
        when(jobConfigData.getSonarInstanceName()).thenReturn(stringWithName);
        builderSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(buildListener, times(1)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "TRUE");
    }

    @Test
    public void testPerformShouldPassUnstableWithWarning() throws QGException, IOException, InterruptedException {
        QGBuilder builderSpy = Mockito.spy(builder);
        Mockito.doReturn(true).when(builderSpy).prebuild(abstractBuild, buildListener);

        String emptyString = "";
        buildDecisionShouldBe(Result.UNSTABLE);
        when(jobConfigData.getSonarInstanceName()).thenReturn(emptyString);
        when(jobConfigData.getIgnoreWarnings()).thenReturn(true);
        builderSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(buildListener, times(2)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
        verify(stream).println(BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "TRUE");
    }

    @Test
    public void testPerformShouldFailWithNoWarning() throws QGException, IOException, InterruptedException {
        QGBuilder builderSpy = Mockito.spy(builder);
        Mockito.doReturn(true).when(builderSpy).prebuild(abstractBuild, buildListener);

        String stringWithName = "Name";
        buildDecisionShouldBe(Result.FAILURE);
        when(jobConfigData.getSonarInstanceName()).thenReturn(stringWithName);
        when(jobConfigData.getIgnoreWarnings()).thenReturn(true);
        builderSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(buildListener, times(1)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "FALSE");
    }

    @Test
    public void testPerformShouldFailWithWarning() throws QGException, IOException, InterruptedException {
        QGBuilder builderSpy = Mockito.spy(builder);
        Mockito.doReturn(true).when(builderSpy).prebuild(abstractBuild, buildListener);

        String emptyString = "";
        buildDecisionShouldBe(Result.FAILURE);
        when(jobConfigData.getSonarInstanceName()).thenReturn(emptyString);
        builderSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(buildListener, times(2)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
        verify(stream).println(BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "FALSE");
    }

    @Test
    public void testPerformThrowsException() throws QGException, IOException, InterruptedException {
        QGBuilder builderSpy = Mockito.spy(builder);
        Mockito.doReturn(true).when(builderSpy).prebuild(abstractBuild, buildListener);

        QGException exception = mock(QGException.class);
        when(buildDecision.getStatus(any(GlobalConfigDataForSonarInstance.class), any(JobConfigData.class))).thenThrow(exception);
        builderSpy.perform(abstractBuild, filePath, launcher, buildListener);
        verify(exception, times(1)).printStackTrace(printStream);
    }

    private void buildDecisionShouldBe(Result toBeReturned) throws QGException {
        doReturn(toBeReturned).when(buildDecision).getStatus(any(GlobalConfigDataForSonarInstance.class), any(JobConfigData.class));
    }

}
