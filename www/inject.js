// CSS Injection for UPSI Portal Mobile Optimization
// Fix status bar overlap issue
(function() {
    'use strict';
    
    function injectMobileCSS() {
        // Check if already injected
        if (document.getElementById('upsi-mobile-css')) return;
        
        const style = document.createElement('style');
        style.id = 'upsi-mobile-css';
        style.textContent = `
            /* PWA Safe Area Handling - Fix Status Bar Overlap */
            :root {
                --safe-area-inset-top: env(safe-area-inset-top, 24px);
                --safe-area-inset-bottom: env(safe-area-inset-bottom, 0px);
                --safe-area-inset-left: env(safe-area-inset-left, 0px);
                --safe-area-inset-right: env(safe-area-inset-right, 0px);
                --status-bar-height: 24px;
            }
            
            /* Fix untuk html element */
            html {
                padding-top: var(--safe-area-inset-top) !important;
                padding-bottom: var(--safe-area-inset-bottom) !important;
                padding-left: var(--safe-area-inset-left) !important;
                padding-right: var(--safe-area-inset-right) !important;
            }
            
            /* Fix untuk body - prevent overlap dengan status bar */
            body {
                margin-top: 0 !important;
                padding-top: max(var(--safe-area-inset-top), var(--status-bar-height)) !important;
                padding-bottom: var(--safe-area-inset-bottom) !important;
                padding-left: var(--safe-area-inset-left) !important;
                padding-right: var(--safe-area-inset-right) !important;
                min-height: calc(100vh - var(--safe-area-inset-top) - var(--safe-area-inset-bottom)) !important;
            }
            
            /* Fix untuk header elements */
            .page-header, header, .header, [role="banner"] {
                position: relative !important;
                top: 0 !important;
                margin-top: 0 !important;
                padding-top: calc(1rem + var(--safe-area-inset-top)) !important;
                z-index: 1000 !important;
            }
            
            /* Fix untuk container elements */
            .container, .container-fluid, main, .main-content, .content, #content {
                padding-top: calc(1rem + var(--safe-area-inset-top)) !important;
                padding-left: max(1rem, var(--safe-area-inset-left)) !important;
                padding-right: max(1rem, var(--safe-area-inset-right)) !important;
                margin-top: 0 !important;
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
        console.log('UPSI Mobile CSS injected - Status bar overlap fixed');
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