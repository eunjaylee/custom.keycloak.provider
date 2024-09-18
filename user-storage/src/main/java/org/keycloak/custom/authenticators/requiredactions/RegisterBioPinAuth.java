package org.keycloak.custom.authenticators.requiredactions;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.*;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;

import java.util.List;

public class RegisterBioPinAuth implements RequiredActionProvider, RequiredActionFactory {
    private static final Logger logger = Logger.getLogger(RegisterBioPinAuth.class);
    private final KeycloakSession session;

    @Override
    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    @Deprecated
    public RegisterBioPinAuth() {
        this(null);
    }

    public RegisterBioPinAuth(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        context.challenge(createResponse(context, null, null));
    }

    protected Response createResponse(RequiredActionContext context, MultivaluedMap<String, String> formData, List<FormMessage> errors) {
        // bioAuth 페이지 띄울꺼임

        LoginFormsProvider form = context.form();
        UserModel user = context.getUser();
        List authTypes = (List<String>)user.getAttributes().get("authType");
        form.setAttribute("authType", authTypes.get(0));
        form.setAttribute("userSeq", user.getAttributes().get("userSeq"));
        user.removeRequiredAction(getId());
        context.getAuthenticationSession().removeRequiredAction(getId());
        context.success();
        return form.createForm("bio-auth.ftl");

        //return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path("bioAuth").build()).build();
    }

    @Override
    public void processAction(RequiredActionContext context) {
        UserModel user = context.getUser();
        user.removeRequiredAction(getId());
        context.getAuthenticationSession().removeRequiredAction(getId());
        context.success();
    }

    @Override
    public void close() {
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return new RegisterBioPinAuth(session);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public String getDisplayText() {
        return "register simple authenticate";
    }


    @Override
    public String getId() {
        return "RegisterBioPinAuth";
    }

    @Override
    public boolean isOneTimeAction() {
        return true;
    }

}
