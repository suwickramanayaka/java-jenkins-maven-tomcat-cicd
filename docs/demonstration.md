# Demonstration and evidence

Completed captures and logs are in the [evidence gallery](evidence/README.md). The steps below reproduce the demonstration. Local validation and AWS deployment are labeled separately.

## A. Initial success

1. Push the working repository to `main`.
2. Run `java-cicd` manually once.
3. Capture Checkout, Build, Test, Package, Archive, Deploy and Verify in Jenkins.
4. Download the WAR and checksum; record the artifact URL/build number.
5. Capture JUnit test results and the application page including its commit/build.

## B. Automatic update

1. Change `app.message` in `src/main/resources/release.properties`, for example to `A new commit. A fresh release.`
2. Commit and push with a clear message such as `demo: update homepage release message`.
3. Wait for the SCM polling interval; do not press Build Now for this evidence.
4. Capture the build cause (`Started by an SCM change`), source commit and successful stages.
5. Reload the application and capture the new message and commit.

## C. Failure blocks deployment

1. Temporarily change the expected short commit in `shortensCommitWithoutChangingIdentity` to an incorrect value.
2. Commit and push with `demo: demonstrate failing test gate`.
3. Capture the assertion failure, failed Test stage, and skipped deployment.
4. Confirm the application still serves scenario B's commit at `/version`.
5. Restore the correct assertion in a new commit; preserve the demonstration history.
6. Push with `fix: restore release identifier assertion` and capture the passing recovery build.

## D. Reproducibility

After exporting the evidence, test a fresh checkout and fresh Compose project/volumes. Regenerate credentials, start the stack and run the pipeline. A restart of existing containers alone is not a recreation test. Record the fresh run separately.

## Evidence index

Save sanitized evidence in `docs/evidence/`. Keep private/raw captures in the ignored `docs/evidence/private/` directory until reviewed.

| Evidence | Suggested filename | Required caption |
| --- | --- | --- |
| Environment versions | `environment.txt` | Date, OS, exact tools and images |
| Successful pipeline | `01-success.png` | Build number and source commit |
| WAR artifact | `02-artifact.png` | Filename, checksum and build number |
| Running application | `03-application.png` | URL, version, commit and build |
| Automatic trigger | `04-trigger.png` | Trigger cause and change commit |
| Updated application | `05-updated.png` | Visible message and new commit |
| Failed test | `06-failure.png` | Assertion cause and skipped deploy |
| Recovery | `07-recovery.png` | Fix commit and successful build |
| Fresh setup | `08-recreated.png` | New environment and successful run |

Exclude account identifiers where unnecessary, tokens, passwords, SSH private keys and administrator credential screens.
