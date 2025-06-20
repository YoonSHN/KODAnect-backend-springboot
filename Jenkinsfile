pipeline {
    agent any

    environment {
        DOCKER_USER = credentials('docker-user')
        DOCKER_PASS = credentials('docker-pass')
        SERVER_HOST = credentials('server-host')
        SLACK_TOKEN = credentials('slack-token')
        IMAGE_NAME = 'kodanect'

        SENTRY_AUTH_TOKEN = credentials('sentry-auth-token')
        SENTRY_DSN = credentials('sentry-dsn')
        SENTRY_ENVIRONMENT = 'prod'

        CI_FAILED = 'false'
        CD_FAILED = 'false'
        MAVEN_OPTS = '-Xmx2g -XX:+UseG1GC -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    githubNotify context: 'checkout', status: 'PENDING', description: '코드 체크아웃 중...'
                    def branchToCheckout = env.CHANGE_BRANCH ?: env.BRANCH_NAME
                    echo "Checking out branch: ${branchToCheckout}"
                    catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: "*/${branchToCheckout}"]],
                            userRemoteConfigs: [[
                                url: 'https://github.com/FC-DEV3-Final-Project/KODAnect-backend-springboot.git',
                                credentialsId: 'github-token'
                            ]],
                            extensions: [
                                [$class: 'CloneOption', shallow: false, noTags: false, depth: 0],
                                [$class: 'PruneStaleBranch'],
                                [$class: 'CleanBeforeCheckout']
                            ]
                        ])
                    }
                    if (currentBuild.currentResult == 'FAILURE') {
                        githubNotify context: 'checkout', status: 'FAILURE', description: '체크아웃 실패'
                        env.CI_FAILED = 'true'
                        error('Checkout 실패')
                    } else {
                        githubNotify context: 'checkout', status: 'SUCCESS', description: '체크아웃 완료'
                    }
                }
            }
        }

        stage('Checkstyle') {
            steps {
                script {
                    githubNotify context: 'checkstyle', status: 'PENDING', description: '체크스타일 검사 중...'
                    catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                        sh './mvnw checkstyle:check'
                    }
                    if (currentBuild.currentResult == 'FAILURE') {
                        githubNotify context: 'checkstyle', status: 'FAILURE', description: '체크스타일 검사 실패'
                        env.CI_FAILED = 'true'
                        error('Checkstyle 실패')
                    } else {
                        githubNotify context: 'checkstyle', status: 'SUCCESS', description: '체크스타일 검사 성공'
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    githubNotify context: 'build', status: 'PENDING', description: '빌드 시작...'
                    def codeChanged = sh(script: 'scripts/skip-if-no-code-change.sh', returnStatus: true)
                    if (codeChanged == 0) {
                        echo "[INFO] 코드 변경 없음. 빌드 스킵"
                        githubNotify context: 'build', status: 'SUCCESS', description: '코드 변경 없음. 빌드 스킵'
                        return
                    }
                    catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                        sh env.BRANCH_NAME == 'main' ? './mvnw clean compile' : './mvnw compile'
                    }
                    if (currentBuild.currentResult == 'FAILURE') {
                        githubNotify context: 'build', status: 'FAILURE', description: '빌드 실패'
                        env.CI_FAILED = 'true'
                        error('Build 실패')
                    } else {
                        githubNotify context: 'build', status: 'SUCCESS', description: '빌드 성공'
                    }
                }
            }
        }

        stage('Test & Coverage') {
            steps {
                script {
                    githubNotify context: 'test', status: 'PENDING', description: '변경된 테스트 실행 중...'
                    def testChanged = sh(script: 'scripts/run-changed-tests.sh', returnStatus: true)
                    if (testChanged == 0) {
                        echo "[INFO] 테스트 변경 없음. 테스트 스킵"
                        githubNotify context: 'test', status: 'SUCCESS', description: '테스트 변경 없음. 스킵'
                        return
                    }
                    catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                        sh 'scripts/run-changed-tests.sh'
                    }
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    if (currentBuild.currentResult == 'FAILURE') {
                        githubNotify context: 'test', status: 'FAILURE', description: '테스트 실패'
                        env.CI_FAILED = 'true'
                        error('Test 실패')
                    } else {
                        githubNotify context: 'test', status: 'SUCCESS', description: '테스트 성공'
                    }
                }
            }
        }

//         stage('SonarCloud Analysis') {
//             when {
//                 branch 'main'
//             }
//             steps {
//                 script {
//                     githubNotify context: 'sonar', status: 'PENDING', description: 'SonarCloud 분석 중...'
//                     withSonarQubeEnv('SonarCloud') {
//                         withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
//                             catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
//                                 sh './mvnw verify -Pwith-coverage'
//                                 def sonarCmd = "./mvnw sonar:sonar" +
//                                     " -Dsonar.projectKey=kodanect" +
//                                     " -Dsonar.organization=fc-dev3-final-project" +
//                                     " -Dsonar.token=${SONAR_TOKEN}" +
//                                     " -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml" +
//                                     " -Dsonar.branch.name=main"
//                                 sh "${sonarCmd}"
//                             }
//                             if (currentBuild.currentResult == 'FAILURE') {
//                                 githubNotify context: 'sonar', status: 'FAILURE', description: 'SonarCloud 분석 실패'
//                                 env.CI_FAILED = 'true'
//                                 error('Sonar 분석 실패')
//                             } else {
//                                 githubNotify context: 'sonar', status: 'SUCCESS', description: 'SonarCloud 분석 완료'
//                             }
//                         }
//                     }
//                 }
//             }
//         }

        stage('Docker Build & Push') {
            when {
                branch 'main'
            }
            steps {
                script {
                    imageTag = "build-${new Date().format('yyyyMMdd-HHmm')}"
                    fullImage = "docker.io/${env.DOCKER_USER}/${env.IMAGE_NAME}:${imageTag}"
                    githubNotify context: 'docker', status: 'PENDING', description: "도커 이미지 빌드 중... [${imageTag}]"
                    catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                        sh """
                            docker build \\
                              --build-arg RUN_MODE=prod \\
                              --build-arg SENTRY_AUTH_TOKEN=${SENTRY_AUTH_TOKEN} \\
                              -t ${fullImage} .
                        """
                        sh """
                            echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
                            docker push ${fullImage}
                        """
                    }
                    if (currentBuild.currentResult == 'FAILURE') {
                        githubNotify context: 'docker', status: 'FAILURE', description: '도커 푸시 실패'
                        env.CD_FAILED = 'true'
                        error('Docker Build & Push 실패')
                    } else {
                        githubNotify context: 'docker', status: 'SUCCESS', description: "도커 이미지 푸시 완료 [${imageTag}]"
                    }
                }
            }
        }

        stage('Create GitHub Deployment') {
            when {
                branch 'main'
            }
            steps {
                script {
                    withCredentials([string(credentialsId: 'github-token-string', variable: 'GITHUB_TOKEN')]) {
                        def commitSha = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                        catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                            def response = sh(
                                script: """
                                    curl -s -X POST https://api.github.com/repos/FC-DEV3-Final-Project/KODAnect-backend-springboot/deployments \\
                                    -H "Authorization: token ${GITHUB_TOKEN}" \\
                                    -H "Accept: application/vnd.github+json" \\
                                    -d '{
                                        "ref": "${commitSha}",
                                        "environment": "production",
                                        "required_contexts": [],
                                        "auto_merge": false,
                                        "description": "Jenkins 배포 트리거"
                                    }'
                                """,
                                returnStdout: true
                            ).trim()
                            def deploymentId = new groovy.json.JsonSlurperClassic().parseText(response).id
                            env.GITHUB_DEPLOYMENT_ID = deploymentId?.toString()
                        }
                    }
                }
            }
        }

        stage('Deploy to Server') {
            when {
                branch 'main'
            }
            steps {
                script {
                    githubNotify context: 'deploy', status: 'PENDING', description: '서버에 배포 중...'
                    withCredentials([
                        string(credentialsId: 'db-host', variable: 'DB_HOST'),
                        string(credentialsId: 'db-port', variable: 'DB_PORT'),
                        string(credentialsId: 'db-name', variable: 'DB_NAME'),
                        string(credentialsId: 'db-username', variable: 'DB_USERNAME'),
                        string(credentialsId: 'db-password', variable: 'DB_PASSWORD'),
                        string(credentialsId: 'spring-profile', variable: 'SPRING_PROFILES_ACTIVE'),
                        string(credentialsId: 'github-token-string', variable: 'GITHUB_TOKEN'),
                        usernamePassword(credentialsId: 'server-ssh-login', usernameVariable: 'SSH_USER', passwordVariable: 'SSH_PASS')
                    ]) {
                        sh """
                            cat > .env <<'EOF'
DB_HOST=${DB_HOST}
DB_PORT=${DB_PORT}
DB_NAME=${DB_NAME}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}
DOCKER_USER=${DOCKER_USER}
IMAGE_TAG=${imageTag}
SENTRY_AUTH_TOKEN=${SENTRY_AUTH_TOKEN}
SENTRY_DSN=${SENTRY_DSN}
SENTRY_ENVIRONMENT=${SENTRY_ENVIRONMENT}
EOF

                            sshpass -p "\$SSH_PASS" ssh -o StrictHostKeyChecking=no \$SSH_USER@\${SERVER_HOST} 'mkdir -p /root/docker-compose-prod'
                            sshpass -p "\$SSH_PASS" scp -o StrictHostKeyChecking=no .env \$SSH_USER@\${SERVER_HOST}:/root/docker-compose-prod/.env
                            sshpass -p "\$SSH_PASS" ssh -o StrictHostKeyChecking=no \$SSH_USER@\${SERVER_HOST} '
                                echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
                                if [ ! -d /root/docker-compose-prod ]; then
                                    git clone https://github.com/FC-DEV3-Final-Project/KODAnect-backend-springboot.git /root/docker-compose-prod
                                else
                                    cd /root/docker-compose-prod && git pull
                                fi
                                cd /root/docker-compose-prod &&
                                docker-compose -f docker-compose.prod.yml pull &&
                                docker-compose -f docker-compose.prod.yml up -d
                                rm -f /root/docker-compose-prod/.env
                            '
                            rm -f .env
                        """

                        githubNotify context: 'deploy', status: 'SUCCESS', description: "배포 완료 [${imageTag}]"
                        sh """
                            export GITHUB_TOKEN=${GITHUB_TOKEN}
                            gh release create ${imageTag} \\
                              --repo FC-DEV3-Final-Project/KODAnect-backend-springboot \\
                              --title "Release ${imageTag}" \\
                              --notes "이미지: ${fullImage}"
                        """
                        sh """
                            curl https://sentry.io/api/0/organizations/my-sentry-3h/releases/ \\
                              -H "Authorization: Bearer ${SENTRY_AUTH_TOKEN}" \\
                              -H 'Content-Type: application/json' \\
                              -d '{"version": "kodanect@${imageTag}", "projects": ["java-spring-boot"]}'
                        """
                        if (currentBuild.currentResult == 'FAILURE') {
                            githubNotify context: 'deploy', status: 'FAILURE', description: '배포 실패'
                            env.CD_FAILED = 'true'
                            error('배포 실패')
                        }
                    }
                }
            }
        }

        stage('Mark GitHub Deployment as Success') {
            when {
                allOf {
                    branch 'main'
                    expression { return env.GITHUB_DEPLOYMENT_ID != null }
                }
            }
            steps {
                script {
                    withCredentials([string(credentialsId: 'github-token-string', variable: 'GITHUB_TOKEN')]) {
                        sh """
                            curl -X POST https://api.github.com/repos/FC-DEV3-Final-Project/KODAnect-backend-springboot/deployments/${GITHUB_DEPLOYMENT_ID}/statuses \\
                            -H "Authorization: token ${GITHUB_TOKEN}" \\
                            -H "Accept: application/vnd.github+json" \\
                            -d '{
                                "state": "success",
                                "environment": "production",
                                "description": "Jenkins에서 배포 성공"
                            }'
                        """
                    }
                }
            }
        }

        stage('Health Check') {
            when {
                branch 'main'
            }
            steps {
                script {
                    githubNotify context: 'healthcheck', status: 'PENDING', description: '헬스체크 중...'
                    def healthCheckUrl = "http://10.8.110.14:8080/actuator/health"
                    def retries = 5
                    def success = false
                    for (int i = 0; i < retries; i++) {
                        try {
                            def response = sh(
                                script: "curl -s -o /dev/null -w '%{http_code}' ${healthCheckUrl}",
                                returnStdout: true
                            ).trim()
                            echo "헬스체크 응답 코드: ${response}"
                            if (response == '200') {
                                success = true
                                break
                            }
                        } catch (Exception e) {
                            echo "헬스체크 중 오류 발생 (시도 ${i+1}/${retries}): ${e.getMessage()}"
                        }
                        sleep 5
                    }
                    if (success) {
                        githubNotify context: 'healthcheck', status: 'SUCCESS', description: '헬스체크 성공'
                    } else {
                        githubNotify context: 'healthcheck', status: 'FAILURE', description: '헬스체크 실패'
                        env.CD_FAILED = 'true'
                        error('Health check failed')
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                githubNotify context: 'continuous-integration/jenkins/branch', status: 'SUCCESS', description: '전체 빌드 및 테스트 성공', targetUrl: "${env.BUILD_URL}"
                if (env.CHANGE_ID != null || env.BRANCH_NAME?.trim() == 'main') {
                    slackSend(channel: '4_파이널프로젝트_1조_jenkins', color: 'good', token: env.SLACK_TOKEN,
                        message: "빌드 성공: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|바로가기>)")
                    if (env.BRANCH_NAME == 'main') {
                        slackSend(channel: '4_파이널프로젝트_1조_jenkins', color: 'good', token: env.SLACK_TOKEN,
                            message: "배포 성공: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|바로가기>)")
                    }
                }
            }
        }
        failure {
            script {
                githubNotify context: 'continuous-integration/jenkins/branch', status: 'FAILURE', description: '전체 빌드 또는 테스트 실패', targetUrl: "${env.BUILD_URL}"
                if (env.CHANGE_ID != null || env.BRANCH_NAME?.trim() == 'main') {
                    slackSend(channel: '4_파이널프로젝트_1조_jenkins', color: 'danger', token: env.SLACK_TOKEN,
                        message: "빌드 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|바로가기>)")
                    if (env.BRANCH_NAME == 'main') {
                        slackSend(channel: '4_파이널프로젝트_1조_jenkins', color: 'danger', token: env.SLACK_TOKEN,
                            message: "배포 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|바로가기>)")
                    }
                }
            }
        }
    }
}
