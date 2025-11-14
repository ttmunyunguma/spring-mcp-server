# Branch Protection Setup Guide

This document explains how to configure branch protection rules for the `master` branch to ensure all tests pass before allowing pull request merges.

## GitHub Actions Workflows

Two workflows have been configured:

### 1. PR Checks (`pr-checks.yml`)
- **Triggers:** When a pull request is created or updated targeting `master` or `main` branch
- **Actions:**
  - Runs all unit tests
  - Runs all integration tests
  - Publishes test results and reports
  - Generates test summary
- **Purpose:** Ensures code quality before merging

### 2. Nightly Build (`nightly-build.yml`)
- **Triggers:**
  - Scheduled: Every night at 2:00 AM UTC
  - Manual: Can be triggered manually from GitHub Actions tab
- **Actions:**
  - Checks out the `master` branch
  - Runs all tests
  - Builds the project
  - Uploads build artifacts
  - Notifies on failure
- **Purpose:** Detects issues that may have been introduced by merged changes

## Setting Up Branch Protection Rules

To enforce that PRs cannot be merged unless all tests pass, follow these steps:

### Step 1: Navigate to Branch Protection Settings

1. Go to your GitHub repository
2. Click on **Settings** (requires admin access)
3. In the left sidebar, click **Branches**
4. Under "Branch protection rules", click **Add rule** or edit existing rule for `master`

### Step 2: Configure Protection Rules

#### Basic Settings
- **Branch name pattern:** `master` (or `main` if that's your default branch)

#### Protection Rules to Enable

✅ **Require a pull request before merging**
- Check this box
- Optional: Require approvals (set to 1 or more if you want code reviews)
- Optional: Dismiss stale pull request approvals when new commits are pushed
- Optional: Require review from Code Owners

✅ **Require status checks to pass before merging** ⭐ **CRITICAL**
- Check this box
- Check "Require branches to be up to date before merging"
- In the search box, add these status checks:
  - `test` (from the PR Checks workflow)
  - `Run Tests` (the job name from pr-checks.yml)

  > **Note:** Status checks will only appear in the list after they have run at least once. Create a test PR first, then come back to add them.

✅ **Require conversation resolution before merging** (Recommended)
- Ensures all PR comments are addressed

✅ **Do not allow bypassing the above settings** (Recommended)
- Prevents admins from bypassing these rules
- Uncheck only if you need emergency override capability

#### Additional Recommended Settings

⚠️ **Require linear history** (Optional)
- Prevents merge commits, requires rebase or squash

⚠️ **Include administrators** (Recommended)
- Apply these rules even to repository admins

### Step 3: Save Changes

Click **Create** or **Save changes** at the bottom of the page.

## Verifying the Setup

### Test the PR Workflow

1. Create a new branch:
   ```bash
   git checkout -b test-ci-setup
   ```

2. Make a small change (e.g., add a comment to a file)
   ```bash
   echo "// Test comment" >> src/main/java/com/cuius/mcpserver/CoinMarketCapMcpServerApplication.java
   git add .
   git commit -m "Test CI setup"
   git push origin test-ci-setup
   ```

3. Create a pull request to `master`

4. Observe that:
   - GitHub Actions automatically runs the PR Checks workflow
   - The PR cannot be merged until the tests pass
   - Test results are displayed in the PR

### Test the Nightly Build

1. Go to **Actions** tab in your repository
2. Click on **Nightly Build** workflow
3. Click **Run workflow** button to trigger it manually
4. Verify it runs successfully

## Workflow Status Badges

Add these badges to your README.md to show build status:

```markdown
![PR Checks](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/pr-checks.yml/badge.svg)
![Nightly Build](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/nightly-build.yml/badge.svg)
```

Replace `YOUR_USERNAME` and `YOUR_REPO` with your actual GitHub username and repository name.

## Troubleshooting

### Status Checks Not Appearing

- Status checks only appear after they've run at least once
- Create a test PR first, then return to branch protection settings
- The exact name must match the job name in the workflow file

### Workflows Not Running

- Ensure workflows are in `.github/workflows/` directory
- Check the Actions tab for error messages
- Verify YAML syntax is correct

### Tests Failing

- Check the Actions tab for detailed logs
- Download test reports from the workflow artifacts
- Review test output in the job logs

## Customization

### Change Nightly Build Time

Edit `.github/workflows/nightly-build.yml`:

```yaml
schedule:
  - cron: '0 2 * * *'  # Change to desired time (UTC)
```

Cron syntax: `minute hour day month day-of-week`

Examples:
- `'0 0 * * *'` - Midnight UTC
- `'0 6 * * *'` - 6:00 AM UTC
- `'0 18 * * 1-5'` - 6:00 PM UTC, Monday through Friday

### Add Required Reviewers

In branch protection settings:
- Enable "Require a pull request before merging"
- Set "Required number of approvals before merging" to desired number

### Add Code Coverage Requirements

1. Add a code coverage tool like JaCoCo to `build.gradle`
2. Add coverage check step to workflow
3. Add coverage status check to required checks

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Branch Protection Rules](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
- [Gradle Testing Guide](https://docs.gradle.org/current/userguide/java_testing.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
