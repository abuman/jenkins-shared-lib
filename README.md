# Jenkins Shared Library — dockerUtils

## Repository structure

```
shared-lib/
└── vars/
    └── dockerUtils.groovy   ← all functions live here
```

## Setup in Jenkins

1. **Manage Jenkins → System → Global Pipeline Libraries**
2. Add a library:
   - Name: `shared-lib`
   - Default version: `main`
   - Retrieval method: **Modern SCM → Git**
   - Repository URL: your shared lib repo (SSH or HTTPS)
   - Credentials: your GitHub credential

---

## Using the library in a Jenkinsfile

```groovy
@Library('shared-lib') _   // underscore imports vars/* into scope

pipeline {
    agent any
    stages {
        stage('Lint') {
            steps { script { dockerUtils.lintDockerfile() } }
        }
        stage('Build') {
            steps { script { dockerUtils.buildImage('myuser/nodemain', 'v1.0') } }
        }
        stage('Scan') {
            steps { script { dockerUtils.scanImage('myuser/nodemain:v1.0') } }
        }
        stage('Push') {
            steps { script { dockerUtils.pushImage('myuser/nodemain', 'v1.0') } }
        }
        stage('Deploy') {
            steps {
                script {
                    dockerUtils.deployContainer('app-main', 'myuser/nodemain:v1.0', '3000')
                }
            }
        }
    }
}
```

---

## Function reference

### `dockerUtils.buildImage(imageName, tag)`
Builds a Docker image from the Dockerfile in the current workspace.

| Param | Example |
|---|---|
| imageName | `'myuser/nodemain'` |
| tag | `'v1.0'` |

---

### `dockerUtils.pushImage(imageName, tag, credentialsId?)`
Logs in to Docker Hub and pushes the image. Logs out after.

| Param | Default | Example |
|---|---|---|
| imageName | — | `'myuser/nodemain'` |
| tag | — | `'v1.0'` |
| credentialsId | `'dockerhub-creds'` | `'my-cred-id'` |

---

### `dockerUtils.pullImage(imageName, tag, credentialsId?)`
Logs in to Docker Hub and pulls the image.

Same params as `pushImage`.

---

### `dockerUtils.scanImage(imageName, severity?)`
Runs Trivy vulnerability scan. Fails the build if issues at or above severity threshold are found.

| Param | Default | Example |
|---|---|---|
| imageName | — | `'myuser/nodemain:v1.0'` |
| severity | `'HIGH,CRITICAL'` | `'CRITICAL'` |

---

### `dockerUtils.lintDockerfile(dockerfile?)`
Runs Hadolint against the Dockerfile.

| Param | Default |
|---|---|
| dockerfile | `'Dockerfile'` |

---

### `dockerUtils.deployContainer(containerName, imageName, hostPort, appPort?)`
Removes any existing container with the same name, then starts a new one.

| Param | Default | Example |
|---|---|---|
| containerName | — | `'app-main'` |
| imageName | — | `'myuser/nodemain:v1.0'` |
| hostPort | — | `'3000'` |
| appPort | `'3000'` | `'3000'` |
