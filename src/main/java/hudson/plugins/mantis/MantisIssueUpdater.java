package hudson.plugins.mantis;

import java.io.IOException;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
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

    @DataBoundConstructor
    public MantisIssueUpdater(final boolean keepNotePrivate, final boolean recordChangelog) {
        this.keepNotePrivate = keepNotePrivate;
        this.recordChangelog = recordChangelog;
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
        } else {
            try {
                @SuppressWarnings("unchecked")
                List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeSets = (List<ChangeLogSet<? extends Entry>>) run
                        .getClass().getMethod("getChangeSets").invoke(run);
                final Updater updater = new Updater(changeSets, isKeepNotePrivate(), isRecordChangelog());
                updater.perform(run, listener);
            } catch (Exception e) {
                throw new IOException(e);
            }
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
