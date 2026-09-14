# 트러블슈팅

강의 Section 8-3(자주 발생하는 문제 해결)과 함께 참고하세요. 여기서는 이 실습 예제
패키지를 실제로 구성하면서 발견한 구체적인 함정들을 정리했습니다.

## 1. Jenkins 컨테이너 안에서 kubectl이 클러스터를 못 찾음

```bash
docker compose exec jenkins cat /var/jenkins_home/.kube/config | grep server
```
서버 주소가 `kubernetes.docker.internal`인지 확인하세요. `.env`의 `KUBE_CONFIG_PATH`가
실제 kubeconfig 경로를 가리키는지도 확인합니다 (`setup-mac.sh`/`setup-windows.ps1`이 자동
생성하지만, 수동으로 옮긴 경우 어긋날 수 있습니다).

## 2. ArgoCD Application이 Unknown 상태

- `repoURL`의 `<내계정>`을 실제 계정명으로 바꿨는지 확인 (README "저장소 URL 치환" 참고)
- Private 저장소라면 ArgoCD에 Git 인증을 등록해야 합니다: Settings > Repositories > Connect Repo

## 3. Sync는 성공했는데 이미지 태그가 안 바뀜 (Rollout 전용 함정)

Kustomize의 이미지 자동 치환 기능은 기본적으로 `Deployment`, `StatefulSet` 등
잘 알려진 리소스 종류만 인식하고, Argo Rollouts의 `Rollout` 커스텀 리소스는 모릅니다.
`overlays/production/kustomizeconfig.yaml` 파일이 없거나 `kustomization.yaml`의
`configurations:` 항목이 빠졌다면 이 문제가 발생합니다. 직접 확인:

```bash
cd product-service-manifests/overlays/production
kustomize build . | grep "image: product-service"
```
버전이 기대한 태그와 다르면 `kustomizeconfig.yaml`과 `configurations:` 설정을 다시 확인하세요.

## 4. `kustomize edit set image` 명령을 Jenkins 컨테이너 안에서 찾을 수 없음

`infra/Dockerfile`에 kustomize 바이너리 설치 단계가 포함되어 있습니다. 이미지를 새로
빌드하지 않고 예전 컨테이너를 계속 쓰고 있다면 `docker compose up -d --build`로
다시 빌드하세요.

## 5. Jenkins staging Approval에서 승인 버튼이 안 보임

`release-mgr` 계정으로 로그인했는지 확인하세요 (Section 4 RBAC — `dev1` 계정은
승인 권한이 없습니다, JCasC의 `role-strategy` 설정 참고). 콘솔 출력 화면 상단의
"Proceed" / "Abort" 링크를 찾으세요.

## 6. git push 시 인증 실패 (Jenkins의 GitOps 커밋 스테이지)

`gitops-repo-cred` Credentials에 등록한 토큰이 `product-service-manifests` 저장소에
**write 권한**을 갖고 있는지 확인하세요 (GitHub Personal Access Token이라면 `repo` 스코프 필요).

## 7. ApplicationSet을 적용했는데 Application이 안 만들어짐

```bash
kubectl --context docker-desktop -n argocd logs deploy/argocd-applicationset-controller
```
로 컨트롤러 로그를 확인하세요. 이름 충돌(이전 실습에서 만든 Application이 아직 남아있는
경우)이 가장 흔한 원인입니다 — `EXERCISES.md` Section 5 순서를 그대로 따르면 방지됩니다.

## 8. Slack 알림이 안 옴

`infra/casc/jenkins.yaml`의 `slackNotifier` 블록은 예시입니다. 사용 중인 Slack
Notification 플러그인 버전에 따라 JCasC 필드명이 다를 수 있으니, 오류가 나면 이 블록을
지우고 Jenkins 관리 > System 화면에서 직접 Team Domain과 Credential을 설정하세요.

## 9. Rollout이 계속 Paused 상태

정상입니다. `pause: { duration: 1m }` 시간이 끝나거나 `kubectl argo rollouts promote`를
직접 실행해야 다음 단계로 진행됩니다.
