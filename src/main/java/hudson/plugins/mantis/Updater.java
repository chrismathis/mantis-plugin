package hudson.plugins.mantis;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.mantis.changeset.ChangeSet;
import hudson.plugins.mantis.changeset.ChangeSetFactory;
import hudson.plugins.mantis.model.MantisIssue;
import hudson.scm.ChangeLogSet;
import hudson.scm.SCM;
import hudson.scm.ChangeLogSet.Entry;

/**
 * Mantis update Logic.
 *
 * @author Seiji Sogabe
 */
final class Updater {

    private static final String CRLF = System.getProperty("line.separator");

    private SCM scm;
    private boolean keepNotePrivate;
    private boolean recordChangeNote;

    Updater(final SCM scm, boolean keepNotePrivate, boolean recordChangeNote) {
        this.scm = scm;
        this.keepNotePrivate = keepNotePrivate;
        this.recordChangeNote = recordChangeNote;
    }

    boolean perform(final Run<?, ?> build, final TaskListener listener) {

        final PrintStream logger = listener.getLogger();

        final MantisSite site = MantisSite.get(build.getParent());
        if (site == null) {
            Utility.log(logger, Messages.Updater_NoMantisSite());
            build.setResult(Result.FAILURE);
            return true;
        }

        final String rootUrl = Hudson.getInstance().getRootUrl();
        if (rootUrl == null) {
            Utility.log(logger, Messages.Updater_NoHudsonUrl());
            build.setResult(Result.FAILURE);
            return true;
        }

        final List<ChangeSet> chnageSets = findChangeSets(build);
        if (chnageSets.isEmpty()) {
            Utility.log(logger, Messages.Updater_NoIssuesFound());
            return true;
        }

        Result result = build.getResult();
        boolean update = true;
        if (result != null) {
            update = !result.isWorseThan(Result.UNSTABLE);
        }
        if (!update) {
            // Keep id for next build
            Utility.log(logger, Messages.Updater_KeepMantisIssueIdsForNextBuild());
            build.addAction(new MantisCarryOverChangeSetAction(chnageSets));
        }

        final List<MantisIssue> issues = new ArrayList<MantisIssue>();
        for (final ChangeSet changeSet : chnageSets) {
            try {
                final MantisIssue issue = site.getIssue(changeSet.getId());
                if (update) {
                    final String text = createUpdateText(build, changeSet, rootUrl);
                    site.updateIssue(changeSet.getId(), text, keepNotePrivate);
                    Utility.log(logger, Messages.Updater_Updating(changeSet.getId()));
                }
                issues.add(issue);
            } catch (final MantisHandlingException e) {
                Utility.log(logger, Messages.Updater_FailedToAddNote(changeSet, e.getMessage()));
                LOGGER.log(Level.WARNING, Messages.Updater_FailedToAddNote_StarckTrace(changeSet), e);
            }
        }

        // build is not null, so mpp is not null
        MantisProjectProperty mpp = MantisProjectProperty.get(build);
        build.getActions().add(new MantisBuildAction(mpp.getRegexpPattern(), issues.toArray(new MantisIssue[0])));

        return true;
    }

    private String createUpdateText(final Run<?, ?> build, final ChangeSet changeSet, final String rootUrl) {
        final String prjName = build.getParent().getName();
        final int prjNumber = build.getNumber();
        final String url = rootUrl + build.getUrl();

        final StringBuilder text = new StringBuilder();
        text.append(Messages.Updater_IssueIntegrated(prjName, prjNumber, url));
        text.append(CRLF).append(CRLF);

        if (recordChangeNote) {
            text.append(changeSet.createChangeLog());
        }
        return text.toString();
    }

    private List<ChangeSet> findChangeSets(final Run<?, ?> build) {
        final List<ChangeSet> chnageSets = new ArrayList<ChangeSet>();

        final Run<?, ?> prev = build.getPreviousBuild();
        if (prev != null) {
            final MantisCarryOverChangeSetAction changeSetAction = prev.getAction(MantisCarryOverChangeSetAction.class);
            if (changeSetAction != null) {
                for (final ChangeSet changeSet : changeSetAction.getChangeSets()) {
                    chnageSets.add(changeSet);
                }
            }
        }

        chnageSets.addAll(findChangeSetsFromSCM(build));

        return chnageSets;
    }

    private List<ChangeSet> findChangeSetsFromSCM(final Run<?, ?> build) {
        final List<ChangeSet> changeSets = new ArrayList<ChangeSet>();

        MantisProjectProperty mpp = MantisProjectProperty.get(build);
        final Pattern pattern = mpp.getRegexpPattern();

        for (ChangeLogSet<? extends Entry> set : RunScmChangeExtractor.getChanges(build)) {
            for (Entry change : set) {
                final Matcher matcher = pattern.matcher(change.getMsg());
                while (matcher.find()) {
                    int id;
                    try {
                        id = Integer.parseInt(matcher.group(1));
                    } catch (final NumberFormatException e) {
                        // if id is not number, skip
                        LOGGER.log(Level.WARNING, Messages.Updater_IllegalMantisId(matcher.group(1)));
                        continue;
                    }
                    changeSets.add(ChangeSetFactory.newInstance(id, scm, change));
                }
            }
        }

        return changeSets;
    }

    private static final Logger LOGGER = Logger.getLogger(Updater.class.getName());
}
