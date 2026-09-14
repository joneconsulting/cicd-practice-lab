# Jenkins × ArgoCD 실전 GitOps CI/CD — 실습 예제 패키지

"Jenkins와 ArgoCD로 배우는 실전 GitOps CI/CD" 강의(Section 0~10)의 공식 실습 예제입니다.
강의 슬라이드와 강의 교안에 나오는 코드는 전부 이 저장소의 실제 파일과 대응합니다.

## 폴더 구성 — 왜 3개로 나뉘어 있나요

이 강의는 처음부터 끝까지 저장소를 **3개**로 나눠서 사용합니다 (Section 2·3 참고).

| 폴더 | 역할 | Git 저장소로 만들어야 하나요? |
|---|---|---|
| `infra/` | Jenkins·ArgoCD·Argo Rollouts를 내 컴퓨터(Docker Desktop)에 설치하는 도구 모음 | **아니요.** 로컬에만 두고 씁니다 |
| `product-service/` | 상품 목록 제공 API — 앱 소스 코드 + Jenkinsfile | **예.** 본인 GitHub에 `product-service`로 push |
| `product-service-manifests/` | Kubernetes 매니페스트 + ArgoCD 리소스 (GitOps 저장소) | **예.** 본인 GitHub에 `product-service-manifests`로 push |

## 시작하는 순서 (Section 번호 기준)

1. **Section 2** — `infra/` 폴더에서 `setup-mac.sh`(또는 `setup-windows.ps1`) 실행 → Jenkins 기동
   → `infra/argocd/install-argocd.sh` 실행 → ArgoCD 설치
2. **Section 3** — `product-service/`, `product-service-manifests/`를 각각 본인 GitHub 저장소로 push
   (아래 "저장소 URL 치환" 섹션 필수 확인)
3. **Section 4** — Jenkins에서 Pipeline Job 생성, `product-service`를 SCM으로 연결
4. **Section 5** — `product-service-manifests/argocd/projects/product-service-project.yaml` 적용,
   `argocd/section5-exploratory/` 안의 예제들로 Application·App of Apps·ApplicationSet 실습
5. **Section 6** — `argocd/product-service-prod-app.yaml` 적용 (최종 production 구성)
6. **Section 7** — `infra/argocd/install-argo-rollouts.sh` 실행 → Canary 배포 실습
7. **Section 9** — 지금까지 만든 것을 그대로 캡스톤 시나리오에 사용

세부 단계는 `EXERCISES.md`를 그대로 따라가세요.

## 저장소 URL 치환 (필수)

아래 파일들에 있는 `<내계정>`을 실제 GitHub 계정/조직명으로 바꿔야 합니다.

```
product-service/Jenkinsfile
product-service-manifests/argocd/*.yaml
product-service-manifests/argocd/projects/*.yaml
product-service-manifests/argocd/section5-exploratory/**/*.yaml
```

macOS/Linux에서 한 번에 바꾸려면:
```bash
grep -rl "<내계정>" . | xargs sed -i '' 's/<내계정>/실제계정명/g'   # macOS
grep -rl "<내계정>" . | xargs sed -i 's/<내계정>/실제계정명/g'      # Linux
```

## 이 패키지가 슬라이드 코드와 다른 점 (알아두면 좋은 것)

슬라이드는 지면상 코드를 간결하게 보여주지만, 실제로 동작하려면 몇 가지를 보강해야 했습니다.
강사·수강생 모두 참고하세요.

- **Jenkinsfile의 Deploy 스테이지**: 슬라이드는 `kubectl apply -k overlays/${TARGET_ENV}`만
  보여주지만, 실제로는 Jenkins가 `product-service`만 체크아웃한 상태이므로 매니페스트 저장소를
  별도로 clone하는 단계가 필요합니다. `Jenkinsfile`에 이 단계가 포함되어 있습니다.
- **Kustomize와 Rollout 리소스**: Kustomize의 이미지 자동 치환 기능은 기본적으로
  Argo Rollouts의 `Rollout` 커스텀 리소스를 인식하지 못합니다. `overlays/production/kustomizeconfig.yaml`
  로 이를 보완했습니다 — 실무에서도 자주 걸리는 함정이니 `TROUBLESHOOTING.md`도 참고하세요.
- **Section 5의 ApplicationSet 예제**: production까지 자동 Sync 대상에 포함하면 "production은
  수동 승인"이라는 이 강의의 핵심 원칙과 충돌합니다. 그래서 `section5-exploratory/appset-product-service.yaml`
  은 dev·staging만 대상으로 합니다.

## 정리(예제를 다 쓴 뒤)

```bash
cd infra
docker compose down -v
kubectl --context docker-desktop delete ns product-service-dev product-service-staging product-service-prod argocd argo-rollouts
```
