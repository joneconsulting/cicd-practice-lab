// product-service Jenkinsfile
// Section 4에서 처음 만들고, Section 6에서 'Update GitOps Repo' 스테이지를 추가해 완성한 버전입니다.
// 강의 슬라이드(04-Jenkins-Push-CICD.pptx, 06-하이브리드-파이프라인.pptx)의 코드를 그대로 따르되,
// 실제로 동작하도록 매니페스트 저장소 체크아웃 단계를 명시적으로 추가했습니다.
// (슬라이드 코드는 지면 관계상 이 부분을 "overlays/${TARGET_ENV}"로 축약해서 보여줍니다.)

pipeline {
  agent any

  parameters {
    choice(name: 'TARGET_ENV', choices: ['dev', 'staging', 'production'], description: '배포할 환경을 선택하세요')
  }

  environment {
    TARGET_ENV     = "${params.TARGET_ENV}"
    IMAGE_NAME     = 'product-service'
    IMAGE_TAG      = "${env.BUILD_NUMBER}"
    MANIFESTS_REPO = 'github.com/<내계정>/product-service-manifests.git'
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        sh './gradlew bootJar --no-daemon'
      }
    }

    stage('Test') {
      steps {
        sh './gradlew test --no-daemon'
      }
    }

    stage('Docker Build') {
      steps {
        sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG .'
      }
    }

    // Section 4: production 뿐 아니라 staging도 사람이 한번 확인하도록 승인 게이트를 둡니다.
    stage('Approval') {
      when {
        expression { params.TARGET_ENV != 'dev' }
      }
      steps {
        timeout(time: 15, unit: 'MINUTES') {
          input message: "${params.TARGET_ENV} 배포를 승인하시겠습니까?",
                submitter: 'release-managers'
        }
      }
    }

    // Section 4/6: dev·staging은 Jenkins가 클러스터에 직접 배포합니다 (Push).
    // 매니페스트는 product-service-manifests 저장소에 있으므로 여기서 함께 체크아웃합니다.
    stage('Deploy (dev/staging - Push)') {
      when {
        expression { params.TARGET_ENV != 'production' }
      }
      steps {
        withCredentials([
          file(credentialsId: 'kubeconfig-docker-desktop', variable: 'KUBECONFIG'),
          usernamePassword(credentialsId: 'gitops-repo-cred', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')
        ]) {
          sh '''
            rm -rf manifests-checkout
            git clone https://${GIT_USER}:${GIT_TOKEN}@${MANIFESTS_REPO} manifests-checkout
            kubectl --context docker-desktop apply -k manifests-checkout/overlays/${TARGET_ENV}
            kubectl --context docker-desktop -n product-service-${TARGET_ENV} \
              set image deployment/product-service product-service=$IMAGE_NAME:$IMAGE_TAG
          '''
        }
      }
    }

    // Section 6: production은 클러스터를 직접 건드리지 않고, 매니페스트 저장소에
    // 이미지 태그를 갱신하는 커밋만 남깁니다. 실제 배포는 ArgoCD가 담당합니다 (Pull).
    // stage('Update GitOps Repo (production - Pull)') {
    //   when {
    //     expression { params.TARGET_ENV == 'production' }
    //   }
    //   steps {
    //     withCredentials([usernamePassword(credentialsId: 'gitops-repo-cred',
    //         usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
    //       sh '''
    //         rm -rf gitops
    //         git clone https://${GIT_USER}:${GIT_TOKEN}@${MANIFESTS_REPO} gitops
    //         cd gitops/overlays/production
    //         kustomize edit set image $IMAGE_NAME=$IMAGE_NAME:$IMAGE_TAG
    //         git config user.email "jenkins@ci.local"
    //         git config user.name "jenkins-ci"
    //         git commit -am "deploy: $IMAGE_NAME:$IMAGE_TAG"
    //         git push origin main
    //       '''
    //     }
    //   }
    // }
  }

  // post {
  //   success {
  //     slackSend(channel: '#deploy', color: 'good',
  //       message: "✅ ${params.TARGET_ENV} 배포 파이프라인 성공 — ${env.IMAGE_NAME}:${env.IMAGE_TAG}")
  //   }
  //   failure {
  //     slackSend(channel: '#deploy', color: 'danger',
  //       message: "❌ ${params.TARGET_ENV} 배포 파이프라인 실패 — 빌드 번호 ${env.BUILD_NUMBER}")
  //   }
  // }
}
