# Argo Rollouts 컨트롤러 설치 (Section 7 실습)
$ErrorActionPreference = "Stop"

Write-Host "1) argo-rollouts 네임스페이스 생성 (이미 k8s-namespaces.yaml로 만들어졌다면 통과됩니다)"
kubectl --context docker-desktop create namespace argo-rollouts --dry-run=client -o yaml | `
  kubectl --context docker-desktop apply -f -

Write-Host "2) Argo Rollouts 설치"
kubectl --context docker-desktop apply -n argo-rollouts -f `
  https://github.com/argoproj/argo-rollouts/releases/latest/download/install.yaml

Write-Host "3) 컴포넌트 기동 대기"
kubectl --context docker-desktop -n argo-rollouts rollout status deploy/argo-rollouts --timeout=120s

Write-Host ""
Write-Host "kubectl 플러그인도 설치하면 편리합니다 (선택):"
Write-Host "  https://argo-rollouts.readthedocs.io/en/stable/installation/#kubectl-plugin-installation"
Write-Host "설치 후 확인: kubectl argo rollouts version"
