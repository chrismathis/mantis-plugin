package hudson.plugins.mantis;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

/**
 * Parses changelog for Mantis issue IDs and updates Mantis issues.
 *
 * @author Seiji Sogabe
 */
public final class MantisIssueUpdater extends Recorder implements SimpleBuildStep {

    private final boolean keepNotePrivate;

    private final boolean recordChangelog;

    private SCM scm;

    @DataBoundConstructor
    public MantisIssueUpdater(final boolean keepNotePrivate, final boolean recordChangelog, SCM scm) {
        this.keepNotePrivate = keepNotePrivate;
        this.recordChangelog = recordChangelog;
        this.scm = scm;
    }

    public boolean isKeepNotePrivate() {
        return keepNotePrivate;
    }

    public boolean isRecordChangelog() {
        return recordChangelog;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public void perform(final Run<?, ?> run, FilePath workspace, final Launcher launcher, final TaskListener listener)
            throws InterruptedException, IOException {

        if (run instanceof MatrixRun) {
            return;
        } else if (run instanceof AbstractBuild<?, ?>) {
            AbstractBuild<?, ?> abstractBuild = (AbstractBuild<?, ?>) run;
            final Updater updater = new Updater(abstractBuild.getParent().getScm(), isKeepNotePrivate(),
                    isRecordChangelog());
            updater.perform(run, listener);
        } else {
            final Updater updater = new Updater(scm, isKeepNotePrivate(), isRecordChangelog());
            updater.perform(run, listener);
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(MantisIssueUpdater.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.MantisIssueUpdater_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/mantis/help.html";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public Publisher newInstance(final StaplerRequest req, final JSONObject formData) {
            return req.bindJSON(MantisIssueUpdater.class, formData);
        }
    }
}
