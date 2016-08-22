package quality.gates.jenkins.plugin;

import quality.gates.sonar.api.QualityGatesProvider;
import quality.gates.sonar.api.QualityGatesStatus;

import org.json.JSONException;

import hudson.model.Result;

public class BuildDecision {

    QualityGatesProvider qualityGatesProvider;

    public BuildDecision() {
        qualityGatesProvider = new QualityGatesProvider();
    }

    public BuildDecision(QualityGatesProvider qualityGatesProvider) {
        this.qualityGatesProvider = qualityGatesProvider;
    }

    public Result getStatus(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance, JobConfigData jobConfigData) throws QGException {
        try {
            QualityGatesStatus qualityGate = qualityGatesProvider.getAPIResultsForQualityGates(jobConfigData, globalConfigDataForSonarInstance);
            if (qualityGate == QualityGatesStatus.GREEN)
            	return Result.SUCCESS;
            if (jobConfigData.getIgnoreWarnings() && qualityGate == QualityGatesStatus.ORANGE)
            	return Result.UNSTABLE;
            return Result.FAILURE;
        } catch (JSONException e) {
            throw new QGException("Please check your credentials or your Project Key", e);
        }
    }
    
    public GlobalConfigDataForSonarInstance chooseSonarInstance (GlobalConfig globalConfig, JobConfigData jobConfigData) {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;
        if (globalConfig.fetchListOfGlobalConfigData().isEmpty()) {
            globalConfigDataForSonarInstance = noSonarInstance(jobConfigData);
        }
        else if (globalConfig.fetchListOfGlobalConfigData().size() == 1) {
            globalConfigDataForSonarInstance = singleSonarInstance(globalConfig, jobConfigData);
        }
        else {
            globalConfigDataForSonarInstance = multipleSonarInstances(jobConfigData.getSonarInstanceName(), globalConfig);
        }
        return globalConfigDataForSonarInstance;
    }

    public GlobalConfigDataForSonarInstance noSonarInstance(JobConfigData jobConfigData) {
        jobConfigData.setSonarInstanceName("");
        return new GlobalConfigDataForSonarInstance();
    }

    public GlobalConfigDataForSonarInstance singleSonarInstance(GlobalConfig globalConfig, JobConfigData jobConfigData) {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = globalConfig.fetchListOfGlobalConfigData().get(0);
        jobConfigData.setSonarInstanceName(globalConfigDataForSonarInstance.getName());
        return globalConfigDataForSonarInstance;
    }

    public GlobalConfigDataForSonarInstance multipleSonarInstances(String instanceName, GlobalConfig globalConfig) {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = globalConfig.getSonarInstanceByName(instanceName);
        if(globalConfigDataForSonarInstance != null) {
            return globalConfigDataForSonarInstance;
        }
        else
            return null;
    }
}