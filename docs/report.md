# CI/CD Project Report — working draft

> Implementation and evidence status must be updated from real runs before submission. Cloud deployment evidence is pending.

## Objective and application

The project demonstrates automated delivery of a Java web application using Git, Jenkins, Maven and Apache Tomcat. Release Observatory displays a message, application version, Git commit and Jenkins build number. `/health` and `/version` support deployment verification. The application deliberately has no database so the work focuses on delivery rather than data management.

## Architecture and environment

Use the architecture diagram in `README.md`. Jenkins coordinates the workflow; a separate agent compiles, tests and packages the WAR. Jenkins archives the WAR and checksum. The agent deploys that same file through Tomcat's Manager API. An application-only proxy exposes the homepage while management interfaces remain private.

Record actual OS, VM size, Docker/Compose, Java, Maven, Jenkins, plugin and Tomcat versions in `docs/evidence/environment.txt`. Distinguish the local validation environment from the final AWS environment.

## Pipeline explanation

Git provides version history and a stable source revision. Jenkins orchestrates the stages and stores credentials, reports and artifacts. Maven resolves dependencies, compiles Java, runs JUnit and packages a WAR. The WAR contains the web application classes/resources and is deployed to Tomcat, which supplies the Jakarta Servlet runtime.

The stages are Checkout, Build, Test, Package, Archive, Deploy and Verify. Maven packaging deliberately reruns tests; no test-skipping flag is used. Failed required tests stop delivery. Deployment checks both HTTP status and Tomcat's text response; verification checks the exact release commit so an older healthy application cannot count as a successful new release.

SCM polling checks every two minutes after the initial run. This is easy to reproduce without exposing Jenkins to receive webhooks; its tradeoff is delayed feedback and periodic repository requests.

## Results and evidence — pending completion

| Scenario | Build | Commit | Evidence | Actual result |
| --- | --- | --- | --- | --- |
| Initial deployment | Pending | Pending | Pending | Pending |
| Automatic visible change | Pending | Pending | Pending | Pending |
| Intentional test failure | Pending | Pending | Pending | Pending |
| Recovery | Pending | Pending | Pending | Pending |
| Fresh environment recreation | Pending | Pending | Pending | Pending |

Final application URL/context path: pending AWS deployment; context is `/cicd-demo/`.

## Manual versus automated deployment

Manual delivery requires a person to fetch code, choose commands, inspect test output, locate the correct WAR and copy it to the server. It is vulnerable to skipped tests, stale artifacts and inconsistent local environments. The pipeline standardizes those steps and associates results with a commit and build number. Failures provide stage-specific feedback and preserved test reports. Automation still depends on correct configuration, available dependencies, working credentials and sufficient server resources.

## Two practical improvements

1. **Rollback:** retain the previous successful WAR and redeploy it when a new release fails verification. This shortens recovery but needs careful handling of future data/schema changes.
2. **Staging and approval:** deploy to a separate staging environment first and require approval before promotion. This lets a person inspect behavior before users receive the release, at the cost of extra resources and time.

The present single-server learning setup does not implement automatic rollback, zero-downtime deployment or production HTTPS. Jenkins is reached over an SSH tunnel. No claim of production readiness is made.

## Attribution and own contribution

The assignment references https://youtu.be/pRHIw-WAZsg. List any parts actually adapted after consulting it. This project's application and setup were developed independently with AI assistance; review your institution's disclosure policy and explain the implementation in your own words. Official documentation references are listed in the README.
