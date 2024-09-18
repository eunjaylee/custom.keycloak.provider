oc create secret generic sec-file --from-file=./quarkus.properties

oc kustomize . > keycloak-secret.yaml

kubeseal --controller-name=sealed-secrets --scope cluster-wide  --controller-namespace=kube-system --format yaml < keycloak-secret.yaml > keycloak-sealed-secret.yaml
