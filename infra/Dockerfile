# Jenkins + Docker CLI + kubectl + kustomize 이미지
# 로컬 실습(Docker Desktop) 전용 이미지입니다. 프로덕션에는 사용하지 마세요.
FROM jenkins/jenkins:lts

USER root

# Docker CLI (호스트 도커 소켓을 마운트해서 사용 — Docker-outside-of-Docker 방식)
# kubectl (Docker Desktop Kubernetes에 접속하기 위함)
# kustomize (Section 6의 'kustomize edit set image' 스테이지에서 사용)
ARG DOCKER_CLI_VERSION=27.3.1
ARG KUSTOMIZE_VERSION=5.4.3
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl ca-certificates git && \
    ARCH="$(dpkg --print-architecture | tr -d '[:space:]')" && \
    echo "감지된 아키텍처(dpkg): '${ARCH}' / uname -m: '$(uname -m)'" && \
    case "$ARCH" in \
      amd64) DARCH=x86_64; KARCH=amd64 ;; \
      arm64) DARCH=aarch64; KARCH=arm64 ;; \
      x86_64) DARCH=x86_64; KARCH=amd64 ;; \
      aarch64) DARCH=aarch64; KARCH=arm64 ;; \
      *) echo "지원하지 않는 아키텍처: '${ARCH}' (uname -m: '$(uname -m)'). \
이 로그를 그대로 캡처해서 문의해주세요." >&2; exit 1 ;; \
    esac && \
    curl -fsSL "https://download.docker.com/linux/static/stable/${DARCH}/docker-${DOCKER_CLI_VERSION}.tgz" -o /tmp/docker.tgz && \
    tar -xzf /tmp/docker.tgz -C /tmp && \
    install -o root -g root -m 0755 /tmp/docker/docker /usr/local/bin/docker && \
    rm -rf /tmp/docker /tmp/docker.tgz && \
    KUBECTL_VERSION=$(curl -L -s https://dl.k8s.io/release/stable.txt) && \
    curl -fsSL -o /usr/local/bin/kubectl "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/${KARCH}/kubectl" && \
    chmod +x /usr/local/bin/kubectl && \
    curl -fsSL "https://github.com/kubernetes-sigs/kustomize/releases/download/kustomize%2Fv${KUSTOMIZE_VERSION}/kustomize_v${KUSTOMIZE_VERSION}_linux_${KARCH}.tar.gz" -o /tmp/kustomize.tgz && \
    tar -xzf /tmp/kustomize.tgz -C /usr/local/bin && \
    chmod +x /usr/local/bin/kustomize && \
    rm -rf /tmp/kustomize.tgz && \
    rm -rf /var/lib/apt/lists/* && \
    docker --version && kubectl version --client && kustomize version

# Jenkins 플러그인 설치
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# 설정 자동화(JCasC) — 초기 설정 마법사 생략, 관리자 계정 및 RBAC(Section 4) 자동 구성
COPY casc/jenkins.yaml /var/jenkins_home/casc.yaml
ENV CASC_JENKINS_CONFIG=/var/jenkins_home/casc.yaml
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV KUBECONFIG=/var/jenkins_home/.kube/config

# 이 실습 이미지는 root로 실행됩니다.
# (호스트 도커 소켓 권한 문제를 피하기 위한 의도적인 단순화이며, 로컬 학습용으로만 사용하세요.)
