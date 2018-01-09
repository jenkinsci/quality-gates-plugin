package quality.gates.sonar.api;


import quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import quality.gates.jenkins.plugin.JobConfigData;
import quality.gates.jenkins.plugin.QGException;
import org.json.JSONException;

public class QualityGatesProvider {

    private QualityGateResponseParser qualityGateResponseParser;
    private SonarHttpRequester sonarHttpRequester;
    private SonarInstanceValidationService sonarInstanceValidationService;

    public QualityGatesProvider() {
        this.qualityGateResponseParser = new QualityGateResponseParser();
        this.sonarHttpRequester = new SonarHttpRequester();
        this.sonarInstanceValidationService = new SonarInstanceValidationService();
    }

    public QualityGatesProvider(QualityGateResponseParser qualityGateResponseParser, SonarHttpRequester sonarHttpRequester, SonarInstanceValidationService sonarInstanceValidationService) {
        this.qualityGateResponseParser = qualityGateResponseParser;
        this.sonarHttpRequester = sonarHttpRequester;
        this.sonarInstanceValidationService = sonarInstanceValidationService;
    }

    public QualityGatesStatus getAPIResultsForQualityGates(JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) throws JSONException {
        GlobalConfigDataForSonarInstance validatedData = sonarInstanceValidationService.validateData(globalConfigDataForSonarInstance);
        
        Float sonarVersion = sonarHttpRequester.getSonarQubeVersion(globalConfigDataForSonarInstance);

        boolean useNewAPI = false;
        if(sonarVersion >= 6.3) {
        	useNewAPI = true;
        }
        
        String requesterResult = getRequesterResult(jobConfigData, validatedData, useNewAPI);
        return qualityGateResponseParser.getQualityGateResultFromJSON(requesterResult, useNewAPI);
    }

    public String getRequesterResult(JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance, boolean useNewAPI) throws QGException {
        
    	if(useNewAPI) {
    		return sonarHttpRequester.getAPIInfoNew(jobConfigData, globalConfigDataForSonarInstance);
    	} else {
    		return sonarHttpRequester.getAPIInfo(jobConfigData, globalConfigDataForSonarInstance);
    	}
    }
}
