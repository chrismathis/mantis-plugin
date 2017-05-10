package hudson.plugins.mantis.changeset;

import java.util.ArrayList;
import java.util.List;

import hudson.scm.EditType;
import hudson.scm.SCM;
import hudson.scm.SubversionChangeLogSet;

/**
 * ChangeSet of subversion.
 *
 * @author Seiji Sogabe
 * @since 0.7
 */
public class SubversionChangeSet extends AbstractChangeSet<SubversionChangeLogSet.LogEntry> {

    private static final long serialVersionUID = 1L;

    public SubversionChangeSet(final int id, final SCM scm, final SubversionChangeLogSet.LogEntry entry) {
        super(id, scm, entry);
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
        for (final AffectedPath path : getAffectedPaths()) {
            text.append(Messages.ChangeSet_ChangedPaths_Path(path.getMark(), path.getPath()));
            text.append(CRLF);
        }
        text.append(CRLF);
        return text.toString();
    }

    protected String getRevision() {
        return String.valueOf(entry.getRevision());
    }

    private List<AffectedPath> getAffectedPaths() {
        final List<AffectedPath> paths = new ArrayList<AffectedPath>();
        for (final SubversionChangeLogSet.Path path : entry.getPaths()) {
            paths.add(new AffectedPath(path.getEditType(), path.getValue()));
        }
        return paths;
    }

    private static class AffectedPath {

        private final String mark;

        private final String path;

        public AffectedPath(final EditType type, final String path) {
            this.path = path;
            this.mark = ChangeSetUtil.getEditTypeMark(type);
        }

        public String getMark() {
            return mark;
        }

        public String getPath() {
            return path;
        }
    }
}
