# Deployment architecture

![Release Observatory deployment architecture](architecture.png)

## Editable source

Download [architecture.drawio](architecture.drawio), open [draw.io / diagrams.net](https://app.diagrams.net/), and choose **File → Open from → Device**. Labels, cards, boundaries, icons and connectors are individually editable. Logos are embedded, so editing does not depend on remote icon URLs.

- [PNG preview](architecture.png): used by the main README; readable on GitHub in light or dark mode.
- [SVG preview](architecture.svg): scalable for reports and presentations.
- [draw.io document](architecture.drawio): editable source.

After editing in draw.io, export PNG and SVG with a white background and update both previews in the same commit. The previews describe the implemented September 2026 deployment, not a proposed high-availability production system.

## Reading the diagram

1. The developer pushes source changes and the Jenkinsfile to GitHub.
2. Jenkins polls the configured branch every two minutes and retrieves source using a read-only deploy key. The arrow points from Jenkins toward GitHub because this is polling, not an inbound webhook.
3. Jenkins runs the job on one Java 21 / Maven agent. Tests and the WAR/checksum are returned to Jenkins for reporting and archiving.
4. The agent deploys the WAR to Tomcat through its internal Manager text API.
5. The agent verifies health, the complete Git commit and the application page.

Green arrows show public application requests: host port 8081 reaches the Nginx proxy, which forwards `/cicd-demo/` to Tomcat's internal port 8080. Purple dashes show administrator access over SSH; Jenkins binds to host loopback and is not public. Security group rules allow SSH only from the workstation's current IP.

The storage card summarizes Docker named volumes backed by the EC2 root EBS volume; it is not another container or separate managed storage service. All four containers run on the same EC2 instance. The compose network boundary represents container networking, not an additional AWS subnet.

## Official icon sources and credits

| Asset | Publisher / source |
| --- | --- |
| Amazon EC2, Amazon EBS and user resource icons | [AWS Architecture Icons](https://aws.amazon.com/architecture/icons/), July 31, 2026 icon package |
| GitHub mark | [GitHub brand toolkit](https://brand.github.com/foundations/logo), [official PNG](https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png) |
| Jenkins logo | [Jenkins artwork](https://www.jenkins.io/artwork/), [original SVG](https://www.jenkins.io/images/logos/jenkins/jenkins.svg) |
| Apache Maven logo | [Apache Maven assets](https://maven.apache.org/images/logos/), `MavenLogoLeaf.svg` |
| Apache Tomcat logo | [Apache project logos](https://www.apache.org/logos/), [Tomcat PNG](https://www.apache.org/logos/res/tomcat/tomcat.png) |

Jenkins artwork is credited to the [Jenkins project](https://www.jenkins.io/) and its original designers at Frontside, under [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/). Vendor marks remain their respective owners' trademarks and identify the technologies used; no endorsement is implied. NGINX is a text label rather than a supplied logo asset.

Layout and project documentation: Sithum Wickramanayaka. The diagram uses editable vector shapes and official vendor icons.
