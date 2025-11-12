// CSS Injection for UPSI Portal Mobile Optimization
// Fix status bar overlap issue
(function() {
    'use strict';
    
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
        `;
        
        document.head.appendChild(style);
        console.log('UPSI Mobile CSS injected - Status bar overlap fixed (Portal only)');
    }
    
    // Inject immediately if DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', injectMobileCSS);
    } else {
        injectMobileCSS();
    }
    
    // Also inject after delays for dynamic content (portal loads dynamically)
    // Inject once after 1 second for dynamic content
    setTimeout(injectMobileCSS, 1000);
    
    // Function untuk show semua panel selepas login
    function showAllPanels() {
        const style = document.getElementById('upsi-mobile-css');
        if (style) {
            // Remove CSS rules yang hide panels
            style.textContent = style.textContent.replace(
                /\/\* ===== HIDE PANELS - SHOW LOGIN ONLY ===== \*\/[\s\S]*?\/\* Fix untuk dashboard page header/,
                '/* Fix untuk dashboard page header'
            );
            console.log('All panels shown after login');
        }
    }
    
    // Expose function globally untuk manual trigger jika perlu
    window.showAllPanels = showAllPanels;
})();