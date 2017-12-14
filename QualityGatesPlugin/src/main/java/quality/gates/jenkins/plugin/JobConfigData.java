package quality.gates.jenkins.plugin;

import org.kohsuke.stapler.DataBoundConstructor;

public class JobConfigData {

    private String projectKey;
    private String sonarInstanceName;
    private boolean ignoreWarnings;

    @DataBoundConstructor
    public JobConfigData(String projectKey, String sonarInstanceName, boolean ignoreWarnings) {
        this.projectKey = projectKey;
        this.sonarInstanceName = sonarInstanceName;
        this.ignoreWarnings = ignoreWarnings;
    }

    public JobConfigData() {}

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getSonarInstanceName() {
        return sonarInstanceName;
    }

    public void setSonarInstanceName(String sonarInstanceName) {
        this.sonarInstanceName = sonarInstanceName;
    }
    
    public boolean getIgnoreWarnings() {
    	return ignoreWarnings;
    }
    
    public void setIgnoreWarnings(boolean ignoreWarnings) {
    	this.ignoreWarnings = ignoreWarnings;
    }

    @Override
    public String toString() {
        return "JobConfigData{" +
                "projectKey='" + projectKey + '\'' +
                ", sonarInstanceName='" + sonarInstanceName + '\'' +
                ", ignoreWarnings=" + ignoreWarnings +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobConfigData that = (JobConfigData) o;

        return projectKey.equals(that.projectKey) 
        	&& sonarInstanceName.equals(that.sonarInstanceName)
        	&& ignoreWarnings == that.ignoreWarnings;
    }

    @Override
    public int hashCode() {
        int result = projectKey.hashCode();
        result = 31 * result + sonarInstanceName.hashCode();
        return result;
    }
}
