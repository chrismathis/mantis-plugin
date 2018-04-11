package hudson.plugins.mantis.changeset;

import java.util.Collection;

import hudson.scm.RepositoryBrowser;
import hudson.scm.ChangeLogSet.Entry;

/**
 * ChangeSet of Git.
 *
 * @author Seiji Sogabe
 * @since 0.7.1
 */
public class GitChangeSet extends AbstractChangeSet<hudson.plugins.git.GitChangeSet> {

    private static final long serialVersionUID = 1L;

    public GitChangeSet(final int id, final RepositoryBrowser<Entry> repoBrowser,
            final hudson.plugins.git.GitChangeSet entry) {
        super(id, repoBrowser, entry);
    }

    @Override
    public String createChangeLog() {
        final StringBuilder text = new StringBuilder();
        text.append(Messages.ChangeSet_Revision(getRevision(), getChangeSetLink()));
        text.append(CRLF);
        text.append(Messages.ChangeSet_Author(getAuthor()));
        text.append(CRLF);
        text.append(Messages.ChangeSet_Log(getMsg()));
        text.append(CRLF);
        text.append(Messages.ChangeSet_ChangedPaths_Header());
        text.append(CRLF);
        for (final String path : getAffectedPaths()) {
            text.append(Messages.ChangeSet_ChangedPaths_Path("", path));
            text.append(CRLF);
        }
        text.append(CRLF);
        return text.toString();
    }

    protected String getRevision() {
        return String.valueOf(entry.getId());
    }

    private Collection<String> getAffectedPaths() {
        return entry.getAffectedPaths();
    }

}
