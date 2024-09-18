```

cd themes/custom-login-admin
 
yarn install --frozen-lockfile
 
yarn build
 
yarn build-keycloak-theme
 
npx keycloakify
 
cp dist_keycloak/target/kbook*.jar ../../builds/libs/


docker build -t default-route-openshift-image-registry.apps.sdo-vrfy.test.cloud/keycloak-dev/keycloak:0.3.0 .

docker push default-route-openshift-image-registry.apps.sdo-vrfy.test.cloud/keycloak-dev/keycloak:0.3.0

docker run -e DB_USER_NAME=keycloak -e DB_PWD=keycloak -e DB_JDBC_URL=jdbc:postgresql://postgres:5432/custom  default-route-openshift-image-registry.apps.sdo-vrfy.test.cloud/keycloak-dev/keycloak:0.1.1 start-dev
```

docker run -e KBOOK_DB_USER_NAME=keycloak -e KBOOK_DB_PWD=keycloak -e KBOOK_DB_JDBC_URL=jdbc:postgresql://postgres:5432/custom  ktest:0.0.1 start-dev


http://localhost:8080/realms/start-up/.well-known/openid-configuration


