# 실습 Step-by-Step 가이드

이 문서는 강의 교안(`Jenkins-ArgoCD-상세강의교안.docx`)의 Lab 2~9와 그대로 대응합니다.
교안이 "왜"를 설명한다면, 이 문서는 "이 저장소의 어떤 파일을, 어떤 명령으로" 실행하는지에
집중합니다. 모든 명령은 각 폴더 이름을 프롬프트에 표시했습니다 — 지금 어디에 있는지 항상
확인하세요.

---

## 0. 시작하기 전에 (한 번만 하면 됨)

### 0-1. Docker Desktop 확인
1. Docker Desktop 실행
2. **Settings > Kubernetes > Enable Kubernetes** 체크 → Apply & Restart
3. 하단 고래 아이콘이 "Kubernetes is running" 상태가 될 때까지 대기 (수 분 소요)
4. 터미널에서 확인:
   ```bash
   kubectl config get-contexts
   ```
   목록에 `docker-desktop` 이 보이면 준비 완료.

### 0-2. 이 폴더를 본인 Git 저장소에 올리기
Jenkins와 ArgoCD 둘 다 **Git 저장소를 읽어서** 동작하기 때문에, 로컬 폴더 그대로는 안 되고
GitHub 같은 원격 저장소에 올려둔 상태여야 합니다. (예시 저장소:
`https://github.com/joneconsulting/product-service.git`)

### 0-3. Windows OS 환경에서 Powershell 사용 시
ps1 파일을 실행하기 전에 아래 명령어를 실행합니다.
```Windows Powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

---


## Section 2 — 실습 환경 구축 (Lab 2)

```bash
(infra) $ ./setup-mac.sh                # 또는 .\setup-windows.ps1
(infra) $ ./verify.sh                   # 또는 .\verify.ps1
(infra) $ ./argocd/install-argocd.sh    # 또는 .\argocd\install-argocd-windows.ps1
```
- Jenkins: http://localhost:8080 (admin / admin123!)
- ArgoCD: `kubectl --context docker-desktop port-forward svc/argocd-server -n argocd 8443:443` 후 https://localhost:8443

## Section 3 — 예제 서비스 준비 (Lab 3)

```bash
(product-service) $ docker build -t product-service:local .
(product-service) $ docker run --rm -p 8080:8080 product-service:local
(다른 터미널)      $ curl localhost:8080/api/products
```
두 저장소를 각각 GitHub에 push (README의 "저장소 URL 치환" 먼저 완료):
```bash
(product-service) $ git init && git add -A && git commit -m "init" \
                     && git remote add origin https://github.com/<내계정>/product-service.git \
                     && git push -u origin main

(product-service-manifests) $ git init && git add -A && git commit -m "init" \
                     && git remote add origin https://github.com/<내계정>/product-service-manifests.git \
                     && git push -u origin main
```

네임스페이스는 `infra/k8s-namespaces.yaml`로 이미 만들어져 있습니다 (Section 2에서 적용됨).

## Section 4 — Jenkins Push 파이프라인 (Lab 4)

1. Jenkins → New Item → `product-service-pipeline` → Pipeline
2. Pipeline > Definition: **Pipeline script from SCM**, Repository URL: `product-service` 저장소 주소, Script Path: `Jenkinsfile`
3. Jenkins 관리 > Credentials 에 아래 2개 등록
   - `kubeconfig-docker-desktop` (Secret file, `~/.kube/config` 업로드)
   - `gitops-repo-cred` (Username/Password 또는 GitHub PAT)
4. Slack 알림을 쓰려면: Credentials에 `slack-webhook-token`(Secret text)도 등록하고 `infra/casc/jenkins.yaml`의 `teamDomain`을 실제 워크스페이스로 수정 후 Jenkins 재기동
5. **Build with Parameters** → `TARGET_ENV=dev` 실행 → 즉시 배포 확인
   ```bash
   kubectl --context docker-desktop port-forward svc/product-service 8081:8080 -n product-service-dev
   curl localhost:8081/api/products
   ```
6. `TARGET_ENV=staging` 실행 → Approval 단계에서 대기 → `release-mgr` 계정으로 로그인해 승인

## Section 5 — ArgoCD 완전 정복 (Lab 5)

```bash
(product-service-manifests) $ argocd login localhost:8443 --username admin --password <초기비밀번호> --insecure

(product-service-manifests) $ kubectl --context docker-desktop apply \
    -f argocd/projects/product-service-project.yaml

(product-service-manifests) $ kubectl --context docker-desktop apply \
    -f argocd/section5-exploratory/product-service-dev-app.yaml
(product-service-manifests) $ argocd app sync product-service-dev
```
Self-Heal 체험:
```bash
kubectl --context docker-desktop scale deployment/product-service --replicas=5 -n product-service-dev
kubectl --context docker-desktop get deploy product-service -n product-service-dev --watch
```
App of Apps 실습 전 정리 후 진행:
```bash
argocd app delete product-service-dev
kubectl --context docker-desktop apply -f argocd/section5-exploratory/apps/root-app.yaml
```
ApplicationSet 실습 전 정리 후 진행:
```bash
argocd app delete root-app product-service-dev product-service-staging
kubectl --context docker-desktop apply -f argocd/section5-exploratory/appset-product-service.yaml
argocd app list
```

## Section 6 — 하이브리드 파이프라인 (Lab 6)

Section 5 실습 정리:
```bash
argocd app delete product-service-dev product-service-staging
```
production 전용 Application 적용 (수동 Sync):
```bash
(product-service-manifests) $ kubectl --context docker-desktop apply \
    -f argocd/product-service-prod-app.yaml
```
Jenkins에서 `TARGET_ENV=production` 실행 → Approval 승인 → GitOps 저장소에 커밋되는 것 확인:
```bash
argocd app get product-service-prod
argocd app diff product-service-prod
argocd app sync product-service-prod        # 운영자 수동 승인
```

## Section 7 — Progressive Delivery (Lab 7)

```bash
(infra) $ bash argocd/install-argo-rollouts.sh
```
`overlays/production`은 이미 `rollout.yaml`로 구성되어 있습니다. `application.yml`의
`app.version`을 `v2`로 바꾼 뒤 새 이미지를 빌드하고 Jenkins로 다시 production 파이프라인을
실행해보세요.
```bash
kubectl argo rollouts get rollout product-service -n product-service-prod --watch
for i in $(seq 1 20); do curl -s localhost:8082/api/products | grep version; done
kubectl argo rollouts promote product-service -n product-service-prod
```

## Section 9 — 캡스톤 (Lab 9)

장애 주입 드릴은 `application.yml`의 `app.simulate-error`를 `true`로 바꾸거나,
배포 시 환경변수 `APP_SIMULATE_ERROR=true`를 주입해 재현합니다. 자세한 시나리오는
교안 Section 9와 슬라이드 09번을 그대로 따라가세요.
