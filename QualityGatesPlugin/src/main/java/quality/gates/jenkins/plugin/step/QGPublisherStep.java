package quality.gates.jenkins.plugin.step;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class QGPublisherStep extends AbstractStepImpl {

    @DataBoundConstructor
    public QGPublisherStep() {}

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() { super(QGPublisherStepExecution.class); }

        @Override
        public String getFunctionName() {
            return "qualityGate";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Publish Sonarqube Quality Gates";
        }
    }
}
