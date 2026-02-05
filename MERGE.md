# PR Merge instructions for humans

In case a PR is in conflict with the latest `master` branch commits, here is the procedure that is advised to sync the PR branch to main (this can be applied to both human-made PRs and coding agents made PRs such as Jules'):

Conflicting PRs are synced using what we call a "forced master-first" conflicts resolution approach. Essentially, this boils down to putting aside the changes in the current PR, and restarting fresh from the latest `master` branch, and replaying over it the changes. This is kind of like a rebase, but the difference is that we are not trying to replay exactly the changes that were done in the PR, instead we replay the "spirit" or objective of each commit in the PR. This is a strategy that works very well with coding agents, hence why it is advised here.

### Conflicts resolution policy: forced main-first
First always try to "Sync with rebase" to the `master` branch from the GitHub UI or in commandline. If this works, great! If not, try sync with merge commit. If again this fails, continue to read.

If merging `origin/main` into the PR branch causes conflicts that cannot be automatically solved with a rebase sync to main, treat `origin/main` as canonical and reapply the PR intent on top. This is what we call the `forced main-first` conflicts resolution strategy. This can be done with a coding agent's task using the exact SOP in AGENTS.md.

Here is the prompt that can be used in conjunction with AGENTS.md:

```
This branch is out of date with the main branch. Please read AGENTS.md in main and then apply the forced main-first onflict resolution protocol to update this branch and resolve conflicts.
```

### Working style
The forced main-first conflicts resolution works better if PRs are made with the following guidelines:

- Keep PRs small and short-lived (minimize touched files and diff size).
- Continuously synchronize: merge `origin/master` into your PR branch regularly
  - Prefer updating after each merge to `master` (or at least daily) to avoid large conflicts.
- Keep CI green; add/adjust tests when behavior changes.

### After conflict is resolved
ALWAYS TEST your PR on your machine, live, before merging, to ensure that each PR squashed merge on the `master` branch leaves the project in a working state!

Once the live test is done:

Merge the synced, conflicts resolved PR branch as a Squash and Merge into the `master` branch, always.

Compared to Rebase or Merge commit, this will allow to keep a clean linear history on the `master` branch, which has two main benefits:

1. PR branches can be created, updated and synced using AI coding agents, because we can work and sync however uncleanly we want in PR branches, so that as long as the end product that we merge, ie the changes in files, is minimized and as clean as possible, the history to get there does not matter.
2. PR branches can be created and worked on in parallel, such as by both human devs and AI coding agents multiple parallel running tasks, because in the end each PR will be synced on a linear history and each of them will become a linear single linear history commit explicitly.

### Troubleshooting
* If the PR was made with a coding agent such as Jules, never try to ask it to do this conflict resolution. Instead, open a new coding agent task under a new PR (and PR branch) asking it to follow the "forced master-first" approach.
* Monitor the count on the "Files changed" tab in GitHub UI: if you had to use the `forced master-first` conflicts resolution strategy, and you made separated PR for that, and you merged it in your original PR that needed to be synced, and Github now shows that a lot more files on the "Files changed" counter, meaning a lot more files from the project than intended will be modified if this PR gets merged, this is not normal. Make sure that maybe there aren't unresolved conflicts left, which regularly happens when the conflicts resolution was done by an AI coding agent, but usually what remains is easy to manually fix and can be done directly from the GitHub web UI. Once all conflicts are resolved, GitHub UI should update asap and show in "Files changed" only the files that required to be changed for this specific PR. (NB: it doesn't matter how you merged the syncing PR into the base PR, you can choose squash and merge, merge with commit, or rebase and merge, they will all result in the same behavior, in all cases GitHub should display only the files changed in the base PR, not all the files from the syncing PR).
* Jules currently (as of Jan 2026) still has an issue with angle brackets, it will stop displaying (and maybe processing) the rest of the prompt. Avoid angle brackets in your prompt. See this ticket: https://discord.com/channels/1172568727942860810/1396234038376927422
    * Meanwhile, the workaround is to systematically enclose my prompts in 2x blocks of 3 backticks ``` , this allows to force the HTML render to be a blockquote and Jules can interpret fine the prompt. Do not individually escape every angle brackets, as this may throw the LLM off and it may misinterpret the instructions.
