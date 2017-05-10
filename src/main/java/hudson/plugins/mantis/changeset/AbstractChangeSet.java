package hudson.plugins.mantis.changeset;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.logging.Logger;

import hudson.model.User;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import hudson.scm.ChangeLogSet.Entry;

/**
 * AbstractChangeSet
 *
 * @author Seiji Sogabe
 * @since 0.7
 */
public abstract class AbstractChangeSet<T extends Entry> implements ChangeSet, Serializable {

    protected int id;
    protected SCM scm;
    protected T entry;

    public AbstractChangeSet(final int id, final SCM scm, final T entry) {
        this.id = id;
        this.scm = scm;
        this.entry = entry;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public abstract String createChangeLog();

    protected RepositoryBrowser getRepositoryBrowser() {
        if (scm == null) {
            return null;
        }
        return scm.getBrowser();
    }

    protected String getChangeSetLink() {
        @SuppressWarnings("unchecked")
        final RepositoryBrowser<T> browser = getRepositoryBrowser();
        if (browser == null) {
            return UNKNOWN_CHANGESETLINK;
        }

        String link = UNKNOWN_CHANGESETLINK;
        try {
            @SuppressWarnings("unchecked")
            final URL url = browser.getChangeSetLink(entry);
            if (url != null) {
                link = url.toString();
            }
        } catch (final IOException e) {
            LOGGER.warning(e.getMessage());
        }
        return link;
    }

    protected String getAuthor() {
        final User user = entry.getAuthor();
        if (user == null) {
            return UNKNOWN_AUTHOR;
        }
        return user.getId();
    }

    protected String getMsg() {
        if (entry == null) {
            return UNKNOWN_MSG;
        }
        return entry.getMsg();
    }

    private static final Logger LOGGER = Logger.getLogger(AbstractChangeSet.class.getName());
}
