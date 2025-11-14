---
description: Create and open a pull request with comprehensive summary and test plan
argument-hint: [pr title] 
allowed-tools: Bash(git:*), Bash(gh:*)
---

# Open Pull Request

Create and open a pull request: $ARGUMENTS

## Process:
1. **Pre-flight Checks**
   - Verify current branch state and commits
   - Check if remote branch exists and is up to date
   - Review all changes that will be included in the PR

2. **Change Analysis**
   - Analyze all commits since branch diverged from main
   - Identify the scope and impact of changes
   - Ensure changes align with the intended feature/fix

3. **Push to Remote**
   - Push current branch to remote repository
   - Set up tracking if needed

4. **PR Creation**
   - Draft comprehensive PR title and description
   - Include summary of changes and their purpose
   - Add test plan with specific steps to verify functionality
   - Reference any related issues or tickets

5. **PR Body Format:**
   ```
   ## Summary
   - [Brief bullet points of main changes]
   
   ## Test Plan
   - [Specific steps to test the changes]
   - [Edge cases covered]
   - [Regression testing performed]
   
   ## Additional Notes
   - [Any deployment considerations]
   - [Breaking changes if any]
   ```

The PR will be opened automatically and the URL will be provided for review.