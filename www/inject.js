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
        
        const style = document.createElement('style');
        style.id = 'upsi-mobile-css';
        style.textContent = `
            /* PWA Safe Area Handling - Fix Status Bar Overlap (Portal Content Sahaja) */
            :root {
                --safe-area-inset-top: env(safe-area-inset-top, 5px);
                --safe-area-inset-bottom: env(safe-area-inset-bottom, 0px);
                --safe-area-inset-left: env(safe-area-inset-left, 0px);
                --safe-area-inset-right: env(safe-area-inset-right, 0px);
            }
            
            /* Fix untuk html element - hanya untuk portal */
            html {
                padding-top: var(--safe-area-inset-top) !important;
                padding-bottom: var(--safe-area-inset-bottom) !important;
                padding-left: var(--safe-area-inset-left) !important;
                padding-right: var(--safe-area-inset-right) !important;
            }
            
            /* Fix untuk body - prevent overlap dengan status bar */
            body {
                margin-top: 0 !important;
                padding-top: var(--safe-area-inset-top) !important;
                padding-bottom: var(--safe-area-inset-bottom) !important;
                padding-left: var(--safe-area-inset-left) !important;
                padding-right: var(--safe-area-inset-right) !important;
            }
            
            /* Fix untuk login page header container */
            .container-fluid.container-xl.position-relative.d-flex.align-items-center.justify-content-between {
                padding-top: 25px !important;
            }
            
            /* Fix untuk dashboard page header dan content */
            .page-header, .page-content {
                padding-top: 30px !important;
                height: auto !important;
            }
            
            /* Background color untuk dashboard page header */
            .page-header {
                background-color: #4D2677 !important;
            }
            
            /* Fix untuk fixed/sticky elements */
            [style*="position: fixed"], [style*="position: sticky"] {
                 top: var(--safe-area-inset-top) !important;
             }
            
            /* Fix untuk iframe/webview content */
            iframe, webview {
                margin-top: var(--safe-area-inset-top) !important;
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
    setTimeout(injectMobileCSS, 500);
    setTimeout(injectMobileCSS, 1000);
    setTimeout(injectMobileCSS, 3000);
    
    // Re-inject when page visibility changes (portal navigation)
    document.addEventListener('visibilitychange', () => {
        if (!document.getElementById('upsi-mobile-css')) {
            injectMobileCSS();
        }
    });
})();