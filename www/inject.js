// CSS Injection for UPSI Portal Mobile Optimization
(function() {
    'use strict';
    
    function injectMobileCSS() {
        // Check if already injected
        if (document.getElementById('upsi-mobile-css')) return;
        
        const style = document.createElement('style');
        style.id = 'upsi-mobile-css';
        style.textContent = `
            /* PWA Safe Area Handling */
            :root {
                --safe-area-inset-top: env(safe-area-inset-top, 20px);
                --safe-area-inset-bottom: env(safe-area-inset-bottom, 0px);
            }
            
            html, body {
                padding-top: var(--safe-area-inset-top) !important;
                padding-bottom: var(--safe-area-inset-bottom) !important;
                margin-top: 0 !important;
            }
            
            .page-header {
                height: auto !important;
                padding-top: calc(3rem + var(--safe-area-inset-top)) !important;
                margin-top: 0 !important;
            }
            
            /* Prevent content from going under status bar */
            .container, .container-fluid {
                padding-top: var(--safe-area-inset-top) !important;
                padding-left: env(safe-area-inset-left, 1rem) !important;
                padding-right: env(safe-area-inset-right, 1rem) !important;
            }
            
            /* Fix for overlapping content */
            .main-content, .content, #content {
                margin-top: var(--safe-area-inset-top) !important;
            }
        `;
        
        document.head.appendChild(style);
        console.log('UPSI Mobile CSS injected');
    }
    
    // Inject immediately if DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', injectMobileCSS);
    } else {
        injectMobileCSS();
    }
    
    // Also inject after a delay for dynamic content
    setTimeout(injectMobileCSS, 1000);
    setTimeout(injectMobileCSS, 3000);
})();