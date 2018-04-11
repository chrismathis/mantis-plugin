package hudson.plugins.mantis.changeset;

import java.util.ArrayList;
import java.util.List;

import hudson.scm.RepositoryBrowser;
import hudson.scm.ChangeLogSet.Entry;

/**
 * Default ChangeSet
 *
 * @author Seiji Sogabe
 * @since 0.7
 */
public class DefaultChangeSet extends AbstractChangeSet<Entry> {

    private static final long serialVersionUID = 1L;

    public DefaultChangeSet(final int id, final RepositoryBrowser<Entry> repoBrowser, final Entry entry) {
        super(id, repoBrowser, entry);
    }

    @Override
    public String createChangeLog() {
        final StringBuilder text = new StringBuilder();
        text.append(Messages.ChangeSet_Revision(UNKNOWN_REVISION, getChangeSetLink()));
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

    private List<String> getAffectedPaths() {
        final List<String> paths = new ArrayList<String>();
        for (final String path : entry.getAffectedPaths()) {
            paths.add(path);
        }
        return paths;
    }
}
