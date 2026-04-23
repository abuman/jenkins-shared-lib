// vars/dockerUtils.groovy
// Shared library functions for Docker build, push, scan, and deploy

/**
 * Build a Docker image
 * @param imageName  full image name e.g. 'myuser/nodemain'
 * @param tag        image tag e.g. 'v1.0'
 */
def buildImage(String imageName, String tag) {
    sh "docker build -t ${imageName}:${tag} ."
}

/**
 * Push image to Docker Hub using stored Jenkins credential
 * @param imageName     full image name e.g. 'myuser/nodemain'
 * @param tag           image tag
 * @param credentialsId Jenkins credential ID for Docker Hub
 */
def pushImage(String imageName, String tag, String credentialsId = 'dockerhub-creds') {
    withCredentials([usernamePassword(
        credentialsId: credentialsId,
        usernameVariable: 'DOCKER_USER',
        passwordVariable: 'DOCKER_PASS'
    )]) {
        sh """
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker push ${imageName}:${tag}
            docker logout
        """
    }
}

/**
 * Pull image from Docker Hub
 * @param imageName     full image name
 * @param tag           image tag
 * @param credentialsId Jenkins credential ID for Docker Hub
 */
def pullImage(String imageName, String tag, String credentialsId = 'dockerhub-creds') {
    withCredentials([usernamePassword(
        credentialsId: credentialsId,
        usernameVariable: 'DOCKER_USER',
        passwordVariable: 'DOCKER_PASS'
    )]) {
        sh """
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker pull ${imageName}:${tag}
            docker logout
        """
    }
}

/**
 * Scan image with Trivy via Docker — no local Trivy installation needed
 * Output is captured and printed as a labelled report in Jenkins logs
 * @param imageName  full image name including tag e.g. 'myuser/nodemain:v1.0'
 * @param severity   comma-separated severities to report (default: HIGH,MEDIUM,LOW)
 * @param exitCode   0 = never fail build, 1 = fail build if issues found (default: 0)
 */
def scanImage(String imageName, String severity = 'HIGH,MEDIUM,LOW', int exitCode = 0) {
    sh """
        docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v trivy-cache:/root/.cache \
            aquasec/trivy:latest image \
            --exit-code ${exitCode} \
            --severity ${severity} \
            --no-progress \
            ${imageName}
    """
}

/**
 * Lint Dockerfile with Hadolint
 * @param dockerfile  path to Dockerfile (default: 'Dockerfile')
 */
def lintDockerfile(String dockerfile = 'Dockerfile') {
    sh "docker run --rm -i hadolint/hadolint < ${dockerfile}"
}

/**
 * Deploy a container — stops old one by name, starts new one
 * @param containerName  name for the container e.g. 'app-main'
 * @param imageName      full image name with tag e.g. 'myuser/nodemain:v1.0'
 * @param hostPort       host port to bind e.g. '3000'
 * @param appPort        container port e.g. '3000'
 */
def deployContainer(String containerName, String imageName, String hostPort, String appPort = '3000') {
    sh """
        docker rm -f ${containerName} 2>/dev/null || true
        docker run -d \
            --expose ${appPort} \
            -p ${hostPort}:${appPort} \
            --name ${containerName} \
            ${imageName}
    """
    echo "${containerName} is running ${imageName} on port ${hostPort}"
}
