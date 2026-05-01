<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${realm.displayName!'BAC Healthcare'} – Sign In</title>
  <link rel="stylesheet" href="${url.resourcesPath}/css/login.css">
</head>
<body>

  <!-- Decorative background blobs -->
  <div class="bg-blobs" aria-hidden="true">
    <div class="blob blob-1"></div>
    <div class="blob blob-2"></div>
    <div class="blob blob-3"></div>
    <div class="blob blob-4"></div>
  </div>

  <div class="page">
    <div class="card-wrap">

      <div class="card">

        <!-- Branding header -->
        <div class="card-head">
          <div class="icon-box">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="white" width="36" height="36" aria-hidden="true">
              <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 3c.55 0 1 .45 1 1v3h3c.55 0 1 .45 1 1s-.45 1-1 1h-3v3c0 .55-.45 1-1 1s-1-.45-1-1v-3H8c-.55 0-1-.45-1-1s.45-1 1-1h3V7c0-.55.45-1 1-1z"/>
            </svg>
          </div>
          <h1>BAC Healthcare</h1>
          <p class="subtitle">Healthcare Management System</p>
          <div class="accent-bar"></div>
        </div>

        <!-- Global error/warning message -->
        <#if message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
          <div class="alert alert-${message.type}" role="alert">
            <svg class="alert-icon" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
            </svg>
            <span>${kcSanitize(message.summary)?no_esc}</span>
          </div>
        </#if>

        <!-- Login form -->
        <form id="kc-form-login" action="${url.loginAction}" method="post">

          <!-- Username / Email field -->
          <#if !usernameHidden??>
          <div class="form-group">
            <label for="username">
              <#if !realm.loginWithEmailAllowed>${msg("username")}
              <#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}
              <#else>Email</#if>
            </label>
            <div class="input-wrap">
              <span class="input-icon" aria-hidden="true">
                <svg viewBox="0 0 20 20" fill="currentColor"><path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/><path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/></svg>
              </span>
              <input
                id="username"
                name="username"
                type="text"
                value="${(login.username!'')}"
                autocomplete="email"
                autofocus
                placeholder="Enter your email"
                class="${messagesPerField.existsError('username','password')?string('input-error', '')}"
              />
            </div>
            <#if messagesPerField.existsError('username')>
              <span class="field-error">${kcSanitize(messagesPerField.get('username'))?no_esc}</span>
            </#if>
          </div>
          </#if>

          <!-- Password field -->
          <div class="form-group">
            <label for="password">${msg("password")}</label>
            <div class="input-wrap">
              <span class="input-icon" aria-hidden="true">
                <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd"/></svg>
              </span>
              <input
                id="password"
                name="password"
                type="password"
                autocomplete="current-password"
                placeholder="Enter your password"
                class="${messagesPerField.existsError('username','password')?string('input-error', '')}"
              />
              <button type="button" class="eye-btn" onclick="togglePwd()" aria-label="Toggle password visibility">
                <svg id="eye-open" viewBox="0 0 20 20" fill="currentColor"><path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/><path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"/></svg>
                <svg id="eye-closed" style="display:none" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clip-rule="evenodd"/><path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.064 7 9.542 7 .847 0 1.669-.105 2.454-.303z"/></svg>
              </button>
            </div>
            <#if messagesPerField.existsError('password')>
              <span class="field-error">${kcSanitize(messagesPerField.get('password'))?no_esc}</span>
            </#if>
          </div>

          <!-- Remember me + Forgot password row -->
          <#if (realm.rememberMe && !usernameHidden??) || realm.resetPasswordAllowed>
          <div class="form-row">
            <#if realm.rememberMe && !usernameHidden??>
              <label class="checkbox-label">
                <input type="checkbox" name="rememberMe" <#if login.rememberMe??>checked</#if>>
                <span>${msg("rememberMe")}</span>
              </label>
            </#if>
            <#if realm.resetPasswordAllowed>
              <a class="forgot-link" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a>
            </#if>
          </div>
          </#if>

          <button type="submit" class="submit-btn">${msg("doLogIn")}</button>

        </form>

      </div>

      <!-- Footer -->
      <footer>
        <p>© 2025 BAC Healthcare. All rights reserved.</p>
        <div class="footer-dots">
          <span class="dot"></span>
          <span>Secure Healthcare Management</span>
          <span class="dot"></span>
        </div>
      </footer>

    </div>
  </div>

  <script>
    function togglePwd() {
      var pwd = document.getElementById('password');
      var open = document.getElementById('eye-open');
      var closed = document.getElementById('eye-closed');
      if (pwd.type === 'password') {
        pwd.type = 'text';
        open.style.display = 'none';
        closed.style.display = '';
      } else {
        pwd.type = 'password';
        open.style.display = '';
        closed.style.display = 'none';
      }
    }
  </script>

</body>
</html>
