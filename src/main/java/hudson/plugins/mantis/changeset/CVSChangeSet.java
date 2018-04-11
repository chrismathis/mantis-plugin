package hudson.plugins.mantis.changeset;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import hudson.scm.CVSChangeLogSet;
import hudson.scm.CVSRepositoryBrowser;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.ChangeLogSet.Entry;

/**
 * ChangeSet of CVS
 *
 * @author Seiji Sogabe
 * @since 0.7
 */
public class CVSChangeSet extends AbstractChangeSet<CVSChangeLogSet.CVSChangeLog> {

    private static final long serialVersionUID = 1L;
    final RepositoryBrowser<?> repoBrowser;

    public CVSChangeSet(final int id, final RepositoryBrowser<Entry> repoBrowser,
            final CVSChangeLogSet.CVSChangeLog entry) {
        super(id, repoBrowser, entry);
        this.repoBrowser = repoBrowser;
    }

    @Override
    public String createChangeLog() {
        final StringBuilder text = new StringBuilder();
        text.append(Messages.ChangeSet_Author(getAuthor()));
        text.append(CRLF);
        text.append(Messages.ChangeSet_Log(getMsg()));
        text.append(CRLF);
        text.append(Messages.ChangeSet_ChangedPaths_Header());
        text.append(CRLF);
        for (final AffectedPath path : getAffectedPaths()) {
            text.append(Messages.ChangeSet_ChangedPaths_CVS_Path(path.getMark(), path.getRevision(), path.getPath(),
                    path.getDiffLink()));
            text.append(CRLF);
        }
        text.append(CRLF);
        return text.toString();
    }

    @Override
    protected String getChangeSetLink() {
        return UNKNOWN_CHANGESETLINK;
    }

    private List<AffectedPath> getAffectedPaths() {
        final List<AffectedPath> paths = new ArrayList<AffectedPath>();
        final CVSRepositoryBrowser browser = (CVSRepositoryBrowser) repoBrowser;
        for (final CVSChangeLogSet.File file : entry.getFiles()) {
            paths.add(new AffectedPath(file, browser));
        }
        return paths;
    }

    private static class AffectedPath {

        private final CVSChangeLogSet.File file;

        private final CVSRepositoryBrowser browser;

        public AffectedPath(final CVSChangeLogSet.File file, final CVSRepositoryBrowser browser) {
            this.file = file;
            this.browser = browser;
        }

        public String getMark() {
            final EditType type = file.getEditType();
            return ChangeSetUtil.getEditTypeMark(type);
        }

        public String getPath() {
            return file.getFullName();
        }

        public String getRevision() {
            return file.getPrevrevision();
        }

        public String getDiffLink() {
            if (browser == null) {
                return UNKNOWN_CHANGESETLINK;
            }
            URL link = null;
            try {
                link = browser.getDiffLink(file);
            } catch (final IOException e) {
                // OK
                LOGGER.warning(e.getMessage());
            }
            if (link == null) {
                return UNKNOWN_CHANGESETLINK;
            }
            return link.toExternalForm();
        }
    }

    private static final Logger LOGGER = Logger.getLogger(CVSChangeLogSet.class.getName());
}
