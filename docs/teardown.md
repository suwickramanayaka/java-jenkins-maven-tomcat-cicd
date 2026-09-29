# Teardown and recreation

## Preserve first

- Push the final working source and configuration to GitHub.
- Export screenshots, sanitized console logs, test results and the evidence index.
- Download any WAR needed for submission; GitHub does not store the Jenkins artifact archive automatically.
- If Jenkins build history is important, back up its volume privately. It contains credentials and must never be committed.

## Local containers

```bash
docker compose down
```

This stops/removes project containers and network while preserving named volumes. Do not use `--volumes` until you deliberately want to erase local Jenkins history, deployed application files, workspaces and cached dependencies.

## AWS resources

After exporting evidence, terminate only the EC2 instance tagged/named for this project. Verify its root disk was deleted. Review project-associated EBS volumes, snapshots and public IP allocations and remove only those no longer needed. Stopping an instance can leave storage and other resource usage active; termination and resource review is the intended cleanup.

Check Billing afterward for remaining resources/usage. Remove the project-specific GitHub deploy key (or revoke the read token) if it will no longer be used. Keep the account on the Free Plan.

## Recreate

Check current Free Plan eligibility and credits, launch a fresh Ubuntu server, clone the repository and follow `docs/setup.md`. New credentials and a new server IP are expected. Credit expiry does not reset when servers are recreated.
