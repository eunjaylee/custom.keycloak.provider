<#import "template.ftl" as layout>

<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        <#--  ${msg("loginAccountTitle")}  -->
    <#elseif section = "form">
        <div
            class="min-w-[340px] mt-[15vh] md:mt-0 m-4 max-w-[340px] box-content md:p-[30px] md:m-0 flex-row items-center">
            <div class="text-center">
                <div class="mb-[20px]">
                    <span class="inline-block align-bottom"><img class="w-[62px]" src="${url.resourcesPath}/imgs/gongri.png" alt=""></span>
                    <div class="inline-block">
                        <h1 class="font-semibold text-[48px]">스타트업</h1>
                        <h2 class="font-semibold text-[15px]">
                            <span class="text-[#017CFE]">스타트업 <span
                                class="text-[#017CFE]">스타트 패키지
                        </h2>
                    </div>
                </div>
            </div>
            <div class="text-[15px]">
            <#if realm.password>
                
                <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post" novalidate="novalidate">
                    <#if !usernameHidden??>
                        <div class="${properties.kcFormGroupClass!}">
                            <label for="username" class="${properties.kcLabelClass!}">
                                <span class="pf-v5-c-form__label-text">
                                    <#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if>
                                </span>
                            </label>

                            <span class="${properties.kcInputClass!} ${messagesPerField.existsError('username','password')?then('pf-m-error', '')}">
                               
                                <input tabindex="1" id="username" name="username" value="${(login.username!'')}" type="text" autofocus autocomplete="off"
                                       aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                                       dir="ltr"
                                />
                                <#if messagesPerField.existsError('username','password')>
                                    <span class="pf-v5-c-form-control__utilities">
                                        <span class="pf-v5-c-form-control__icon pf-m-status">
                                        <i class="fas fa-exclamation-circle" aria-hidden="true"></i>
                                        </span>
                                    </span>
                                </#if>
                            </span>

                            <#if messagesPerField.existsError('username','password')>
                                <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                        ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
                                </span>
                            </#if>

                        </div>
                    </#if>

                    <div class="${properties.kcFormGroupClass!}">
                        <label for="password" class="${properties.kcLabelClass!}">
                            <span class="pf-v5-c-form__label-text">${msg("password")}</span>
                        </label>

                        <div class="${properties.kcInputGroup!}" dir="ltr">
                            <span class="${properties.kcInputClass!}">
                                <input tabindex="2" id="password" name="password" type="password" autocomplete="off"
                                       aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                                />
                            </span>

                            <button class="${properties.kcFormPasswordVisibilityButtonClass!}" type="button" aria-label="${msg('showPassword')}"
                                    aria-controls="password" data-password-toggle
                                    data-icon-show="${properties.kcFormPasswordVisibilityIconShow!}" data-icon-hide="${properties.kcFormPasswordVisibilityIconHide!}"
                                    data-label-show="${msg('showPassword')}" data-label-hide="${msg('hidePassword')}">
                                <i class="${properties.kcFormPasswordVisibilityIconShow!}" aria-hidden="true"></i>
                            </button>

                        </div>

                        <#if usernameHidden?? && messagesPerField.existsError('username','password')>
                            <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                    ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
                            </span>
                        </#if>

                    </div>

                        <#--  
                         <div>
                           <input class="bg-[#C0DBEA] w-full mt-5 py-2 px-4 outline-none focus:bg-[#dbedf6] rounded-md"
                            type="text" required placeholder="아이디">
                         </div>
                         <div>
                            <input class="bg-[#C0DBEA] w-full mt-5 py-2 px-4 outline-none focus:bg-[#dbedf6] rounded-md"
                                type="password" required placeholder="비밀번호">
                         </div>
                        -->

                      <label class="text-gray-400 text-[15px]" for="keep">
                        <#if realm.rememberMe && !usernameHidden??>
                            <#if login.rememberMe??>
                            <input class="my-4" tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checked> ${msg("rememberMe")}
                            <#else>
                            <input class="my-4" tabindex="3" id="rememberMe" name="rememberMe" type="checkbox"> ${msg("rememberMe")}
                            </#if>
                        </#if>  
                      </label> 

                      <div id="kc-form-buttons">
                        <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                        <input tabindex="4" 
                            class="w-full bg-[#D885A3] text-white py-3 leading-[120%] rounded-md hover:bg-[#dd5a8a] mb-3"  name="login" id="kc-login" type="submit"  >${msg("doLogIn")}</button>
                      </div>
                </form>



            </#if>
            </div>
        </div>
        <script type="module" src="${url.resourcesPath}/js/passwordVisibility.js"></script>
    
    
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div class="mt-5 text-gray-400 text-[15px]">
                <ul class="flex justify-center">
                    <li> 
                      <#if realm.resetPasswordAllowed>
                         <span><a tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                      </#if>
                    </li>
                    <li class="mx-2">&#124;</li>
                    <li>아이디 찾기</li>
                    <li class="mx-2">&#124;</li>
                    <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
                    <li><a tabindex="6" href="${url.registrationUrl}">${msg("doRegister")}</a></li>
                    </#if>
                </ul>
          </div>
        </#if>

    <#elseif section = "socialProviders" >
        <div class="relative">
            <span class="inline-block border-b-[1px] w-full border-gray-400"></span>
            <span
                class="w-[30%] bg-white text-center absolute left-[50%] translate-x-[-50%] text-gray-400">OR</span>
        </div>
        <#if realm.password && social.providers??>
            <div id="kc-social-providers" >
                <ul class="flex gap-6 justify-center mt-4">
                    <#list social.providers as p>
                        <li>
                            <a id="social-${p.alias}" class="${properties.kcFormSocialAccountListButtonClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountGridItem!}</#if>" aria-label="${p.displayName}"
                                    type="button" href="${p.loginUrl}">
                                <img class="w-[42px]" src="${url.resourcesPath}/imgs/ico_sns_${p.alias}@2x.png" alt="">
                            </a>
                        </li>
                    </#list>
                </ul>
            </div>
        </#if>
    </#if>

</@layout.registrationLayout>
