package quality.gates.jenkins.plugin;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

public class QGBuilder extends Builder implements SimpleBuildStep {

    private JobConfigData jobConfigData;
    private BuildDecision buildDecision;
    private JobConfigurationService jobConfigurationService;
    private JobExecutionService jobExecutionService;
	private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @DataBoundConstructor
    public QGBuilder(JobConfigData jobConfigData) {
        this.jobConfigData = jobConfigData;
        this.buildDecision = new BuildDecision();
        this.jobExecutionService = new JobExecutionService();
        this.jobConfigurationService = new JobConfigurationService();
        this.globalConfigDataForSonarInstance = null;
    }

    protected QGBuilder(JobConfigData jobConfigData, BuildDecision buildDecision, JobExecutionService jobExecutionService, JobConfigurationService jobConfigurationService, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        this.jobConfigData = jobConfigData;
        this.buildDecision = buildDecision;
        this.jobExecutionService = jobExecutionService;
        this.jobConfigurationService = jobConfigurationService;
        this.globalConfigDataForSonarInstance = globalConfigDataForSonarInstance;
    }

    public JobConfigData getJobConfigData() {
        return jobConfigData;
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
        try {
            JobConfigData checkedJobConfigData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
            Result buildStatus = buildDecision.getStatus(globalConfigDataForSonarInstance, checkedJobConfigData);
            boolean buildHasPassed = buildStatus == Result.SUCCESS || buildStatus == Result.UNSTABLE;
            if("".equals(jobConfigData.getSonarInstanceName()))
                listener.getLogger().println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
            listener.getLogger().println("Build-Step: Quality Gates plugin build passed: "
                    + String.valueOf(buildHasPassed).toUpperCase());
            if (buildStatus == Result.UNSTABLE) {
                build.setResult(Result.UNSTABLE);
            }
            build.setResult(determineBuildResult(buildHasPassed));
            return;
        }
        catch (QGException e){
            e.printStackTrace(listener.getLogger());
        }
        build.setResult(Result.FAILURE);
    }

    @Override
    public QGBuilderDescriptor getDescriptor() {
        return (QGBuilderDescriptor) super.getDescriptor();
    }

    private Result determineBuildResult(boolean gateResult) {
        if (gateResult) { return Result.SUCCESS; }
        return Result.FAILURE;
    }
}
