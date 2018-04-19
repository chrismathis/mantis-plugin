# mantis-plugin
Jenkins mantis plugin

In pipeline jobs issues can be upated with the following call:
<code>step([$class: 'MantisIssueUpdater', keepNotePrivate: false, recordChangelog: true])</code>

Configuration for other jobs has not changed.
