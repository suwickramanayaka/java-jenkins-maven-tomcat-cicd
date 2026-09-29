# Reproducible setup

## Fresh Ubuntu 24.04 server

Use the AWS guide first. Connect using your own SSH key. Clone the repository into a short path such as `~/java-cicd`. For a private repository, authenticate Git using a credential helper or SSH; do not embed a token in the clone URL or save one in shell history.

```bash
sudo bash scripts/setup-ubuntu.sh
python3 scripts/configure.py \
  --repository https://github.com/suwickramanayaka/java-jenkins-maven-tomcat-cicd.git \
  --ssh-deploy-key --public-app
sudo docker compose up -d --build
sudo docker compose ps
```

Register `.secrets/github_ssh_key.pub` under repository **Settings → Deploy keys**, leaving write access disabled. The private key stays in Jenkins Credentials. The images include GitHub's published SSH host keys for verification. For token authentication, use `--private-repository` instead of `--ssh-deploy-key`.

The setup script uses Docker's official Ubuntu package repository. The configuration script generates two random passwords, preserves existing passwords on reruns, and prompts for GitHub read credentials only when requested. For GitHub, use a fine-grained token scoped to this repository with **Contents: Read-only** and an appropriate expiration. Token creation is a user action; do not share it in chat.

All five secrets files must exist, including empty GitHub files for a public repository. The enclosing `.secrets` directory has mode 0700; individual files must be readable by the different container users. Do not relax the directory permissions or copy it to GitHub.

## Access Jenkins from your Mac

```bash
ssh -i /path/to/your-key.pem -L 8080:127.0.0.1:8080 ubuntu@SERVER_IP
```

Keep this connection open, then visit http://localhost:8080/. Read `.secrets/jenkins_admin_password` privately on the server and sign in as `admin`. Avoid including the password in screenshots or logs.

Jenkins bootstrap creates:

- One administrator; self-signup and anonymous access are disabled.
- No executors on the controller.
- One inbound WebSocket build agent labeled `maven`.
- `tomcat-deployer` in Jenkins Credentials.
- `github-ssh` for an SSH deploy key, or `github-read` for a token.
- A Pipeline-from-SCM job named `java-cicd` using `Jenkinsfile`.

The bootstrap configuration is authoritative on restart. Change tracked setup files rather than relying on undocumented UI configuration. The first pipeline run is manual; the Jenkinsfile installs the polling trigger for later changes.

## Check the deployment

After the first successful pipeline:

```bash
curl -f http://SERVER_IP:8081/cicd-demo/health
curl -f http://SERVER_IP:8081/cicd-demo/version
```

The first returns `UP`; the second returns the exact source commit. The homepage identifies the release and Jenkins build.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Agent offline | `docker compose logs --tail=80 agent jenkins`; confirm bootstrap completed |
| Git checkout fails | Repository URL, branch, token repository scope and expiration |
| Jenkins inaccessible | SSH tunnel and local port 8080 availability |
| Public app inaccessible | EC2 security group, `.env` bind address, web container, and completed deployment |
| Deployment 401/403 | Matching generated Tomcat/Jenkins credential, private network access |
| Deployment HTTP 200 but pipeline fails | Tomcat can return `FAIL -` in response text; read the logged reason |
| Health works but Verify fails | Compare `/version` to build commit; an older release may be serving |
| Container exits during build | Inspect memory availability; only one build runs at a time |
| Host Maven cannot find dependencies | Avoid a colon in the checkout path; use the container agent |

Use `sudo` for Docker commands on Ubuntu unless Docker access has been deliberately configured. Never publish Jenkins port 8080 or the Docker socket to the internet.
