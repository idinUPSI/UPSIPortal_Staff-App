// CSS Injection for UPSI Portal Mobile Optimization
// Fix status bar overlap issue
(function() {
    'use strict';
    
    // Debug mode - set to false in production to disable console logs
    const DEBUG_MODE = false;
    
    function debugLog(...args) {
        if (DEBUG_MODE) {
            console.log(...args);
        }
    }
    
    function injectMobileCSS() {
        // Check if already injected
        if (document.getElementById('upsi-mobile-css')) return;
        
        // Jangan inject untuk splash screen (index.html)
        // Hanya inject untuk portal content (unistaff.upsi.edu.my)
        const currentUrl = window.location.href;
        const isSplashScreen = currentUrl.includes('index.html') || 
                              currentUrl.endsWith('/') || 
                              !currentUrl.includes('unistaff.upsi.edu.my');
        
        if (isSplashScreen) {
            // Untuk splash screen, jangan inject CSS yang akan affect logo position
            return;
        }
        
        // Check if current URL is login page
        const isLoginPage = currentUrl.includes('/login') || 
                           currentUrl.includes('/auth') ||
                           currentUrl.includes('login.php') ||
                           currentUrl.includes('signin');
        
        const style = document.createElement('style');
        style.id = 'upsi-mobile-css';
        
        // CSS untuk hide panels - hanya untuk login page
        const hideOtherPanelsCSS = isLoginPage ? `
            /* ===== HIDE PANELS - SHOW LOGIN ONLY ===== */
            /* Sembunyikan header/navigation sebelum login */
            header, nav, .navbar, .page-header, .header {
                display: none !important;
            }
            
            /* Sembunyikan sidebar/menu */
            aside, .sidebar, .page-sidebar, .nav-menu {
                display: none !important;
            }
            
            /* Sembunyikan footer sebelum login */
            footer, .footer, .page-footer {
                display: none !important;
            }
            
            /* Hanya paparkan container login */
            .login-container, .auth-container, [class*="login"], [id*="login"] {
                display: block !important;
                visibility: visible !important;
            }
        ` : '';
        
        style.textContent = `
            ${hideOtherPanelsCSS}
            
            /* PWA Safe Area Handling - Fix Status Bar Overlap (Portal Content Sahaja) */
           /* :root {
                --safe-area-inset-top: env(safe-area-inset-top, 0px);
                --safe-area-inset-bottom: env(safe-area-inset-bottom, 0px);
                --safe-area-inset-left: env(safe-area-inset-left, 0px);
                --safe-area-inset-right: env(safe-area-inset-right, 0px);
            } */
            
            /* Fix untuk html element - hanya untuk portal */
           /* html {
                padding-top: var(--safe-area-inset-top) !important;
                padding-bottom: var(--safe-area-inset-bottom) !important;
                padding-left: var(--safe-area-inset-left) !important;
                padding-right: var(--safe-area-inset-right) !important;
            }*/
            /* Fix untuk body - prevent overlap dengan status bar */
            /* body {
                padding-top: var(--safe-area-inset-top) !important;
                padding-bottom: var(--safe-area-inset-bottom) !important;
                padding-left: var(--safe-area-inset-left) !important;
                padding-right: var(--safe-area-inset-right) !important;
            } */
           
            /* Fix untuk login page header container */
           /* .container-fluid.container-xl.position-relative.d-flex.align-items-center.justify-content-between {
                padding-top: 25px !important;
            }*/
            
            /* Fix untuk dashboard page header dan content */
            .page-header, .page-content {
                padding-top: 30px !important;
                height: auto !important;
            }
            
            /* Background color untuk dashboard page header */
            .page-header {
                background-color: #4D2677 !important;
            }
            
            .header-function-fixed:not(.nav-function-top) .page-content {
                margin-top: 5rem;
            }

            .page-header .hidden-sm-up{
                display: none !important;
            }
            
            /* Fix untuk fixed/sticky elements */
            /* [style*="position: fixed"], [style*="position: sticky"] {
                 top: var(--safe-area-inset-top) !important;
             } */
            
            /* Fix untuk iframe/webview content */
          /*  iframe, webview {
                margin-top: var(--safe-area-inset-top) !important;
            }*/
        `;
        
        document.head.appendChild(style);
        debugLog('UPSI Mobile CSS injected - Status bar overlap fixed (Portal only)');
    }
    
    // Inject immediately if DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', injectMobileCSS);
    } else {
        injectMobileCSS();
    }
    
    // Also inject after delays for dynamic content (portal loads dynamically)
    // REDUCED: Only inject once after 1 second instead of 3 times
    setTimeout(injectMobileCSS, 1000);
    // setTimeout(injectMobileCSS, 3000); // Commented - not needed
    
    // Re-inject when page visibility changes (portal navigation)
    // COMMENTED: Not necessary and can cause performance issues
    /*
    document.addEventListener('visibilitychange', () => {
        if (!document.getElementById('upsi-mobile-css')) {
            injectMobileCSS();
        }
    });
    */
    
    // Function untuk show semua panel selepas login
    function showAllPanels() {
        const style = document.getElementById('upsi-mobile-css');
        if (style) {
            // Remove CSS rules yang hide panels
            style.textContent = style.textContent.replace(
                /\/\* ===== HIDE PANELS - SHOW LOGIN ONLY ===== \*\/[\s\S]*?\/\* PWA Safe Area Handling/,
                '/* PWA Safe Area Handling'
            );
            debugLog('All panels shown after login');
        }
    }
    
    // Detect login success dan show panels
    // Method 1: Check URL change (portal biasanya redirect selepas login)
    // COMMENTED: This MutationObserver causes lag - monitors ALL DOM changes
    /*
    let lastUrl = location.href;
    const urlObserver = new MutationObserver(() => {
        const url = location.href;
        if (url !== lastUrl) {
            lastUrl = url;
            // Jika URL berubah dari login page, show all panels
            if (!url.includes('login') && !url.includes('auth')) {
                setTimeout(showAllPanels, 100);
            }
        }
    });
    urlObserver.observe(document, { subtree: true, childList: true });
    */
    
    // Method 2: Check for dashboard/home elements (indicate successful login)
    // COMMENTED: This setInterval causes lag - querySelector every 1 second for 30 seconds
    /*
    const checkLoginSuccess = setInterval(() => {
        const isDashboard = document.querySelector('.page-content, .dashboard, [class*="dashboard"]');
        const isLoggedIn = document.querySelector('.user-profile, .logout, [class*="logout"]');
        
        if (isDashboard || isLoggedIn) {
            showAllPanels();
            clearInterval(checkLoginSuccess);
        }
    }, 1000);
    
    // Clear check after 30 seconds (prevent infinite checking)
    setTimeout(() => clearInterval(checkLoginSuccess), 30000);
    */
    
    // Expose function globally untuk manual trigger jika perlu
    window.showAllPanels = showAllPanels;
})();
