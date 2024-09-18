#FROM quay.io/keycloak/keycloak:24.0.2 as builder
#
## Enable health and metrics support
#COPY ./build/libs/*.jar /opt/keycloak/providers/
#
## Configure a database vendor
#ENV KC_DB=postgres
#
#WORKDIR /opt/keycloak
## for demonstration purposes only, please make sure to use proper certificates in production instead
#RUN /opt/keycloak/bin/kc.sh build --cache=ispn --cache-stack=tcp
#
#
FROM quay.io/keycloak/keycloak:24.0.2

COPY ./build/libs/*.jar /opt/keycloak/providers/
# COPY ./lib/*.jar /opt/keycloak/lib/deployment/
COPY ./lib/*.jar /opt/keycloak/providers/

COPY ./quarkus.properties /opt/keycloak/conf/

EXPOSE 8080
CMD [  "start" ]

#FROM registry.access.redhat.com/ubi9 AS ubi-micro-build
#RUN mkdir -p /mnt/rootfs
#RUN dnf install --installroot /mnt/rootfs net-tools curl --releasever 9 --setopt install_weak_deps=false --nodocs -y && \
#    dnf --installroot /mnt/rootfs clean all && \
#    rpm --root /mnt/rootfs -e --nodeps setup
#
## RUN dnf install --installroot /mnt/rootfs curl --releasever 9 --setopt install_weak_deps=false --nodocs -y && \
##     dnf --installroot /mnt/rootfs clean all && \
##     rpm --root /mnt/rootfs -e --nodeps setup
#
#FROM quay.io/keycloak/keycloak:24.0.2
#COPY --from=ubi-micro-build /mnt/rootfs /