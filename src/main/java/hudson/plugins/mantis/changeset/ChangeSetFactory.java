package hudson.plugins.mantis.changeset;

import hudson.model.Hudson;
import hudson.scm.CVSChangeLogSet;
import hudson.scm.SCM;
import hudson.scm.SubversionChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;

/**
 * ChangeSet Factory.
 *
 * @author Seiji Sogabe
 * @since 0.7
 */
public final class ChangeSetFactory {

    private ChangeSetFactory() {
        // hide default constructor
    }

    public static ChangeSet newInstance(final int id) {
        return new CompatibleChangeSet(id);
    }

    public static ChangeSet newInstance(final int id, final SCM scm, final Entry entry) {
        if (entry == null) {
            throw new IllegalArgumentException();
        }

        final Hudson hudson = Hudson.getInstance();

        // CVS
        if (hudson.getPlugin("cvs") != null && entry instanceof CVSChangeLogSet.CVSChangeLog) {
            return new CVSChangeSet(id, scm, (CVSChangeLogSet.CVSChangeLog) entry);
        }
        // Subversion
        if (hudson.getPlugin("subversion") != null && entry instanceof SubversionChangeLogSet.LogEntry) {
            return new SubversionChangeSet(id, scm, (SubversionChangeLogSet.LogEntry) entry);
        }
        // Mercurial
        if (hudson.getPlugin("mercurial") != null && entry instanceof hudson.plugins.mercurial.MercurialChangeSet) {
            return new MercurialChangeSet(id, scm, (hudson.plugins.mercurial.MercurialChangeSet) entry);
        }
        // Git
        if (hudson.getPlugin("git") != null && entry instanceof hudson.plugins.git.GitChangeSet) {
            return new GitChangeSet(id, scm, (hudson.plugins.git.GitChangeSet) entry);
        }
        // else
        return new DefaultChangeSet(id, scm, entry);
    }
}
