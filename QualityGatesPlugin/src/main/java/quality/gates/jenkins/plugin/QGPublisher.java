
package quality.gates.jenkins.plugin;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

public class QGPublisher extends Recorder implements SimpleBuildStep {

    private JobConfigData jobConfigData;
    private BuildDecision buildDecision;
    private JobConfigurationService jobConfigurationService;
    private JobExecutionService jobExecutionService;
    private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @DataBoundConstructor
    public QGPublisher(JobConfigData jobConfigData) {
        this.jobConfigData = jobConfigData;
        this.buildDecision = new BuildDecision();
        this.jobExecutionService = new JobExecutionService();
        this.jobConfigurationService = new JobConfigurationService();
        this.globalConfigDataForSonarInstance = null;
    }

    public QGPublisher(JobConfigData jobConfigData, BuildDecision buildDecision, JobExecutionService jobExecutionService,JobConfigurationService jobConfigurationService, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        this.jobConfigData = jobConfigData;
        this.buildDecision = buildDecision;
        this.jobConfigurationService = jobConfigurationService;
        this.jobExecutionService = jobExecutionService;
        this.globalConfigDataForSonarInstance = globalConfigDataForSonarInstance;
    }

    public JobConfigData getJobConfigData() {
        return jobConfigData;
    }

    @Override
    public QGPublisherDescriptor getDescriptor() {
        return (QGPublisherDescriptor) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    private void retrieveGlobalConfig(Run<?, ?> build, TaskListener listener) {
        globalConfigDataForSonarInstance = buildDecision.chooseSonarInstance(jobExecutionService.getGlobalConfigData(), jobConfigData);
        if(globalConfigDataForSonarInstance == null) {
            listener.error(JobExecutionService.GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR, jobConfigData.getSonarInstanceName());
            build.setResult(Result.FAILURE);
        }
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        retrieveGlobalConfig(build, listener);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace(listener.getLogger());
        }
        Result result = build.getResult();
        if (Result.SUCCESS != result && null != result) {
            listener.getLogger().println("Previous steps failed the build.\nResult is: " + result);
            return;
        }
        try {
            JobConfigData checkedJobConfigData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
            Result buildStatus = buildDecision.getStatus(globalConfigDataForSonarInstance, checkedJobConfigData);
            boolean buildHasPassed = buildStatus == Result.SUCCESS || buildStatus == Result.UNSTABLE;
            if("".equals(jobConfigData.getSonarInstanceName()))
                listener.getLogger().println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
            listener.getLogger().println("PostBuild-Step: Quality Gates plugin build passed: "
                    + String.valueOf(buildHasPassed).toUpperCase());
            if (buildStatus == Result.UNSTABLE) {
                build.setResult(Result.UNSTABLE);
                return;
            }

            build.setResult(determineBuildResult(buildHasPassed));
            return;
        } catch (QGException e) {
            e.printStackTrace(listener.getLogger());
        }

        build.setResult(Result.FAILURE);
    }

    private Result determineBuildResult(boolean gateResult) {
        if (gateResult) { return Result.SUCCESS; }
        return Result.FAILURE;
    }
}