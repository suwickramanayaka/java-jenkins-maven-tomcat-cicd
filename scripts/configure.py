#!/usr/bin/env python3
"""Generate local-only secrets and environment settings without printing credentials."""
import argparse
import getpass
import os
from pathlib import Path
import re
import secrets
import subprocess

root = Path(__file__).resolve().parent.parent
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--repository', required=True, help='GitHub HTTPS clone URL')
parser.add_argument('--branch', default='main')
parser.add_argument('--public-app', action='store_true', help='Publish application port on all host interfaces')
parser.add_argument('--private-repository', action='store_true', help='Prompt for a read-only GitHub token')
parser.add_argument('--ssh-deploy-key', action='store_true', help='Generate a repository read-only SSH deploy key to register on GitHub')
args = parser.parse_args()
if args.private_repository and args.ssh_deploy_key:
    parser.error('Choose token authentication or an SSH deploy key, not both')
if not re.fullmatch(r'https://github\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+(?:\.git)?', args.repository):
    parser.error('Use a GitHub HTTPS URL without embedded credentials')
if not re.fullmatch(r'[A-Za-z0-9_./-]+', args.branch) or '..' in args.branch:
    parser.error('Invalid branch')
os.umask(0o077)
folder = root / '.secrets'
folder.mkdir(exist_ok=True, mode=0o700)
for name in ('jenkins_admin_password', 'tomcat_password'):
    path = folder / name
    if not path.exists():
        path.write_text(secrets.token_hex(24))
for name in ('github_username', 'github_token', 'github_ssh_key'):
    path = folder / name
    if not path.exists():
        path.write_text('')
if args.ssh_deploy_key:
    key = folder / 'github_ssh_key'
    if key.stat().st_size == 0:
        key.chmod(0o600)
        key.unlink()
        subprocess.run(['ssh-keygen', '-q', '-t', 'ed25519', '-N', '', '-C', 'java-cicd-read-only', '-f', str(key)], check=True)
if args.private_repository:
    username = input('GitHub username: ').strip()
    token = getpass.getpass('Read-only repository token (hidden): ').strip()
    if not username or not token:
        parser.error('Private repository requires username and token')
    for name, value in [('github_username', username), ('github_token', token)]:
        (folder / name).chmod(0o600)
        (folder / name).write_text(value)
# Container users differ from the host user. Files are readable to containers, while
# the enclosing host directory is 0700 and never included in Docker build contexts.
for path in folder.iterdir():
    path.chmod(0o444)
env = root / '.env'
repository = args.repository.replace('https://github.com/', 'git@github.com:') if args.ssh_deploy_key else args.repository
env.write_text(f'GIT_REPOSITORY_URL={repository}\nGIT_BRANCH={args.branch}\n'
               f'JENKINS_URL=http://localhost:8080/\n'
               f'APP_BIND_ADDRESS={"0.0.0.0" if args.public_app else "127.0.0.1"}\nAPP_PORT=8081\n')
print('Local configuration ready. Secrets are in .secrets/ (excluded from Git).')
print('Jenkins username: admin. Read .secrets/jenkins_admin_password privately when signing in.')
if args.ssh_deploy_key:
    print('Register .secrets/github_ssh_key.pub as a read-only deploy key on this repository before building.')
