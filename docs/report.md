# CI/CD Project Report

> Developed by Sithum Wickramanayaka. Functional implementation, local/AWS validation and screenshots are included. Review and personalize the reflection before academic submission.

## Objective and application

The project demonstrates automated delivery of a Java web application using Git, Jenkins, Maven and Apache Tomcat. Release Observatory displays a message, application version, Git commit and Jenkins build number. `/health` and `/version` support deployment verification. The application deliberately has no database so the work focuses on delivery rather than data management.

## Architecture and environment

Use the architecture diagram in `README.md`. Jenkins coordinates the workflow; a separate agent compiles, tests and packages the WAR. Jenkins archives the WAR and checksum. The agent deploys that same file through Tomcat's Manager API. An application-only proxy exposes the homepage while management interfaces remain private.

Actual software versions are recorded in `docs/evidence/local-environment.txt` and `docs/evidence/aws-environment.txt`; exact plugin versions are in `infrastructure/jenkins/plugins.txt`. AWS uses Ubuntu 24.04 on a c7i-flex.large (2 vCPU, 4 GiB RAM), with a 25 GiB encrypted gp3 root volume. Local validation uses Docker Desktop on ARM64; the fresh AWS environment uses x86_64.

## Pipeline explanation

Git provides version history and a stable source revision. Jenkins orchestrates the stages and stores credentials, reports and artifacts. Maven resolves dependencies, compiles Java, runs JUnit and packages a WAR. The WAR contains the web application classes/resources and is deployed to Tomcat, which supplies the Jakarta Servlet runtime.

The stages are Checkout, Build, Test, Package, Archive, Deploy and Verify. Maven packaging deliberately reruns tests; no test-skipping flag is used. Failed required tests stop delivery. Deployment checks both HTTP status and Tomcat's text response; verification checks the exact release commit so an older healthy application cannot count as a successful new release.

SCM polling checks every two minutes after the initial run. This is easy to reproduce without exposing Jenkins to receive webhooks; its tradeoff is delayed feedback and periodic repository requests.

## Results and evidence

The following runs were completed on the local Docker environment on September 29, 2026. Full console logs, build metadata and JUnit results are saved under `docs/evidence/local-build-*`. AWS validation is recorded separately below.

| Scenario | Build | Commit | Evidence | Actual result |
| --- | --- | --- | --- | --- |
| Initial deployment | Local #1 | `2dc462f` | `local-build-1.*` | SUCCESS; 5 tests; archived WAR; exact commit verified |
| Automatic visible change | Local #2 | `3da6be3` | `local-build-2.*` | SUCCESS; SCM change triggered build; updated message deployed |
| Intentional test failure | Local #3 | `1a6f908` | `local-build-3.*`, `local-failure-live-release.txt` | FAILURE; deployment skipped; build #2 remained live |
| Recovery | Local #4 | `b02f773` | `local-build-4.*` | SUCCESS; corrected test; all 5 tests pass; exact commit deployed |
| Fresh AWS environment | AWS #1 | `b02f773` | `aws-build-1.*`, `aws-environment.txt` | SUCCESS; built images on fresh Ubuntu; fetched private GitHub repository; 5 tests pass; WAR archived and deployed; exact commit verified |

AWS application URL: `http://13.204.65.27:8081/cicd-demo/`. Public TCP 8081 access was enabled with user approval and verified with HTTP 200 and health `UP`; `/manager/html` returns HTTP 404. Jenkins remains private at `http://localhost:18080/` through the SSH tunnel. SSH remains restricted to the workstation's outbound IP. This temporary public IP can change after stopping and starting the server; no custom domain or HTTPS is configured.

## AWS hosting evidence

The [redacted EC2 console image](evidence/screenshots/aws-ec2-redacted.png) shows the project instance running in Mumbai with 3/3 status checks passed. Account details and the instance ID are hidden using AI-assisted image redaction; the visible deployment facts were checked against the user-supplied original. The original remains local and excluded from Git.

## Screenshots and later AWS validation

See the [captioned evidence gallery](evidence/README.md) for the running app, AWS stages, WAR artifacts, passing tests, local automatic trigger, intentional failure and recovery. AWS build #6 serves commit `b935060` with the requested author credit; its build metadata and logs are included. AWS #2 and #3 experienced GitHub SSH authentication failures. The registered key was still present, and retrying the pipeline succeeded; the exact transient cause was not established. No credentials or host verification settings were changed to bypass authentication.

## Lessons from the implementation

- A colon in the original macOS checkout path broke Java classpaths and Docker bind paths. A short path and containerized build agent removed this environment dependency.
- Browser and terminal outbound IPs differed, so the initial SSH rule timed out. Restricting SSH to the terminal’s actual address fixed connectivity.
- A healthy endpoint alone cannot establish which release is running. Verifying the complete Git SHA prevents an older release from being mistaken for a successful new deployment.
- The deliberate assertion failure stopped Package, Archive, Deploy and Verify while the prior application remained available. A failure after deployment would still require a separate rollback strategy.

## Manual versus automated deployment

Manual delivery requires a person to fetch code, choose commands, inspect test output, locate the correct WAR and copy it to the server. It is vulnerable to skipped tests, stale artifacts and inconsistent local environments. The pipeline standardizes those steps and associates results with a commit and build number. Failures provide stage-specific feedback and preserved test reports. Automation still depends on correct configuration, available dependencies, working credentials and sufficient server resources.

## Two practical improvements

1. **Rollback:** retain the previous successful WAR and redeploy it when a new release fails verification. This shortens recovery but needs careful handling of future data/schema changes.
2. **Staging and approval:** deploy to a separate staging environment first and require approval before promotion. This lets a person inspect behavior before users receive the release, at the cost of extra resources and time.

The present single-server learning setup does not implement automatic rollback, zero-downtime deployment or production HTTPS. Jenkins is reached over an SSH tunnel. No claim of production readiness is made.

## Attribution and own contribution

The assignment references https://youtu.be/pRHIw-WAZsg. List any parts actually adapted after consulting it. This project's application and setup were developed independently with AI assistance; review your institution's disclosure policy and explain the implementation in your own words. Official documentation references are listed in the README.
