// CSS Injection for UPSI Portal Mobile Optimization
(function() {
    'use strict';
    
    function injectMobileCSS() {
        // Check if already injected
        if (document.getElementById('upsi-mobile-css')) return;
        
        const style = document.createElement('style');
        style.id = 'upsi-mobile-css';
        style.textContent = `
            .page-header {
                height: auto !important;
                padding-top: 3rem !important;
            }
            
            /* Additional mobile optimizations */
            body {
                padding-top: env(safe-area-inset-top) !important;
            }
            
            .container-fluid {
                padding-left: 1rem !important;
                padding-right: 1rem !important;
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