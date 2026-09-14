#!/usr/bin/env bash
# Argo Rollouts 컨트롤러 설치 (Section 7 실습)
set -e

echo "1) argo-rollouts 네임스페이스 생성 (이미 k8s-namespaces.yaml로 만들어졌다면 통과됩니다)"
kubectl --context docker-desktop create namespace argo-rollouts --dry-run=client -o yaml | \
  kubectl --context docker-desktop apply -f -

echo "2) Argo Rollouts 설치"
kubectl --context docker-desktop apply -n argo-rollouts -f \
  https://github.com/argoproj/argo-rollouts/releases/latest/download/install.yaml

echo "3) 컴포넌트 기동 대기"
kubectl --context docker-desktop -n argo-rollouts rollout status deploy/argo-rollouts --timeout=120s

echo ""
echo "kubectl 플러그인도 설치하면 편리합니다 (선택):"
echo "  https://argo-rollouts.readthedocs.io/en/stable/installation/#kubectl-plugin-installation"
echo "설치 후 확인: kubectl argo rollouts version"
