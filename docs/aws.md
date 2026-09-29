# Temporary AWS Free Plan deployment

## Cost boundary

The user's console showed Free Plan status, $100 remaining credit and an expiry of March 29, 2027. This is an observation from September 29, 2026, not a permanent entitlement. Check the live account before launch. Do not upgrade to a Paid Plan, subscribe to Marketplace images, or enable paid-only services for this project.

The plan consumes credits for eligible infrastructure; "Free tier eligible" does not mean resources use no credits. See [AWS Free Plan](https://aws.amazon.com/free/) and [eligible EC2 types](https://aws.amazon.com/free/compute/). Export the project and evidence before expiration or credit exhaustion.

## Intended server

| Setting | Value |
| --- | --- |
| Region | Asia Pacific (Mumbai), `ap-south-1` |
| Name | `java-cicd-demo` |
| OS | Canonical Ubuntu Server 24.04 LTS, official image, no software fee |
| Architecture | x86_64 |
| Instance | Free Plan eligible type with at least 4 GiB RAM; check `c7i-flex.large` availability and account quotas in console |
| Root storage | 25 GiB gp3, encrypted, delete on termination |
| Public IPv4 | Enabled for this temporary server; included in credit consumption calculation |
| SSH | Your own key; source restricted to your current public IP `/32` |
| Application | TCP 8081; initially restricted to your IP, optionally public for the demonstration |
| Jenkins/Tomcat Manager | No public inbound rules |
| Other resources | No NAT gateway, load balancer, managed database or reserved commitment |
| Metadata | Require IMDSv2 |

The combined container memory limits are about 3.4 GiB. A 1 GiB micro VM is unsuitable for this complete stack. If a qualifying instance is unavailable, stop and review alternatives rather than upgrading the account.

## Launch and installation sequence

1. Recheck Free Plan and credits in Billing.
2. Select an eligible Ubuntu image and instance size; review the displayed rate and credits.
3. Configure the SSH key, minimal security group, encrypted disk and instance name.
4. Launch only after reviewing the complete configuration.
5. Record the instance ID and public IP privately in `.local/`; do not put credentials in Git.
6. Connect via SSH, clone the repository, and follow [setup](setup.md).
7. Open Jenkins through the SSH tunnel, run `java-cicd`, and open the app URL.
8. Capture the [demonstration evidence](demonstration.md).
9. Follow [teardown](teardown.md) to remove project resources.

No instance, firewall rule, key pair, IAM user or paid upgrade is created by these repository files. Provisioning is a separate explicit step.
